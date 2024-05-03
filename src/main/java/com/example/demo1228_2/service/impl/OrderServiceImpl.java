package com.example.demo1228_2.service.impl;

import com.example.demo1228_2.Vo.Alipay;
import com.example.demo1228_2.Vo.ProductUpdateByListVo;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.dto.BuylistDto;
import com.example.demo1228_2.entity.Buylist;
import com.example.demo1228_2.entity.Order;
import com.example.demo1228_2.entity.Product;
import com.example.demo1228_2.entity.User;
import com.example.demo1228_2.mapper.BuylistMapper;
import com.example.demo1228_2.mapper.OrderMapper;
import com.example.demo1228_2.mapper.ProductMapper;
import com.example.demo1228_2.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yjz
 * @since 2024-02-18
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    BuylistServiceImpl buylistService;

    // 回滚 //一次搞完减order // 如果是未支付的 加product数
    /**
     * 删订单，回架（未支付的话），回车
     * @param Id 要删除的订单ID
     * @param backBuyList 是否回车
     * @return 成功返回true 不成功抛异常
     */
    @Transactional(rollbackFor = CustomException.class)
    public boolean removeByIdAndAddProductNumIfUnBuy(Long Id, Boolean backBuyList, HttpSession session)throws Exception{
        Order order = orderMapper.selectById(Id);

        List<BuylistDto> buylistDtosList = order.getInfo();//List反序列化失效//Gpt帮助下成功
        List<Buylist> buylistList = new ArrayList<>();

        // 未支付的话 商品回架子//回车
        if(order.getStatus().equals("未支付")) {
            for (BuylistDto buylistDto : buylistDtosList) {
                Long product_Id = buylistDto.getBuylist().getProduct_id();
                int product_num = buylistDto.getBuylist().getProduct_num();
                Product product = productMapper.selectById(product_Id);
                // 数量加本身
                product.setNum(product.getNum() + product_num);
                // 销量减去
                product.setSold_num(product.getSold_num() - product_num);
                if(productMapper.updateById(product)==0)
                    throw new CustomException("商品"+product.getName()+"回架失败,请重试");
                buylistList.add(buylistDto.getBuylist());
            }
            // 是否回购物车（默认不回）
            if(backBuyList){
                for(Buylist buylist:buylistList){
                    if(buylistService.saveOneOrUpdateOne(buylist,session).getCode()==0)
                        throw new Exception("购物车回车失败，回滚");
                }

            }
        }
        // 已支付 上面的都不做

        // 只删订单就行
        if(orderMapper.deleteById(Id)==0)throw new CustomException("订单"+Id+"删除失败");
        else{
            log.info("订单{}删除成功",Id);
            return true;
        }
    }

    // 余额支付 // 同时更新用户余额 和 订单状态
    @Transactional(rollbackFor = CustomException.class)
    public void UpdateUserMoneyAndOrderState(User user, Order order) throws Exception {
        if(!userService.updateById(user))
            throw new CustomException("余额扣减失败");

        // 扣钱成功就更新订单状态为 已支付
        if(!this.updateById(order))
            throw new CustomException("订单状态更新失败");
    }



    // 根据支付宝传回的信息 确定修改订单支付状态 //订单信息和金额对不上就抛异常
    public void updateByAliPay(Map<String, String> params)throws Exception{
        Long id = Long.parseLong(params.get("out_trade_no"));
        BigDecimal amount = new BigDecimal(params.get("buyer_pay_amount")).setScale(2, RoundingMode.HALF_UP);
        String tradeStatus = params.get("trade_status");

        Order order = this.getById(id);

        if(order==null) throw new CustomException("查询不到订单，不允许支付");

        if(order.getStatus().equals("已支付")) throw new CustomException("订单已支付，不能再次支付");

        if(!order.getStatus().equals("未支付")) throw new CustomException("订单状态异常，不是未支付，不允许支付");

        if(order.getTotalMoney().compareTo(amount)!=0) throw new CustomException("支付金额异常，不等于订单金额");

        if(!tradeStatus.equals("TRADE_SUCCESS")) throw new CustomException("支付结果不是TRADE_SUCCESS，而是："+tradeStatus);

        // 走到这说明没啥毛病 改订单状态吧
        order.setStatus("已支付");
        if(!this.updateById(order))throw new CustomException("数据库订单更新支付状态失败");

        log.info("修改订单：{}状态为已支付成功",id);
    }
}
