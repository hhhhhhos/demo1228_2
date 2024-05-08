package com.example.demo1228_2.service.impl;

import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.entity.Product;
import com.example.demo1228_2.entity.ProductRelatedList;
import com.example.demo1228_2.mapper.ProductRelatedListMapper;
import com.example.demo1228_2.service.IProductRelatedListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yjz
 * @since 2024-05-05
 */
@Service
public class ProductRelatedListServiceImpl extends ServiceImpl<ProductRelatedListMapper, ProductRelatedList> implements IProductRelatedListService {

    @Autowired
    ProductRelatedListMapper productRelatedListMapper;

    @Autowired
    ProductServiceImpl productService;

    /**
     * 添加关联  -1.关系应该大于1  0.新增关系不能覆盖旧关系 1.本表赋值 2.每个商品的关联键赋值  回滚
     * @param data 1
     * @throws Exception 1
     */
    @Transactional(rollbackFor = CustomException.class)
    public void UpdateSelfAndEveryProductRelatedListId(List<Product> data)throws Exception{
        //1
        ProductRelatedList productRelatedList = new ProductRelatedList();
        Set<Long> PIds = new HashSet<>();
        data.forEach(item->{
            PIds.add(item.getId());
        });
        //-1
        if(PIds.size()<2)throw new CustomException("新建的关联数量要大于1");
        List<Long> PIdsL = new ArrayList<>(PIds);
        productRelatedList.setProduct_id_list(PIdsL);
        if(productRelatedListMapper.insert(productRelatedList)!=1)
            throw new CustomException("productRelatedList数据库插入失败");
        //2
        List<Product> db_data = productService.listByIds(PIds);
        for(Product item :db_data){
            //0
            if(item.getProduct_related_list_id()!=null)
                throw new CustomException(item.getName()+"新增关系不能覆盖旧关系，请先在控制面板删除旧关系");
            item.setProduct_related_list_id(productRelatedList.getId());
        }
        //2
        if(!productService.updateBatchById(db_data)){
            throw new CustomException("product数据库插入失败，并发？建议重试");
        }

    }
}
