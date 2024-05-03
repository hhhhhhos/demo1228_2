package com.example.demo1228_2.service.impl;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.Vo.Alipay;
import com.example.demo1228_2.Vo.ProductUpdateByListVo;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.entity.*;
import com.example.demo1228_2.mapper.OrderMapper;
import com.example.demo1228_2.mapper.ProductMapper;
import com.example.demo1228_2.mapper.TestMapper;
import com.example.demo1228_2.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yjz
 * @since 2024-01-15
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    @Autowired
    ProductMapper productMapper;

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    BuylistServiceImpl buylistService;

    // 回滚 //一次搞完减库 建单 删车 //返回前端要的Vo数据
    @Transactional(rollbackFor = CustomException.class)
    public Alipay updateByList(ProductUpdateByListVo map) throws Exception{
        List<Product> productList = map.getProductList();
        Order order = map.getOrder();
        Set<Long> BuylistIds = map.getBuylistIds();
        Alipay alipay = new Alipay();

        for(Product product : productList){
            if(productMapper.updateById(product)==0)
                throw new CustomException("更新失败，可能是版本号冲突");
        }


        if(!orderService.save(order))
            throw new CustomException("更新成功，但订单生成失败，回滚");

        if(!buylistService.removeByIds(new ArrayList<>(BuylistIds)))
            throw new CustomException("更新成功，但购物车删除失败，回滚");

        alipay.setId(order.getId());
        alipay.setNum(order.getTotalNum());
        alipay.setName(order.getInfo().get(0).getProduct().getName());
        alipay.setMoney(order.getTotalMoney());

        return alipay;
    }

    /**
     * 直接购买一个商品 减库 建单
     * @param params 泛参数，不用麻烦定义vo dto
     * @return Alipay
     * @throws Exception 异常
     */
    @Transactional(rollbackFor = CustomException.class)
    public Alipay updateByOne(Map<String,Object> params) throws Exception{
        Alipay alipay = new Alipay();
        /*
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            log.info("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
        */
        Order order = (Order)params.get("order");
        Product db_product = (Product)params.get("db_product");


        //减库
        if(db_product.getNum() - order.getTotalNum() < 0)
            throw new CustomException("库存不足");

        db_product.setNum(db_product.getNum() - order.getTotalNum());
        // 加销量
        db_product.setSold_num(order.getTotalNum());

        if(productMapper.updateById(db_product)==0)
            throw new CustomException("版本号冲突");

        if(!orderService.save(order))
            throw new CustomException("更新成功，但订单生成失败，回滚");


        alipay.setId(order.getId());
        alipay.setNum(order.getTotalNum());
        alipay.setName(order.getInfo().get(0).getProduct().getName());
        alipay.setMoney(order.getTotalMoney());

        return alipay;
    }

    /**
     * (有人评分之后)重新计算商品评分 和 评分人数
     * @return 是否成功
     */
    public Boolean RecaculateProductRateById(Long id){
        // 拿商品评分(平均)
        List<ProductRate> productRateList = Db.lambdaQuery(ProductRate.class)
                .eq(ProductRate::getProduct_id,id).list();
        Optional<BigDecimal> sum = productRateList.stream()
                .map(ProductRate::getRate) // 提取rate值
                .reduce(BigDecimal::add); // 使用BigDecimal的add方法累加
        // 计算平均值
        BigDecimal averageRate = BigDecimal.ZERO;
        if (!productRateList.isEmpty() && sum.isPresent()) {
            // 将总和除以列表的大小
            averageRate = sum.get().divide(BigDecimal.valueOf(productRateList.size()), 2, BigDecimal.ROUND_HALF_UP);
        }
        // 更新Rate 和 Rate人数
        Product product = this.getById(id);
        product.setRate_num(productRateList.size());
        product.setRate(averageRate);
        return this.updateById(product);
    }
}
