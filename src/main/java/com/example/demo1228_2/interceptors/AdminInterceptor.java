package com.example.demo1228_2.interceptors;

import com.alibaba.fastjson.JSON;
import com.example.demo1228_2.config.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class AdminInterceptor implements HandlerInterceptor { //拦截器
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getRequestURI().endsWith("byadmin")) {
            try{
                log.info("{}拦截器,拦截到byadmin结尾请求，是否放行？",request.getRequestURI());

                if(request.getSession().getAttribute("Role")==null){
                    log.info("未登录,不放行");
                    response.setHeader("Content-Type","text/html; charset=UTF-8"); // 不加这一行 无法解析中文下面
                    response.getWriter().write(JSON.toJSONString(R.error("未登录")));
                    return false;
                }


                if (!request.getSession().getAttribute("Role").toString().equals("admin")){
                    log.info("当前角色为：{}，权限不足不放行", request.getSession().getAttribute("Role").toString());
                    response.setHeader("Content-Type","text/html; charset=UTF-8"); // 不加这一行 无法解析中文下面
                    response.getWriter().write(JSON.toJSONString(R.error("当前角色为："+request.getSession().getAttribute("Role").toString()+"，权限不足")));
                    return false;
                }

                log.info("admin角色，放行byadmin结尾请求");
                return true;

            }catch (Exception e){
                log.info(e.getMessage());
                return false;
            }
        } else {
            return true;
        }
    }

}

