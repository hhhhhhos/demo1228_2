package com.example.demo1228_2.Vo;

import com.example.demo1228_2.entity.Order;
import com.example.demo1228_2.entity.Product;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ProductUpdateByListVo {

    List<Product> productList;

    Order order;

    Set<Long> BuylistIds;
}
