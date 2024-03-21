package com.example.demo1228_2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayProperties {
    private String serverUrl;
    private String appId;
    private String privateKey;
    private String format;
    private String charset;
    private String alipayPublicKey;
    private String signType;

    private String notifyUrl;
    private String returnUrl;

    // 省略getter和setter方法
}
