package com.example.demo1228_2.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.entity.Order;
import com.example.demo1228_2.entity.User;
import com.example.demo1228_2.entity.UserAgentDetails;
import com.example.demo1228_2.mapper.UserAgentDetailsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * <p>
 *  查访客记录的
 * </p>
 *
 * @author yjz
 * @since 2024-03-19
 */
@Slf4j
@RestController
@RequestMapping("/user-agent-details")
public class UserAgentDetailsController {

    @Autowired
    UserAgentDetailsMapper userAgentDetailsMapper;

    @GetMapping("/selectbyadmin") // 分页查询 接收params //防空设默认
    public R<Page<UserAgentDetails>> FindPageUsers(@RequestParam Map<String, String> params, HttpSession session){
        // 使用LambdaQueryChainWrapper构建查询
        LambdaQueryChainWrapper<UserAgentDetails> query = new LambdaQueryChainWrapper<>(userAgentDetailsMapper);

        /*
          "ip": null,
          "ip_location":null,
          "url":null,
          "method":null,
          "device":null,
          "brand":null,
          "brand_version":null,
          "os":null,
          "os_version":null,
          "browser":null,
          "uuid":null,
        */


        // 根据条件动态添加查询条件
        if (params.get("ip") != null) {
            query.like(UserAgentDetails::getRealIp, params.get("ip"));
        }
        if (params.get("ip_location") != null) {
            query.like(UserAgentDetails::getCity, params.get("ip_location"));
        }
        if (params.get("url") != null) {
            query.like(UserAgentDetails::getOriginalURI, params.get("url"));
        }
        if (params.get("method") != null) {
            query.like(UserAgentDetails::getMethod, params.get("method"));
        }
        if (params.get("device") != null) {
            query.like(UserAgentDetails::getDeviceName, params.get("device"));
        }
        if (params.get("brand") != null) {
            query.like(UserAgentDetails::getDeviceBrand, params.get("brand"));
        }
        if (params.get("brand_version") != null) {
            query.like(UserAgentDetails::getDeviceVersion, params.get("brand_version"));
        }
        if (params.get("os") != null) {
            query.like(UserAgentDetails::getOperatingSystemName, params.get("os"));
        }
        if (params.get("os_version") != null) {
            query.like(UserAgentDetails::getOperatingSystemVersion, params.get("os_version"));
        }
        if (params.get("browser") != null) {
            query.like(UserAgentDetails::getAgentName, params.get("browser"));
        }
        if (params.get("uuid") != null) {
            query.like(UserAgentDetails::getUser_uuid, params.get("uuid"));
        }



        // 单独处理startDate，如果存在则查询大于等于这个日期的记录
        if (params.get("startDate") != null) {
            query.ge(UserAgentDetails::getCreate_time, params.get("startDate")); // ge是“greater than or equal to”的缩写
        }
        // 单独处理endDate，如果存在则查询小于等于这个日期的记录
        if (params.get("endDate") != null) {
            query.le(UserAgentDetails::getCreate_time, params.get("endDate")); // le是“less than or equal to”的缩写
        }


        Page<UserAgentDetails> page = new Page<>(1,10);
        // 防空参数
        if(params.get("currentPage")!=null && params.get("PageSize")!=null)
            page = new Page<>(Long.parseLong(params.get("currentPage")),Long.parseLong(params.get("PageSize")));

        // 按日期排序
        query.orderByDesc(UserAgentDetails::getCreate_time);

        // 执行分页查询
        Page<UserAgentDetails> result = query.page(page);

        // map返回筛选
        R<Page<UserAgentDetails>> response = R.success(result);
        response.setMap(params);

        return response;
    }



}
