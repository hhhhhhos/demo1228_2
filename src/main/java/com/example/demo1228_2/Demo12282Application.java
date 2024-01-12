package com.example.demo1228_2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan
public class Demo12282Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo12282Application.class, args);
    }

}
