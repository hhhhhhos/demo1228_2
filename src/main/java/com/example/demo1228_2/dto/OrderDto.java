package com.example.demo1228_2.dto;

import com.example.demo1228_2.Vo.Address;
import com.example.demo1228_2.entity.Buylist;
import com.example.demo1228_2.entity.Order;
import com.example.demo1228_2.entity.Product;
import lombok.Data;

@Data //自动生成get set方法 // Dto耦合entity数据
public class OrderDto {
    Order order;

    Product product;

}
