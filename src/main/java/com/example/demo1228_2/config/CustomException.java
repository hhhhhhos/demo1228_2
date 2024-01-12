package com.example.demo1228_2.config;

/**
 * 自定义业务异常类
 */
public class CustomException extends Exception {
    public CustomException(String message){
        super(message);
    }
}
