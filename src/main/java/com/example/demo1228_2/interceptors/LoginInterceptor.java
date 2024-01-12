package com.example.demo1228_2.interceptors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo1228_2.config.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import com.alibaba.fastjson.JSON;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor { //拦截器
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("拦截器,拦截到请求{},是否放行？",request.getRequestURI());
        try{
            // 获取登录状态
            Object isLogin = request.getSession().getAttribute("IsLogin");
            if(isLogin!=null){
                log.info("已登录,放行");
                return true; //进行下一跳
            }else{
                response.setHeader("Content-Type","text/html; charset=UTF-8"); // 不加这一行 无法解析中文下面
                response.getWriter().write(JSON.toJSONString(R.error("未登录")));
                log.info("未登录,不放行");
                return false;
            }
        }catch (Exception e){
            log.info("拦截器异常:{}",e.getMessage());
            return false;
        }
    }
}

