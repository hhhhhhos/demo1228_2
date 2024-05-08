package com.example.demo1228_2.config;

import com.example.demo1228_2.interceptors.AdminInterceptor;
import com.example.demo1228_2.interceptors.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class WebConfig implements WebMvcConfigurer { //拦截器配置

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns("/vue-admin-template/user/login","/user/login","/user/regis","/product/**","/error"
                        ,"/test/notifyUrl","/user/loginByWechat","/user/loginByEmail","/user/sendEmail","/user/getCaptch");

        registry.addInterceptor(adminInterceptor)
                .excludePathPatterns("/order/selectpagebyadmin","/product/selectpagebyadmin","/user/selectpagebyadmin",
                        "/user-agent-details/selectbyadmin","/product-related-list/selectpagebyadmin");
    }

}

