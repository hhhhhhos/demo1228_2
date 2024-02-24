package com.example.demo1228_2.service.impl;

import com.example.demo1228_2.entity.Order;
import com.example.demo1228_2.mapper.OrderMapper;
import com.example.demo1228_2.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yjz
 * @since 2024-02-18
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

}
