package com.example.demo1228_2.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo1228_2.config.CustomException;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.dto.BuylistDto;
import com.example.demo1228_2.dto.OrderDto;
import com.example.demo1228_2.entity.Buylist;
import com.example.demo1228_2.entity.Order;
import com.example.demo1228_2.entity.Product;
import com.example.demo1228_2.entity.User;
import com.example.demo1228_2.mapper.BuylistMapper;
import com.example.demo1228_2.mapper.OrderMapper;
import com.example.demo1228_2.mapper.ProductMapper;
import com.example.demo1228_2.service.IBuylistService;
import com.example.demo1228_2.service.IOrderService;
import com.example.demo1228_2.service.IProductService;
import com.example.demo1228_2.service.impl.OrderServiceImpl;
import com.example.demo1228_2.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yjz
 * @since 2024-02-18
 */
@RestController
@RequestMapping("/order")
@Slf4j // 自动生成log对象
public class OrderController {

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    UserServiceImpl userService;

    @GetMapping("/page") // 分页查询 接收params //防空设默认
    public R<Page<Order>> FindPage(@RequestParam(defaultValue = "-1")int currentPage,
                               @RequestParam(defaultValue = "-1")int PageSize,
                            @RequestParam(defaultValue = "false")boolean Is_backdesk, HttpSession session) {
        try {
            // 空参数抛异常
            if (currentPage == -1 || PageSize == -1) throw new CustomException("分页查询参数为空");

            // 分页查询
            Page<Order> page = new Page<>(currentPage, PageSize);
            // 创建LambdaQueryWrapper实例
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            // 根据时间从高到低排序
            queryWrapper.orderByDesc(Order::getCreate_time);
            // 根据权限查询
            if(!Is_backdesk)
                queryWrapper.eq(Order::getUser_id, session.getAttribute("IsLogin").toString());
            else{
                String role = session.getAttribute("Role").toString();
                if(!role.equals("admin") && !role.equals("visitor"))
                    throw new CustomException("后台查询，但角色错误");
            }



            // 执行查询
            Page<Order> res = orderService.page(page, queryWrapper);

            log.info("order分页查询成功");
            log.info("{}",res.getRecords());
            return R.success(res);

        } catch (Exception e) {
            log.info("order分页查询失败{}", e.getMessage());
            return R.error("e.getMessage()");
        }

    }

    @DeleteMapping("/deletebyid") // 根据id删一个
    public R<String> DeleteById(@RequestParam(defaultValue = "-1")Long D_id,
                                @RequestParam(defaultValue = "false")Boolean backBuyList,HttpSession session){
        try{
            // 查要删的是不是属于客户
            Order order = orderService.getById(D_id);
            if(order==null)
                return R.error("订单号对应的订单不存在");
            // 比较客户号和session id
            if(!order.getUser_id().equals(Long.parseLong(session.getAttribute("IsLogin").toString())))
                return R.error("不能删除非自己的订单");


            if(orderService.removeByIdAndAddProductNumIfUnBuy(D_id,backBuyList,session))return R.success("删除成功");
            else return R.error("删除失败");

        }catch(Exception e){
            return R.error("异常："+e.getMessage());
        }
    }

    @PostMapping("/payonmoney") //余额支付
    public R<String> PayOnMoney(@RequestParam Map<String, String> params, HttpSession session){
        Long Id = Long.parseLong(session.getAttribute("IsLogin").toString());
        Long order_Id = Long.parseLong(params.get("order_id"));
        try{
            Order order = orderService.getById(order_Id);
            User user = userService.getById(Id);

            if(order == null)
                throw new CustomException("订单不存在");

            if(!order.getUser_id().equals(Id))
                throw new CustomException("订单非本人，不可支付");

            if(!order.getStatus().equals("未支付"))
                throw new CustomException("订单状态不是未支付，不能支付");

            if (user.getMoney().compareTo(order.getTotalMoney()) < 0) {
                // 用户的钱少于订单的总金额
                throw new CustomException("余额不足");
            }

            // 余额充足就扣钱
            user.setMoney(user.getMoney().subtract(order.getTotalMoney()).setScale(2, RoundingMode.HALF_UP));
            // 订单是未支付就支付 该状态
            order.setStatus("已支付");

            // 事物一起提交
            orderService.UpdateUserMoneyAndOrderState(user ,order);
            // 不抛异常就成功了

            return R.success("余额支付成功");

        }catch (Exception e){
            return R.error("异常："+e.getMessage());
        }
    }


    /**
     * 删除订单（管理员）
     * @param orderList 要删除的列表
     */
    @PostMapping("/deletelistbyadmin") // 删一堆商品
    public R<String> DLO(@RequestBody List<Order> orderList,HttpSession session){
        try{
            // 验证权限
            if(!session.getAttribute("Role").toString().equals("admin"))
                throw new CustomException("不是管理员，禁止操作");

            // 防空
            if(orderList==null)
                throw new CustomException("参数不能为空");

            // 提Id
            Set<Long> order_Ids = new HashSet<>();
            for(Order order:orderList)
                order_Ids.add(order.getId());
            List<Long> order_Ids_list = new ArrayList<>(order_Ids);

            // 删订单回架
            for(Long id:order_Ids_list){
                orderService.removeByIdAndAddProductNumIfUnBuy(id,false,session);
            }

            return R.success("");

        }catch(Exception e){
            return R.error(e.getMessage());
        }
    }

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    ObjectMapper objectMapper;
    /**
     * 查询订单（管理员）
     */
    @GetMapping("/selectpagebyadmin") // 分页查询 接收params //防空设默认
    public R<Page> FindPageProducts(@RequestParam Map<String, String> params, HttpSession session){
        // 使用LambdaQueryChainWrapper构建查询
        LambdaQueryChainWrapper<Order> query = new LambdaQueryChainWrapper<>(orderMapper);

        // 根据条件动态添加查询条件
        if (params.get("user_id") != null) {
            query.like(Order::getUser_id, params.get("user_id"));
        }
        // 单独处理startDate，如果存在则查询大于等于这个日期的记录
        if (params.get("startDate") != null) {
            query.ge(Order::getCreate_time, params.get("startDate")); // ge是“greater than or equal to”的缩写
        }
        // 单独处理endDate，如果存在则查询小于等于这个日期的记录
        if (params.get("endDate") != null) {
            query.le(Order::getCreate_time, params.get("endDate")); // le是“less than or equal to”的缩写
        }
        if (params.get("id") != null) {
            query.like(Order::getId, params.get("id"));
        }
        if (params.get("status") != null) {
            query.like(Order::getStatus, params.get("status"));
        }

        Page<Order> page = new Page<>(1,10);
        // 防空参数
        if(params.get("currentPage")!=null && params.get("PageSize")!=null)
            page = new Page<>(Long.parseLong(params.get("currentPage")),Long.parseLong(params.get("PageSize")));
        // 执行分页查询
        Page result = query.orderByDesc(Order::getCreate_time).page(page);

        List<Order> rec = result.getRecords();
        // 加点数据
        result.setRecords(rec.stream().map(order->{
            Map<String,Object> newEntry = objectMapper.convertValue(order,Map.class);
            Map<String,Object> user_map = objectMapper.convertValue(userService.getById(order.getUser_id()),Map.class);
            newEntry.put("user_info",user_map);
            return newEntry;
        }).collect(Collectors.toList()));

        // map返回筛选
        R<Page> response = R.success(result);
        response.setMap(params);

        return response;
    }
}
