package com.example.demo1228_2.controller;


import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.Vo.Address;
import com.example.demo1228_2.entity.Test;
import com.example.demo1228_2.entity.User;
import com.example.demo1228_2.service.impl.TestServiceImpl;
import com.example.demo1228_2.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yjz
 * @since 2024-01-17
 */
@RestController
@Slf4j // 自动生成log对象
@RequestMapping("/test")
public class TestController {

    @Autowired
    TestServiceImpl testService;

    @Autowired
    UserServiceImpl userService;

    @GetMapping
    public Boolean Test(){
        List<Address> list = new ArrayList<>();
        Address address = new Address();

        address.setDetail("22222");
        address.setName("sb");
        address.setPhone("13313313133");
        list.add(address);
        list.add(address);
        list.add(address);
        User user = userService.getById(33);
        user.setAddresses(list);

        return userService.updateById(user);
    }
}
