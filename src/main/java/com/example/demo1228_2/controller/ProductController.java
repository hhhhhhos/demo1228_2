package com.example.demo1228_2.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.entity.Product;
import com.example.demo1228_2.entity.User;
import com.example.demo1228_2.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/product")
@Slf4j // 自动生成log对象
public class ProductController {
    @Autowired
    ProductMapper productmapper;

    @GetMapping("/page") // 分页查询 接收params //防空设默认
    public R<Page<Product>> FindPageProduct(@RequestParam(defaultValue = "-1")int currentPage,
                                            @RequestParam(defaultValue = "-1")int PageSize,
                                            @RequestParam(required = false) String FName,
                                            @RequestParam(required = false) String FType){
        log.info("!!:{}",FType);
        try {
            // 空参数抛异常
            if(currentPage == -1 || PageSize == -1 )throw new CustomException("分页查询参数为空");
            // 分页查询
            Page<Product> page = new Page<>(currentPage, PageSize);

            // 创建LambdaQueryWrapper实例筛选器
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            // 根据id从低到高排序
            queryWrapper.orderByAsc(Product::getId);
            // FName不为空 筛选名字
            if(FName != null)queryWrapper.like(Product::getName,FName);
            // FType不为空 筛选种类
            if(FType != null)queryWrapper.eq(Product::getType,FType);

            // 执行查询
            Page<Product> res = productmapper.selectPage(page, queryWrapper);

            /*
            //控制台打印json
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(res);
            System.out.println(json);
            //
            */
            log.info("分页查询成功");
            return R.success(res);
        }catch(Exception e){
            log.info("分页查询失败：{}",e.getMessage());
            return R.error(e.getMessage());
        }


    }

    @GetMapping("/getone") // 查一个商品
    public R<Product> FindOneProduct(@RequestParam(defaultValue = "-1")int id){
        Product product;
        if(id ==-1)return R.error("id为空");
        try{
            product = productmapper.selectById(id);
        }catch(Exception e){
            log.info("查一个商品失败：{}",e.getMessage());
            return R.error(e.getMessage());
        }
        return R.success(product);
    }
}
