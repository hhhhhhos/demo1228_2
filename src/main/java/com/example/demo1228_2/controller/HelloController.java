package com.example.demo1228_2.controller;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.entity.User;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 返回json格式的控制器注解 而不是页面

@RestController
@Slf4j // 自动生成log对象
public class HelloController {


    @ApiOperation("你好啊")
    @GetMapping("/hello")
    public Long hello(String name){
        return Db.count(User.class);
    }

    @RequestMapping(value = "post1",method = RequestMethod.POST)
    public User post1(User user){
        System.out.println(user.getName());
        return user;
    }


}
