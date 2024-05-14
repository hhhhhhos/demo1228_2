package com.example.demo1228_2.controller;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.Vo.VueAdminToken;
import com.example.demo1228_2.Vo.VueAdminUser;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.config.Tool;
import com.example.demo1228_2.entity.User;
import com.example.demo1228_2.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vue-admin-template")
@Slf4j // 自动生成log对象
public class VueAdminTemplateController {

    @Autowired
    UserServiceImpl userService;

    @PostMapping("/user/login") // 管理页面登录
    public R Login(@RequestBody VueAdminUser user, HttpSession session,HttpServletRequest request){
        log.info("{}",user);
        VueAdminToken vueAdminToken = new VueAdminToken();
        vueAdminToken.setToken("123"); // 这token没啥用 我用session验证状态
        try{
            User db_user = Db.lambdaQuery(User.class).eq(User::getName,user.getUsername()).one();
            if(db_user == null)
                throw new CustomException("用户不存在");

            if(!Tool.matches(user.getPassword(),db_user.getPassword()))
                throw new CustomException("密码错误");

            if(!db_user.getRole().equals("admin") && !db_user.getRole().equals("visitor"))
                throw new CustomException("用户无后台登录权限");

            // 验证码验证
            userService.login_check_before(user.getCaptch(),session);

            log.info("{}:密码正确，登陆成功",db_user.getName());
            log.info("设置session登录IsLogin为用户Id");
            userService.setLoginSession(db_user,session,request);

            return new R<>(20000,null,vueAdminToken );

        }catch (Exception e){
            log.info("异常:{}",e.getMessage());
            return new R<>(60204, e.getMessage(),null);
        }
    }

    @GetMapping("/user/info")
    public R getInfo(@RequestParam(defaultValue = "-1")String token, HttpSession session) {
        String role = "";
        if(session.getAttribute("Role")!=null)
            role = session.getAttribute("Role").toString();
        else
            return new R<>(50008,null,null ); // 未登录不返回20000

        Map<String, Object> data = new HashMap<>();
        data.put("roles", List.of("admin"));
        data.put("introduction", "I am a super administrator");
        data.put("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        data.put("name", role);
        return new R<>(20000,null,data );
    }

    @PostMapping("/user/logout")
    public R Logout(HttpSession session){
        if(session.getAttribute("IsLogin")!=null){
            session.invalidate();
            return new R<>(20000,null,"success");
        }
        else {
            return new R<>(0,"未登录无法登出",null);
        }
    }



    /*
    @PostMapping("/upload") // 上传图片到Tool.PHOTO_SAVE_URL路径
    public String upload(//String nickname,
                         MultipartFile photo,
                         HttpServletRequest request) throws IOException {
        //System.out.println(nickname);
        log.info(photo.getOriginalFilename());
        log.info(photo.getContentType());

        //String path = request.getServletContext().getRealPath("/upload/");
        String path = Tool.PHOTO_SAVE_URL;
        log.info(path);
        saveFile(photo, Tool.PHOTO_SAVE_URL);
        return "上传成功";
    }
    */





}
