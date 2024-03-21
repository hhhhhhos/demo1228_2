package com.example.demo1228_2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync // 启动类加这个 Async注解才生效？
@SpringBootApplication
@MapperScan(basePackages = { "com.example.demo1228_2.mapper"}) //不加这行service就异常
public class Demo12282Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo12282Application.class, args);
    }

}
