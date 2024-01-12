package com.example.demo1228_2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo1228_2.entity.Product;
import com.example.demo1228_2.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

}
