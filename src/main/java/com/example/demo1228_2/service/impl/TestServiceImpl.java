package com.example.demo1228_2.service.impl;

import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.entity.Test;
import com.example.demo1228_2.mapper.TestMapper;
import com.example.demo1228_2.service.ITestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Autowired
    TestMapper testMapper;

    // 回滚范例
    @Transactional(rollbackFor = CustomException.class)
    public void updateByList(List<Test> testList) throws Exception{
        for(Test test : testList){
            if(testMapper.updateById(test)==0)
                throw new CustomException("更新失败，可能是版本号，回滚");
        }

    }

    //测试悲观锁
    @Transactional(rollbackFor = CustomException.class)
    public void updateOneLock(Test test){

    }

}
