package com.example.demo1228_2.config;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.demo1228_2.entity.DataResult;
import com.example.demo1228_2.mapper.DataResultMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luciad.imageio.webp.WebPWriteParam;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.net.ssl.*;
import javax.servlet.http.HttpSession;
import redis.clients.jedis.Jedis;

/**
 * <p>
 *  静态工具类
 * </p>
 *
 * @author yjz
 * @since 2024-04-22
 */
@Slf4j
public final class Tool {

    private static final String SMTP_HOST = "smtp.163.com";
    private static final int SMTP_PORT = 25;
    private static final String EMAIL_USERNAME = "blogsendhelper@163.com";
    public static String EMAIL_PASSWORD;
    public static String PHOTO_SAVE_URL;
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";


    // 密码加密器单例？
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 发起请求器单例
    public static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    // user-agent-analyze分析器单例
    public static final UserAgentAnalyzer uaa = UserAgentAnalyzer.newBuilder().build();

    // 私有构造器，防止实例化
    private Tool() {
        throw new AssertionError("Utility class cannot be instantiated");
    }


    /**
     * 返回用户在服务器session里的ID
     * @param session 你懂的
     * @return Long
     */
    public static Long getUserSessionId(HttpSession session){
        return Long.parseLong(session.getAttribute("IsLogin").toString());
    }

    /**
     * 返回当前时间 年月日-时分秒
     * @return 返回当前时间 年月日-时分秒
     */
    public static String getDateTime() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 如果原始密码与加密密码匹配，返回true；否则返回false。
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 存图片到本地（会生成时间戳）
     * @param photo 图片
     * @param path 路径
     */
    public static void saveFile(MultipartFile photo, String path) throws IOException {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        // 获取原始文件名
        String originalFileName = photo.getOriginalFilename();
        // 提取文件名（不含扩展名）
        String fileNameWithoutExt = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        // 提取文件扩展名
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        // 构造新的文件名（包含时间戳）
        String newFileName = fileNameWithoutExt + "." + System.currentTimeMillis() + extension;
        File file = new File(dir, newFileName);

        photo.transferTo(file);
    }


    /**
     * 检查目录PHOTO_SAVE_URL中是否存在具有特定名称的文件
     *
     * @param targetFileName 要搜索的文件名（包括扩展名）
     * @return 存在返回true，否则返回false
     */
    public static boolean checkFileExists(String targetFileName) {
        File directory = new File(PHOTO_SAVE_URL);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().equals(targetFileName)) {
                        return true; // 找到匹配的文件名
                    }
                }
            }
        }
        return false; // 未找到匹配的文件名
    }


    /**
     * 转换为Webp并存储到本地 (会生成时间戳）
     * @param photo 图片
     * @return 图片名字字符串（带.webp）
     */
    public static String convertToWebp(MultipartFile photo) {
        // 确保保存路径存在
        File saveDir = new File(PHOTO_SAVE_URL);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        try (InputStream input = photo.getInputStream()) {
            BufferedImage image = ImageIO.read(input);

            // 使用LocalDateTime和DateTimeFormatter获取当前的年月日时分
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            String formattedDate = now.format(formatter);

            // 构造新的文件名（包含时间戳）
            String newFileName = "product" + generateRandomString(6) + "_" + formattedDate + ".webp";

            File outputFile = new File(saveDir, newFileName);

            ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
            WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionType(writeParam.getCompressionTypes()[WebPWriteParam.LOSSY_COMPRESSION]);
            writeParam.setCompressionQuality(0.8f); // 设置压缩质量

            try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(outputFile)) {
                writer.setOutput(outputStream);
                writer.write(null, new IIOImage(image, null, null), writeParam);
            }
            return newFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert image to WebP", e);
        }
    }

    /**
     * 删除图片.webp
     * @param name 图片名字.webp
     * @return 是否删除成功
     */
    public static Boolean deleteOneWebp(String name){
        // 指定要删除的文件路径
        String filePath = Tool.PHOTO_SAVE_URL + name; // 替换为实际的文件路径
        File file = new File(filePath);
        // 如果是 noproduct.webp 不能删除！默认图片！
        if(name.equals("noproduct.webp"))return true;
        // 尝试删除文件
        return file.delete();
    }

    /**
     * 发邮件
     * @param to 给谁
     * @param subject 标题
     * @param content 内容
     * @throws Exception 抛！
     */
    public static void sendEmail(String to, String subject, String content)throws Exception {
        if (to==null) {
            throw new CustomException("收件人不能为空");
        }
        if (subject==null) {
            throw new CustomException("邮件主题不能为空");
        }
        if (content==null) {
            throw new CustomException("邮件内容不能为空");
        }

        MailAccount account = new MailAccount();
        account.setHost(SMTP_HOST);
        account.setPort(SMTP_PORT);
        account.setAuth(true);
        account.setFrom(EMAIL_USERNAME);
        account.setUser(EMAIL_USERNAME);
        account.setPass(EMAIL_PASSWORD);

        MailUtil.send(account, to, subject, content, true);


    }

    /**
     * 邮箱正则检查
     * @param email 1
     * @return 1
     */
    public static boolean isValidEmail(String email) {
        return email.matches(EMAIL_REGEX);
    }

    /**
     * 生成随机数（字母数字）
     * @param length 随机数个数
     * @return 返回随机数
     * */
    private static String generateRandomString(int length) {
        String charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charPool.length());
            sb.append(charPool.charAt(index));
        }
        return sb.toString();
    }
}
