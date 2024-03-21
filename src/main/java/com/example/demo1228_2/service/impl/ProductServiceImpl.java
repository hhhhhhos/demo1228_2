package com.example.demo1228_2.service.impl;

import com.example.demo1228_2.Vo.Alipay;
import com.example.demo1228_2.Vo.ProductUpdateByListVo;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.entity.Buylist;
import com.example.demo1228_2.entity.Order;
import com.example.demo1228_2.entity.Product;
import com.example.demo1228_2.entity.Test;
import com.example.demo1228_2.mapper.OrderMapper;
import com.example.demo1228_2.mapper.ProductMapper;
import com.example.demo1228_2.mapper.TestMapper;
import com.example.demo1228_2.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
}
