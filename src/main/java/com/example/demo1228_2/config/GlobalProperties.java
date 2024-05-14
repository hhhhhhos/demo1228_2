package com.example.demo1228_2.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * <p>
 *  一些全局变量 从配置注入
 * </p>
 *
 * @author yjz
 * @since 2024-04-22
 */
@Data
@Slf4j
@Component
public class GlobalProperties {

    @Value("${wechat.secret}")
    public String  WECHAT_SECRET;
    @Value("${wechat.secret.fwh}")
    public String  WECHAT_SECRET_FWH;
    @Value("${redis.secret}")
    public String  REDIS_SECRET;
    @Value("${email.secret}")
    public String  EMAIL_PASSWORD;

    @Value("${openai.key}")
    public String OPENAI_KEY;

    @Value("${file.saveurl}")
    public String  PHOTO_SAVE_URL;

    // @Component注入后才会调用@PostConstruct
    @PostConstruct
    public void init() {
        log.info("GlobalProperties通过@Component注入application.properties数据后初始化Tool数据");
        Tool.EMAIL_PASSWORD = EMAIL_PASSWORD;
        Tool.PHOTO_SAVE_URL = PHOTO_SAVE_URL;
    }
}
