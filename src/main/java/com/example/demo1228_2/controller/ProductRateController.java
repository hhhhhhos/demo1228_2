package com.example.demo1228_2.controller;


import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.config.Tool;
import com.example.demo1228_2.entity.Buylist;
import com.example.demo1228_2.entity.Product;
import com.example.demo1228_2.entity.ProductRate;
import com.example.demo1228_2.mapper.ProductMapper;
import com.example.demo1228_2.mapper.ProductRateMapper;
import com.example.demo1228_2.service.impl.ProductServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yjz
 * @since 2024-04-22
 */
@RestController
@RequestMapping("/product-rate")
public class ProductRateController {

    @Autowired
    ProductRateMapper productRateMapper;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    ProductServiceImpl productService;

    /**
     * 更新或插入评分
     * @param productRate 1
     * @param session 1
     * @return 1
     */
    @PostMapping("/add-or-update")
    public R<String> AddOrUpdate(@RequestBody ProductRate productRate, HttpSession session){
        try{
            // 判断0.5-5
            BigDecimal rate = productRate.getRate();
            if(rate == null || rate.compareTo(BigDecimal.valueOf(0.5))<0 || rate.compareTo(BigDecimal.valueOf(5))>0)
                throw new CustomException("rate值错误");
            if(productRate.getUser_id() == null)
                productRate.setUser_id(Tool.getUserSessionId(session));
            if(!productRate.getUser_id().equals(Tool.getUserSessionId(session)))
                throw new CustomException("id非本人");
            if(productMapper.selectById(productRate.getProduct_id())==null)
                throw new CustomException("商品不存在");


            // 有则更改 无则增加 (他对它的评分)
            ProductRate db_productRate = Db.lambdaQuery(ProductRate.class)
                    .eq(ProductRate::getUser_id, Tool.getUserSessionId(session))
                    .eq(ProductRate::getProduct_id,productRate.getProduct_id()).one();
            if(db_productRate!=null){
                // 更改
                db_productRate.setRate(rate);
                db_productRate.setCreate_time(LocalDateTime.now());
                if(productRateMapper.updateById(db_productRate)==0)
                    throw new CustomException("数据库更新你的评分失败");
                // 每点赞一次就重新评分
                productService.RecaculateProductRateById(productRate.getProduct_id());
                return R.success("你的评分更新成功");
            }else{
                // 增加
                if(productRateMapper.insert(productRate)==0)
                    throw new CustomException("数据库插入你的评分失败");
                // 每点赞一次就重新评分
                productService.RecaculateProductRateById(productRate.getProduct_id());
                return R.success("你的评分插入成功");
            }


        }catch (Exception e){
            return R.error("错误:"+e.getMessage());
        }

    }


}
