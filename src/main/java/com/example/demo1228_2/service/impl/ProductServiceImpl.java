package com.example.demo1228_2.service.impl;

import com.example.demo1228_2.entity.Product;
import com.example.demo1228_2.mapper.ProductMapper;
import com.example.demo1228_2.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yjz
 * @since 2024-01-15
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

}
