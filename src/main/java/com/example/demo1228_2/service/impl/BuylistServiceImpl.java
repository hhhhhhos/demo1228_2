package com.example.demo1228_2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.config.Tool;
import com.example.demo1228_2.dto.BuylistDto;
import com.example.demo1228_2.entity.Buylist;
import com.example.demo1228_2.entity.Product;
import com.example.demo1228_2.mapper.BuylistMapper;
import com.example.demo1228_2.mapper.ProductMapper;
import com.example.demo1228_2.service.IBuylistService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
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
public class BuylistServiceImpl extends ServiceImpl<BuylistMapper, Buylist> implements IBuylistService {

    @Autowired
    BuylistMapper buylistMapper;

    @Autowired
    ProductMapper productMapper;

    // 购物车批跟新
    @Transactional(rollbackFor = CustomException.class)
    public R<String> updateByList2(List<BuylistDto> buylistdtos, HttpSession session)throws Exception{
        // 获取订单合集 //构建Map (id,buylist)
        Map<Long,Buylist> buylistNumMap = new HashMap<>();
        // 获取订单id合集 // 去重
        Set<Long> buylistIds = new HashSet<>();

        for(BuylistDto buylistdto : buylistdtos){
            Long buylist_Id = buylistdto.getBuylist().getId();
            buylistNumMap.put(buylist_Id,buylistdto.getBuylist());
            buylistIds.add(buylist_Id);
        }

        // 根据ids 获取数据库订单(防伪造)
        List<Buylist> legal_buylists = new ArrayList<>();
        if(!buylistIds.isEmpty()){
            legal_buylists = this.listByIds(buylistIds);
        }else{
            return R.success("空购物车不进行更新批处理");
        }
        log.info("buylistIds:{}",buylistIds);
        // 对获取的数据库订单赋值num
        for(Buylist legal_buylist : legal_buylists){
            // 必须是本人订单 才能修改
            if(!legal_buylist.getUser_id().equals(Long.parseLong(session.getAttribute("IsLogin").toString())))
                throw new CustomException("不是本人订单");
            // 3.num不为负数
            if(legal_buylist.getProduct_num()<1)
                throw new CustomException("商品数量小于1");
            // 根据Map的id索引设置num
            legal_buylist.setProduct_num(buylistNumMap.get(legal_buylist.getId()).getProduct_num());
            // 根据Map的id索引设置is_selected
            legal_buylist.set_selected(buylistNumMap.get(legal_buylist.getId()).is_selected());
        }

        // null或没有不会设置空 而是略(但是为啥我的product_id被化成0)
        // int值不设置默认设置0 然后就被设置0
        log.info("购物车批处理{}",legal_buylists);
        if(this.updateBatchById(legal_buylists))return R.success("购物车更新批处理成功");
        else return R.error("购物车更新批处理失败");
    }

    // 购物车单个保存（重复则在原有加数量）
    @Transactional(rollbackFor = CustomException.class)
    public R<String> saveOneOrUpdateOne(Buylist buylist, HttpSession session)throws Exception{
        // 参数无商品号 返回错误
        if(buylist.getProduct_id()==null)
            throw new CustomException("商品号缺失");
        // 用session赋值User_id
        buylist.setUser_id(Long.parseLong(session.getAttribute("IsLogin").toString()));
        // 根据商品号查找商品
        Product product = productMapper.selectById(buylist.getProduct_id());

        // 商品不存在 不加购
        if(product==null)throw new CustomException("商品号不存在或已被删除");

        // 商品售罄 不加购
        if(product.getNum()==0)throw new CustomException("商品"+product.getName()+"卖完啦");


        // 购物车有无同商品id订单
        LambdaQueryWrapper<Buylist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Buylist::getProduct_id,buylist.getProduct_id())
                .eq(Buylist::getUser_id,buylist.getUser_id());
        Buylist db_buylist = buylistMapper.selectOne(queryWrapper);
        //LambdaUpdateWrapper<Buylist> u = new LambdaUpdateWrapper<>();
        // 如有
        if(db_buylist!=null) {
            // 数量加上原数量
            db_buylist.setProduct_num(db_buylist.getProduct_num() + buylist.getProduct_num());
            // 时间更新
            db_buylist.setCreate_time(buylist.getCreate_time());
            // 更新是否成功
            if (buylistMapper.updateById(db_buylist) != 0) {
                return R.success("购物车成功添加" + buylist.getProduct_num() + "件商品");
            } else {
                return R.error("添加失败，返回0条");
            }
        }
        // 向buylist表插入（如果前面没订单）
        int res=0;
        try{
            res = buylistMapper.insert(buylist);
        }catch(Exception e){
            throw new CustomException("异常："+e.getMessage());
        }
        if(res!=0)return R.success("购物车成功添加"+buylist.getProduct_num()+"件商品");
        else return R.error("添加失败，返回0条");

    }



}
