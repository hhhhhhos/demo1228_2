package com.example.demo1228_2.dto;

import com.example.demo1228_2.entity.Buylist;
import com.example.demo1228_2.entity.Product;
import lombok.Data;

@Data //自动生成get set方法
public class BuylistDto {
    Buylist buylist;

    Product product;

}
