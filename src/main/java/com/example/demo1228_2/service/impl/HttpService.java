package com.example.demo1228_2.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo1228_2.config.Tool;
import com.example.demo1228_2.entity.User;
import com.example.demo1228_2.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import com.example.demo1228_2.config.CustomException;

import javax.servlet.http.HttpSession;

@Slf4j
@Service
public class HttpService {

    /**
     * 发送Get请求
     * @param url 请求网址
     * @throws Exception 异常
     * @return statusCode状态码 body是JSON
     */
    public Map<String,String> sendGet(String url) throws Exception {
        log.info("我是{}网络请求开始",Thread.currentThread().getId());
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .build();


        HttpResponse<String> response = Tool.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Map<String,String> map = new HashMap<>();
        map.put("statusCode",String.valueOf(response.statusCode()));
        map.put("body",response.body());
        log.info("我是{}网络请求返回码：{}。内容：{}",Thread.currentThread().getId(),response.statusCode(),response.body());
        log.info("我是{}网络请求结束",Thread.currentThread().getId());
        //Thread.sleep(6000);
        return map;
    }

    @Autowired
    UserMapper userMapper;
    /**
     * 异步获取ip归属地 调用上面get请求 更新到数据库
     * @param session 1
     * @throws Exception http
     */
    @Async
    public void sendGetIpLocationToDb(HttpSession session) throws Exception{
        String location = "火星人"; // 默认值
        String realIp= session.getAttribute("X-Real-IP").toString();

        if(realIp == null)return;
        log.info("realIp:{}",realIp);
        log.info("https://api.vore.top/api/IPdata?ip="+realIp);
        Map<String,String> map = new HashMap<>();

        map = this.sendGet("https://api.vore.top/api/IPdata?ip="+realIp);
        log.info(map.get("statusCode"));


        // 200 表示获取成功
        if(map.get("statusCode").equals("200")){
            JSONObject jsonObject = JSONObject.parseObject(map.get("body"));

            String city = jsonObject.getJSONObject("ipdata").getString("info3");
            String region = jsonObject.getJSONObject("ipdata").getString("info2");
            String country = jsonObject.getJSONObject("ipdata").getString("info1");

            if (region != null && city != null && !region.isEmpty() && !city.isEmpty()) {
                location = region + ". " + city;
            } else if (city != null && !city.isEmpty()) {
                location = city;
            } else if (region != null && !region.isEmpty()) {
                location = region;
            } else if (country != null && !country.isEmpty()) {
                location = country;
            }
            log.info("城市请求成功：{}",location);
            }

        long user_id = Tool.getUserSessionId(session);// 用户id

        // 归属地更新到数据库
        User db_user = userMapper.selectById(user_id);
        db_user.setIp_location(location);
        userMapper.updateById(db_user);

    }

    /**
     * 发送POST请求到OpenAI
     * @param apiKey OpenAI API密钥
     * @return 包含状态码和响应体的Map
     * @throws Exception 抛出异常
     */
    public Map<String, String> sendPostToOpenAI(String apiKey,String json) throws Exception {
        log.info("我是{}网络请求开始",Thread.currentThread().getId());
        // 构建JSON数据
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());

        Map<String, String> resultMap = new HashMap<>();
        log.info("我是{}网络请求返回码：{}。内容：{}",Thread.currentThread().getId(),response.statusCode(),response.body());
        log.info("我是{}网络请求结束",Thread.currentThread().getId());
        resultMap.put("statusCode", String.valueOf(response.statusCode()));
        resultMap.put("body", response.body());
        return resultMap;
    }
}
