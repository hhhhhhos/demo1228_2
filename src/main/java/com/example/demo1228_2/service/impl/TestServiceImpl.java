package com.example.demo1228_2.service.impl;

import com.example.demo1228_2.entity.Test;
import com.example.demo1228_2.mapper.TestMapper;
import com.example.demo1228_2.service.ITestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yjz
 * @since 2024-01-17
 */
@Service
public class TestServiceImpl extends ServiceImpl<TestMapper, Test> implements ITestService {

}
