package com.example.demo1228_2.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.demo1228_2.config.R;
import com.example.demo1228_2.config.Tool;
import com.example.demo1228_2.dto.CommentOrderUserRateDto;
import com.example.demo1228_2.entity.*;
import com.example.demo1228_2.mapper.CommentMapper;
import com.example.demo1228_2.mapper.UserMapper;
import com.example.demo1228_2.service.impl.CommentServiceImpl;
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

    @Autowired
    CommentServiceImpl commentService;

    /**
     * 返回当前产品评论分页 内含评论列表(已废弃)
     * @param params product_id 商品id
     * @return page
     */

    @GetMapping("/page")
    public R<Page> FindOneProduct(@RequestParam Map<String,String> params){

        Long product_id;
        int currentPage;
        int PageSize;
        String value2;
        try {
            product_id = Long.parseLong(params.get("product_id"));
            currentPage = Integer.parseInt(params.get("currentPage"));
            PageSize = Integer.parseInt(params.get("PageSize"));
            value2 = params.get("value2");
        }catch (Exception e){
            return R.error(e.getMessage());
        }

        Page page = new Page<>(currentPage, PageSize);
        // 使用LambdaQueryChainWrapper构建查询
        LambdaQueryChainWrapper<Comment> query = new LambdaQueryChainWrapper<>(commentMapper);

        page = query
                .eq(Comment::getProduct_id,product_id)
                .page(page);



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
                    new_entry.put("love_list",entry.getLove_list());
                    //new_entry.put("love_num",entry.getLove_list_num());
                    new_entry.put("is_top",entry.getIs_top());
                    new_entry.put("ip_location",entry.getIp_location());
                    new_entry.put("name",userMap.get(entry.getUser_id())==null?"用户不存在":userMap.get(entry.getUser_id()).getName());
                    new_entry.put("ip",entry.getIp());
                    new_entry.put("saw_num",entry.getSaw_num());
                    //new_entry.put("sub_num",entry.getSub_num());
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


    /**
     * 返回当前产品评论分页 内含评论列表
     * @param params product_id 商品id
     * @return page
     */

    @GetMapping("/page2")
    public R<IPage> FindOneProduct2(@RequestParam Map<String,String> params,HttpSession session){

        Long product_id;
        int currentPage;
        int PageSize;
        String value2;
        Long father_comment_id;
        try {
            product_id = Long.parseLong(params.get("product_id"));
            currentPage = Integer.parseInt(params.get("currentPage"));
            PageSize = Integer.parseInt(params.get("PageSize"));
            value2 = params.get("value2");
            father_comment_id = Long.parseLong(params.get("father_comment_id"));
        }catch (Exception e){
            return R.error(e.getMessage());
        }

        Page page = new Page<>(currentPage, PageSize);

        // 用Mapper@select 连表查询 返回DTO
        IPage commentListPage;
        // 主评论分页
        if(father_comment_id==0)
            commentListPage = PageQueryValue2(value2,page,product_id);
        // 子评论分页
        else
            commentListPage = commentMapper.selectByProductIdLeftJoinOrderByRateSub(page,product_id,father_comment_id);
        List<CommentOrderUserRateDto> commentList = commentListPage.getRecords();

        List<Map<String,Object>> re = commentList.stream()
                .map(entry -> {
                    Map<String,Object> new_entry = new HashMap<>();
                    // 回复某人的话 加前缀
                    if(entry.getReplay_to_user_id()==0)
                        new_entry.put("info",entry.getInfo());
                    else {
                        User user = userMapper.selectById(entry.getReplay_to_user_id());
                        String before_s = "回复 @";
                        if(user==null)before_s+="用户不存在";
                        else if(user.getWechat_nickname()!=null)before_s+=user.getWechat_nickname();
                        else before_s+=user.getName();
                        before_s+="：";
                        new_entry.put("info", before_s+entry.getInfo());
                    }

                    new_entry.put("id",entry.getId().toString());
                    new_entry.put("user_id",entry.getUser_id().toString());
                    new_entry.put("datetime",entry.getCreate_time());
                    List<Long> loveList = commentMapper.selectById(entry.getId()).getLove_list();
                    new_entry.put("love_list",loveList); // @select无法正确映射json
                    new_entry.put("is_loved",loveList.contains(Tool.getUserSessionId(session)));
                    new_entry.put("love_num",loveList.size()); // 数据库不用存了
                    new_entry.put("is_top",entry.getIs_top());
                    new_entry.put("ip_location",entry.getIp_location());
                    new_entry.put("name",entry.getName()==null?"用户不存在":entry.getName());
                    new_entry.put("ip",entry.getIp());
                    new_entry.put("saw_num",entry.getSaw_num());
                    new_entry.put("sub_num",Db.lambdaQuery(Comment.class) // 数据库不用存了
                            .eq(Comment::getFather_comm_id,entry.getId())
                            .count());
                    // 一些我想加的参数
                    new_entry.put("wechat_nickname",entry.getWechat_nickname());
                    new_entry.put("wechat_headimgurl",entry.getWechat_headimgurl());
                    new_entry.put("totalPurchaseNum",entry.getTotal_quantity());
                    new_entry.put("totalPurchasePrice",entry.getTotal_spent());
                    new_entry.put("rate",entry.getRate());
                    return new_entry;
                }).collect(Collectors.toList());

        //log.info("re:"+re);
        return R.success(commentListPage.setRecords(re));
    }




    @Autowired
    UserAgentDetailsServiceImpl userAgentDetailsService;

    @Autowired
    UserMapper userMapper;

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
        String comment_info;
        Long product_id;
        Long father_comment_id;
        Long replay_to_user_id;
        try{
            comment_info = params.get("comment_info").toString();
            product_id = Long.parseLong(params.get("product_id").toString());
            father_comment_id = Long.parseLong(params.get("father_comment_id").toString()); // 父默认0
            replay_to_user_id = Long.parseLong(params.get("replay_to_user_id").toString()); // 父默认0
        }catch (Exception e){
            return R.error("参数异常："+e.getMessage());
        }


        if(comment_info.length()>200)
            return R.error("评论过长");
        if(comment_info.length()==0)
            return R.error("评论不能为空");


        CompletableFuture<UserAgentDetails> future = userAgentDetailsService.saveByAsync(handleRequestResponseData(request,response),false);
        future.thenAccept(userAgentDetails -> {
            // 这里处理返回的 userAgentDetails
            log.info("接收到的 UserAgentDetails: " + userAgentDetails);
            Comment comment = new Comment();
            comment.setProduct_id(product_id);
            comment.setFather_comm_id(father_comment_id);
            comment.setUser_id(Tool.getUserSessionId(session));
            comment.setReplay_to_user_id(replay_to_user_id);
            comment.setInfo(comment_info);
            comment.setIp_location(userAgentDetails.getCity());
            comment.setIp(userAgentDetails.getRealIp());
            comment.setLove_list(new ArrayList<>());

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

    /**
     * 删一个评论
     * @param session 1
     * @return 1
     */
    @DeleteMapping("/deleteone")
    public R<String> FindOneProduct(@RequestParam Long id,HttpSession session){
        if(id==null||id==0)
            return R.error("参数缺失");

        Comment comment = commentMapper.selectById(id);
        if(comment==null||!comment.getUser_id().equals(Tool.getUserSessionId(session)))
            return R.error("不能删除非本人评论");

        if(commentMapper.deleteById(id)!=1)
            return R.error("数据库删除失败");
        else
            return R.success("删除成功");
    }

    /**
     * 新增一个赞
     * @param params comment_id
     * @param session 1
     * @return 1
     */
    @PostMapping("/addone/zan") // 加一个
    public R<String> FindOneProduct(@RequestBody Map<String,Object> params, HttpSession session){
        Long comment_id;
        Long user_id;
        try{
            user_id = Long.parseLong(session.getAttribute("IsLogin").toString());
            comment_id = Long.parseLong(params.get("comment_id").toString());
        }catch (Exception e){
            return R.error(e.getMessage());
        }
        Comment comment = commentMapper.selectById(comment_id);
        List<Long> loveList = comment.getLove_list();
        // 点赞过了 取消点赞
        if(loveList.contains(user_id)){
            loveList.remove(user_id);
            comment.setLove_list(loveList);
            //comment.setLove_list_num(comment.getLove_list_num()-1);
            if(commentMapper.updateById(comment)!=1)
                return R.error("取消点赞失败，建议重试");
            return R.success("取消点赞成功");
        // 没点赞过 点赞
        }else{
            loveList.add(user_id);
            comment.setLove_list(loveList);
            //comment.setLove_list_num(comment.getLove_list_num()+1);
            if(commentMapper.updateById(comment)!=1)
                return R.error("点赞失败，建议重试");
            return R.success("点赞成功");
        }

    }

    /**
     * 根据value2 分类排序返回
     * @param value2 a点赞 b时间 c土豪 d高评 e低评
     * @param page 1
     * @param product_id 1
     * @return List
     */
    private IPage PageQueryValue2(String value2, Page page, Long product_id) {
        switch(value2){
            case "a": // 点赞排序
                return commentMapper.selectByProductIdLeftJoinOrderByLike(page,product_id);
            case "b": // 时间排序
                return commentMapper.selectByProductIdLeftJoinOrderByTime(page,product_id);
            case "c": // 土豪排序
                return commentMapper.selectByProductIdLeftJoinOrderByPrice(page,product_id);
            case "d": // 评分高排序
                return commentMapper.selectByProductIdLeftJoinOrderByRate(page,product_id);
            case "e": // 评分低排序
                return commentMapper.selectByProductIdLeftJoinOrderByRateLow(page,product_id);

            default:
                break;
        }
        return page;

    }

}
