package com.example.demo1228_2.interceptors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.entity.UserAgentDetails;
import com.example.demo1228_2.service.impl.UserAgentDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.alibaba.fastjson.JSON;


@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor { //拦截器

    @Autowired
    UserAgentDetailsServiceImpl userAgentDetailsService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        //异步存访客记录
        userAgentDetailsService.saveByAsync(handleRequestResponseData(request,response));

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

    /**
     * 从Header报头获取IP userAgent..(异步的话获取不到request response就跑了)
     *
     * @return Map
     */
    private Map<String,String> handleRequestResponseData(HttpServletRequest request, HttpServletResponse response){
        // cookie拿访客uuid
        String uuid = checkCookieUuid(request,response);

        // region 从请求中获取Nginx转发的头信息
        String realIp = request.getHeader("X-Real-IP");
        String userAgent_s = request.getHeader("User-Agent");
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String originalURI = request.getHeader("X-Original-URI");
        if(originalURI!=null && originalURI.length()>200)
            originalURI = originalURI.substring(0, 200);
        String method = request.getMethod();

        // 有无登录，登陆了存名字
        String visitor_name = null;
        try{
            visitor_name  = request.getSession().getAttribute("LoginName").toString();
        }catch (Exception e){
            log.info("not_login");
        }

        log.info("X-Real-IP:{}",realIp);
        log.info("User-Agent:{}",userAgent_s);

        Map<String,String> params = new HashMap<>();
        params.put("realIp",realIp);
        params.put("userAgent_s",userAgent_s);
        params.put("forwardedProto",forwardedProto);
        params.put("originalURI",originalURI);
        params.put("method",method);
        params.put("uuid",uuid);
        params.put("visitor_name",visitor_name);

        return params;
    }

    /**
     * 检查Cookie有无user_uuid 有就返回，没有新建再返回
     *
     * @return uuid
     */
    private String checkCookieUuid(HttpServletRequest request,HttpServletResponse response){
        // 从请求中获取cookie中的user_uuid
        String uuid = "doesn't exist";
        Cookie[] cookies = request.getCookies();
        if(cookies != null)
            for(Cookie cookie:cookies){
                log.info("{}:{}",cookie.getName(),cookie.getValue());
                if(cookie.getName().equals("user_uuid")){
                    log.info("cookie.getName().equals(\"user_uuid\")!path is:{}",cookie.getPath());
                    uuid = cookie.getValue();
                    break;
                }
            }
        if(uuid.equals("doesn't exist")){
            log.info("uuid doesn't exist,generate new..");
            uuid = UUID.randomUUID().toString();
            Cookie cookie = new Cookie("user_uuid",uuid);
            cookie.setMaxAge(60*60*24*365*5); // 不设置默认会话关闭就没了
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        return uuid;
    }


}

