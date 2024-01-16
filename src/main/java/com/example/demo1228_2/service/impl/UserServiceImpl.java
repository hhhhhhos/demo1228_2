package com.example.demo1228_2.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.entity.User;
import com.example.demo1228_2.mapper.UserMapper;
import com.example.demo1228_2.service.IUserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
}
