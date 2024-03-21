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
 *  前端控制器
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

        // 根据条件动态添加查询条件
        if (params.get("city") != null) {
            query.like(UserAgentDetails::getCity, params.get("city"));
        }
        // 单独处理startDate，如果存在则查询大于等于这个日期的记录
        if (params.get("startDate") != null) {
            query.ge(UserAgentDetails::getCreate_time, params.get("startDate")); // ge是“greater than or equal to”的缩写
        }
        // 单独处理endDate，如果存在则查询小于等于这个日期的记录
        if (params.get("endDate") != null) {
            query.le(UserAgentDetails::getCreate_time, params.get("endDate")); // le是“less than or equal to”的缩写
        }
        if (params.get("id") != null) {
            query.eq(UserAgentDetails::getId, params.get("id"));
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
