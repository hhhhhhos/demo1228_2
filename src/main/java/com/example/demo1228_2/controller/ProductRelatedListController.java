package com.example.demo1228_2.controller;


import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.config.Tool;
import com.example.demo1228_2.entity.Order;
import com.example.demo1228_2.entity.Product;
import com.example.demo1228_2.entity.ProductRelatedList;
import com.example.demo1228_2.mapper.ProductRelatedListMapper;
import com.example.demo1228_2.mapper.TestMapper;
import com.example.demo1228_2.service.impl.ProductRelatedListServiceImpl;
import com.example.demo1228_2.service.impl.ProductServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yjz
 * @since 2024-05-05
 */
@Slf4j
@RestController
@RequestMapping("/product-related-list")
public class ProductRelatedListController {

    @Autowired
    ProductRelatedListMapper productRelatedListMapper;

    @Autowired
    ProductRelatedListServiceImpl productRelatedListService;

    @Autowired
    ProductServiceImpl productService;

    @Autowired
    TestMapper testMapper;

    /**
     * 查询订单（管理员）
     */
    @GetMapping("/selectpagebyadmin") // 分页查询 接收params //防空设默认
    public R<Page<ProductRelatedList>> FindPageProducts(@RequestParam Map<String, String> params, HttpSession session){
        // 使用LambdaQueryChainWrapper构建查询
        LambdaQueryChainWrapper<ProductRelatedList> query = new LambdaQueryChainWrapper<>(productRelatedListMapper);

        Page<ProductRelatedList> page = new Page<>(1,10);
        // 防空参数
        if(params.get("currentPage")!=null && params.get("PageSize")!=null)
            page = new Page<>(Long.parseLong(params.get("currentPage")),Long.parseLong(params.get("PageSize")));

        // 执行分页查询
        Page<ProductRelatedList> result = query.orderByDesc(ProductRelatedList::getCreate_time).page(page);

        // map返回筛选
        R<Page<ProductRelatedList>> response = R.success(result);
        response.setMap(params);

        log.info("{}",result.getRecords());
        log.info("{}",productRelatedListMapper.selectById("2"));
        return response;
    }

    /**
     * 添加关联 0.新增关系不能覆盖旧关系 1.本表赋值 2.每个商品的关联键赋值  回滚
     * @param data 1
     */
    @PostMapping("/addonebyadmin")
    public R<String> FindOneProduct(@RequestBody List<Product> data, HttpSession session){
        try{
            productRelatedListService.UpdateSelfAndEveryProductRelatedListId(data);
        }catch (Exception e){
            return R.error(e.getMessage());
        }
        return R.success("添加成功");
    }
}
