package com.example.demo1228_2.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.entity.*;
import com.example.demo1228_2.mapper.CommentMapper;
import com.example.demo1228_2.service.impl.UserAgentDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import static com.example.demo1228_2.interceptors.LoginInterceptor.handleRequestResponseData;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yjz
 * @since 2024-05-07
 */
@RestController
@Slf4j
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    CommentMapper commentMapper;

    /**
     * 返回当前产品评论分页 内含评论列表
     * @param params product_id 商品id
     * @return page
     */
    @GetMapping("/page")
    public R<Page> FindOneProduct(@RequestParam Map<String,String> params){
        /* 前端类型
        {
                  "info": "哈喽啊小伙伴们，测试，欢迎留言",
                  "id": 2110,
                  "datetime": "2023-10-28T03:49:36",
                  "love_num": 6,
                  "is_top": false,
                  "ip_location": "木卫二",
                  "name": "我是UP",
                  "ip": "6.6.6.6",
                  "saw_num": 0,
                  "sub_num": 0
              },
         */
        Long product_id;
        int currentPage;
        int PageSize;
        try {
            product_id = Long.parseLong(params.get("product_id"));
            currentPage = Integer.parseInt(params.get("currentPage"));
            PageSize = Integer.parseInt(params.get("PageSize"));
        }catch (Exception e){
            return R.error(e.getMessage());
        }

        Page page = new Page<>(currentPage, PageSize);

        // 该商品评论列表
        page = Db.lambdaQuery(Comment.class)
                .eq(Comment::getProduct_id,product_id)
                .orderByDesc(Comment::getCreate_time)
                .page(page);  // 使用page方法代替list方法以实现分页

        List<Comment> commentList = page.getRecords();
        //log.info("commentList!!:"+commentList);
        if(commentList.isEmpty())return R.success(page.setRecords(new ArrayList<>()));

        // 构建 评论的用户id列表
        List<Long> userIdList = commentList.stream()
                .map(Comment::getUser_id)
                .distinct()  // 去重，确保每个用户ID只查询一次
                .toList();
        //log.info("userIdList!!:"+userIdList);

        // 返回<Id,用户>（查最新用户信息）
        Map<Long,User> userMap = Db.lambdaQuery(User.class)
                .in(User::getId,userIdList)
                .list()
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        //log.info("userMap!!:"+userMap);

        // 获取所有相关订单
        List<Order2> orders = Db.lambdaQuery(Order2.class)
                .eq(Order2::getProduct_id, product_id)
                .in(Order2::getUser_id, userIdList)
                .list();

        // 聚合每个用户的订单数据
        Map<Long, Map<String, Object>> userOrderStatsMap = orders.stream()
                .collect(Collectors.groupingBy(
                        Order2::getUser_id,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    int totalPurchaseNum = list.stream()
                                            .mapToInt(Order2::getPurchase_num)
                                            .sum();
                                    BigDecimal totalPurchasePrice = list.stream()
                                            .map(Order2::getPurchase_total_price)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return Map.of(
                                            "totalPurchaseNum", totalPurchaseNum,
                                            "totalPurchasePrice", totalPurchasePrice
                                    );
                                }
                        )
                ));

        // 拿评分
        Map<Long,BigDecimal> userRateMap = Db.lambdaQuery(ProductRate.class)
                .eq(ProductRate::getProduct_id,product_id)
                .list()
                .stream()
                .collect(Collectors.toMap(ProductRate::getUser_id, ProductRate::getRate));


        List<Map<String,Object>> re = commentList.stream()
                .map(entry -> {
                    Map<String,Object> new_entry = new HashMap<>();
                    new_entry.put("info",entry.getInfo());
                    new_entry.put("id",entry.getId().toString());
                    new_entry.put("datetime",entry.getCreate_time());
                    new_entry.put("love_num",entry.getLove_list());
                    new_entry.put("is_top",entry.getIs_top());
                    new_entry.put("ip_location",entry.getIp_location());
                    new_entry.put("name",userMap.get(entry.getUser_id())==null?"用户不存在":userMap.get(entry.getUser_id()).getName());
                    new_entry.put("ip",entry.getIp());
                    new_entry.put("saw_num",entry.getSaw_num());
                    new_entry.put("sub_num",entry.getSub_num());
                    // 一些我想加的参数
                    new_entry.put("wechat_nickname",userMap.get(entry.getUser_id())==null?null:userMap.get(entry.getUser_id()).getWechat_nickname());
                    new_entry.put("wechat_headimgurl",userMap.get(entry.getUser_id())==null?null:userMap.get(entry.getUser_id()).getWechat_headimgurl());
                    new_entry.put("totalPurchaseNum",userOrderStatsMap.get(entry.getUser_id())==null?null:userOrderStatsMap.get(entry.getUser_id()).get("totalPurchaseNum").toString());
                    new_entry.put("totalPurchasePrice",userOrderStatsMap.get(entry.getUser_id())==null?null:userOrderStatsMap.get(entry.getUser_id()).get("totalPurchasePrice").toString());
                    new_entry.put("rate",userRateMap.get(entry.getUser_id())==null?null:Double.parseDouble(userRateMap.get(entry.getUser_id()).toString()));
                    return new_entry;
                }).collect(Collectors.toList());

        return R.success(page.setRecords(re));
    }

    @Autowired
    UserAgentDetailsServiceImpl userAgentDetailsService;

    /**
     * 新增一条评论
     * @param params 1comment_info 2product_id 3father_comm_id（可选）
     * @param session 1
     * @param request 1
     * @param response 1
     * @return 1
     */
    @PostMapping("/addone") // 加一个
    public R<String> FindOneProduct(@RequestBody Map<String,Object> params, HttpSession session, HttpServletRequest request, HttpServletResponse response){
        log.info("{}",params);
        String comment_info = (String) params.get("comment_info");
        log.info("{}",comment_info);
        String product_id_s = (String)params.get("product_id");
        if(product_id_s==null || comment_info==null)
            return R.error("缺少参数");
        Long product_id = Long.parseLong(product_id_s);

        CompletableFuture<UserAgentDetails> future = userAgentDetailsService.saveByAsync(handleRequestResponseData(request,response),false);
        future.thenAccept(userAgentDetails -> {
            // 这里处理返回的 userAgentDetails
            log.info("接收到的 UserAgentDetails: " + userAgentDetails);
            Comment comment = new Comment();
            comment.setProduct_id(product_id);
            Long father_comm_id = (Long) params.get("father_comm_id");
            // 如果新增的是子评论
            if(father_comm_id!=null){
                Comment comment_father = commentMapper.selectById(father_comm_id);
                if(comment_father == null){log.info("父评论不存在，结束");return ;}
                // 父评论的子评论数+1
                comment_father.setSub_num(comment_father.getSub_num()+1);
                // 子评论设置父评论id
                comment.setFather_comm_id(father_comm_id);
            }
            comment.setUser_id(Long.parseLong(session.getAttribute("IsLogin").toString()));
            comment.setInfo(comment_info);
            comment.setIp_location(userAgentDetails.getCity());
            comment.setIp(userAgentDetails.getRealIp());
            if(commentMapper.insert(comment)==1)
                log.info("评论插入成功");
            else
                log.info("评论插入失败");
        }).exceptionally(ex -> {
            // 处理异步操作中的异常
            log.info("处理异常: " + ex.getMessage());
            return null;
        });
        log.info("结束咯");
        return R.success("你的评论将在审核后展示");
    }

}
