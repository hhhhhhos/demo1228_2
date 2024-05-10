package com.example.demo1228_2.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.Tool;
import com.example.demo1228_2.entity.UserAgentDetails;
import com.example.demo1228_2.mapper.UserAgentDetailsMapper;
import com.example.demo1228_2.service.IUserAgentDetailsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 *  访客服务实现类
 * </p>
 *
 * @author yjz
 * @since 2024-03-19
 */
@Slf4j
@Service
public class UserAgentDetailsServiceImpl extends ServiceImpl<UserAgentDetailsMapper, UserAgentDetails> implements IUserAgentDetailsService {

    @Autowired
    UserAgentDetailsMapper userAgentDetailsMapper;

    @Autowired
    HttpService httpService;

    /**
     * 异步存储访客记录
     * @param params 报头数据
     * @param insert 是否插入历史数据
     * @return UserAgentDetails
     */
    @Async
    public CompletableFuture<UserAgentDetails> saveByAsync(Map<String,String> params,Boolean insert){

        CompletableFuture<UserAgentDetails> future = new CompletableFuture<>();
        // region 初始化数据
        String uuid = params.get("uuid");
        String realIp = params.get("realIp");
        String userAgent_s = params.get("userAgent_s");
        String forwardedProto = params.get("forwardedProto");
        String originalURI = params.get("originalURI");
        String method = params.get("method");
        String visitor_name = params.get("visitor_name");
        String wechat_nickname = params.get("wechat_nickname");
        String wechat_headimgurl = params.get("wechat_headimgurl");
        String wechat_unionid = params.get("wechat_unionid");
        Long user_id = null;
        if(params.get("user_id")!=null)
            user_id = Long.parseLong(params.get("user_id"));
        // endregion

        // region 获取ip的城市（发请求）
        String location = "火星人"; // 默认值
        try{
            if(realIp == null)throw new CustomException("ip为空");
            log.info("realIp:{}",realIp);
            log.info("https://api.vore.top/api/IPdata?ip="+realIp);
            Map<String,String> map = new HashMap<>();

            map = httpService.sendGet("https://api.vore.top/api/IPdata?ip="+realIp);
            log.info(map.get("statusCode"));


            // 200 表示获取成功
            if(map.get("statusCode").equals("200")){
                JSONObject jsonObject = JSONObject.parseObject(map.get("body"));

                String city = jsonObject.getJSONObject("ipdata").getString("info3");
                String region = jsonObject.getJSONObject("ipdata").getString("info2");
                String country = jsonObject.getJSONObject("ipdata").getString("info1");

                if (region != null && city != null && !region.isEmpty() && !city.isEmpty()) {
                    location = region + ". " + city;
                } else if (city != null && !city.isEmpty()) {
                    location = city;
                } else if (region != null && !region.isEmpty()) {
                    location = region;
                } else if (country != null && !country.isEmpty()) {
                    location = country;
                }
                log.info("城市请求成功：{}",location);
            }

            log.info("当前线程id{}",Thread.currentThread().getId());
        }catch (Exception e){
            log.info("ip地址请求失败：{}",e.getMessage());
        }
        // endregion

        // region 初始化UserAgentDetails
        // 分析userAgent
        UserAgent userAgent = Tool.uaa.parse(userAgent_s);
        UserAgentDetails userAgentDetails = new UserAgentDetails(userAgent);
        // 和set 7个附加信息
        userAgentDetails.setRealIp(realIp);
        userAgentDetails.setForwardedProto(forwardedProto);
        userAgentDetails.setOriginalURI(originalURI);
        userAgentDetails.setMethod(method);
        userAgentDetails.setUser_uuid(uuid);
        userAgentDetails.setCity(location);
        userAgentDetails.setVisitor_name(visitor_name);
        userAgentDetails.setWechat_nickname(wechat_nickname);
        userAgentDetails.setWechat_headimgurl(wechat_headimgurl);
        userAgentDetails.setWechat_unionid(wechat_unionid);
        userAgentDetails.setUser_id(user_id);
        // endregion

        // 插入数据库
        if(insert){
            if(userAgentDetailsMapper.insert(userAgentDetails)==1){
                log.info("成功插入访客记录");
            }
            else{
                log.info("插入访客记录失败");
            }
        }


        future.complete(userAgentDetails); // 正常完成时设置返回值

        return future;
    }

}
