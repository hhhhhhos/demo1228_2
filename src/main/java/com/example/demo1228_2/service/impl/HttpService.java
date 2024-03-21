package com.example.demo1228_2.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo1228_2.config.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

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

        log.info("我是{}网络请求结束",Thread.currentThread().getId());
        //Thread.sleep(6000);
        return map;
    }
}
