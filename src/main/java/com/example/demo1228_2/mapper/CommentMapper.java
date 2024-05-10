package com.example.demo1228_2.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo1228_2.dto.CommentOrderUserRateDto;
import com.example.demo1228_2.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yjz
 * @since 2024-05-07
 */
public interface CommentMapper extends BaseMapper<Comment> {


    // 按评分排序(废弃)
    @Select("SELECT t_comment.*, t_product_rate.rate " +
            "FROM t_comment " +
            "LEFT JOIN t_product_rate ON t_comment.user_id = t_product_rate.user_id " +
            "AND t_comment.product_id = t_product_rate.product_id " +
            "WHERE t_comment.product_id = #{product_id} " +
            "order by t_product_rate.rate DESC, create_time DESC "+ // 次排序create_time
            "LIMIT #{limit} OFFSET #{offset}") //#{limit} 是每页你希望返回的记录数。#{offset} 是你希望跳过的记录数，通常计算为 (页码 - 1) * 每页记录数
    List<CommentOrderUserRateDto> FeiQile(Long limit,Long offset, Long product_id); // 左连接 右可空

    /**
     * 按财主排序 // 连表 Order2 和product_rate 和user // 左连接 右可空
     * @param product_id 商品id
     * @return List
     */
    @Select("SELECT c.*, o.total_spent, o.total_quantity, r.rate, u.name, u.wechat_nickname, u.wechat_headimgurl " +
            "FROM t_comment AS c " +
            "LEFT JOIN (" +
            "    SELECT user_id, SUM(purchase_total_price) AS total_spent, SUM(purchase_num) AS total_quantity " +
            "    FROM t_order2 " +
            "    WHERE product_id = #{product_id} " +
            "    GROUP BY user_id" +
            ") AS o ON c.user_id = o.user_id " +
            "LEFT JOIN t_product_rate AS r ON c.user_id = r.user_id AND c.product_id = r.product_id " +
            "LEFT JOIN t_user AS u ON c.user_id = u.id " +
            "WHERE c.product_id = #{product_id} AND (c.father_comm_id = 0 OR c.father_comm_id IS NULL) " +
            "ORDER BY o.total_spent DESC, c.create_time DESC " // 次排序create_time
    )
    IPage<CommentOrderUserRateDto> selectByProductIdLeftJoinOrderByPrice(Page<CommentOrderUserRateDto> page, Long product_id);

    /**
     * 按时间排序 // 连表 Order2 和product_rate 和user // 左连接 右可空
     * @param product_id 商品id
     * @return List
     */
    @Select("SELECT c.*, o.total_spent, o.total_quantity, r.rate, u.name, u.wechat_nickname, u.wechat_headimgurl " +
            "FROM t_comment AS c " +
            "LEFT JOIN (" +
            "    SELECT user_id, SUM(purchase_total_price) AS total_spent, SUM(purchase_num) AS total_quantity " +
            "    FROM t_order2 " +
            "    WHERE product_id = #{product_id} " +
            "    GROUP BY user_id" +
            ") AS o ON c.user_id = o.user_id " +
            "LEFT JOIN t_product_rate AS r ON c.user_id = r.user_id AND c.product_id = r.product_id " +
            "LEFT JOIN t_user AS u ON c.user_id = u.id " +
            "WHERE c.product_id = #{product_id} AND (c.father_comm_id = 0 OR c.father_comm_id IS NULL) " +
            "ORDER BY c.create_time DESC " // 次排序create_time
    )
    IPage<CommentOrderUserRateDto> selectByProductIdLeftJoinOrderByTime(Page<CommentOrderUserRateDto> page, Long product_id);

    /**
     * 按评分排序 // 连表 Order2 和product_rate 和user // 左连接 右可空
     * @param product_id 商品id
     * @return List
     */
    @Select("SELECT c.*, o.total_spent, o.total_quantity, r.rate, u.name, u.wechat_nickname, u.wechat_headimgurl " +
            "FROM t_comment AS c " +
            "LEFT JOIN (" +
            "    SELECT user_id, SUM(purchase_total_price) AS total_spent, SUM(purchase_num) AS total_quantity " +
            "    FROM t_order2 " +
            "    WHERE product_id = #{product_id} " +
            "    GROUP BY user_id" +
            ") AS o ON c.user_id = o.user_id " +
            "LEFT JOIN t_product_rate AS r ON c.user_id = r.user_id AND c.product_id = r.product_id " +
            "LEFT JOIN t_user AS u ON c.user_id = u.id " +
            "WHERE c.product_id = #{product_id} AND (c.father_comm_id = 0 OR c.father_comm_id IS NULL) " +
            "ORDER BY r.rate DESC, create_time DESC "
    )
    IPage<CommentOrderUserRateDto> selectByProductIdLeftJoinOrderByRate(Page<CommentOrderUserRateDto> page, Long product_id);

    /**
     * 子评论查询 按点赞排序 // 连表 Order2 和product_rate 和user // 左连接 右可空
     * @param product_id 商品id
     * @return List
     */
    @Select("SELECT c.*, o.total_spent, o.total_quantity, r.rate, u.name, u.wechat_nickname, u.wechat_headimgurl " +
            "FROM t_comment AS c " +
            "LEFT JOIN (" +
            "    SELECT user_id, SUM(purchase_total_price) AS total_spent, SUM(purchase_num) AS total_quantity " +
            "    FROM t_order2 " +
            "    WHERE product_id = #{product_id} " +
            "    GROUP BY user_id" +
            ") AS o ON c.user_id = o.user_id " +
            "LEFT JOIN t_product_rate AS r ON c.user_id = r.user_id AND c.product_id = r.product_id " +
            "LEFT JOIN t_user AS u ON c.user_id = u.id " +
            "WHERE c.product_id = #{product_id} AND c.father_comm_id = #{father_comm_id} " +
            "ORDER BY c.love_list DESC, create_time DESC "
    )
    IPage<CommentOrderUserRateDto> selectByProductIdLeftJoinOrderByRateSub(Page<CommentOrderUserRateDto> page, Long product_id,Long father_comm_id);

    /**
     * 按点赞排序 // 连表 Order2 和product_rate 和user // 左连接 右可空
     * @param product_id 商品id
     * @return List
     */
    @Select("SELECT c.*, o.total_spent, o.total_quantity, r.rate, u.name, u.wechat_nickname, u.wechat_headimgurl " +
            "FROM t_comment AS c " +
            "LEFT JOIN (" +
            "    SELECT user_id, SUM(purchase_total_price) AS total_spent, SUM(purchase_num) AS total_quantity " +
            "    FROM t_order2 " +
            "    WHERE product_id = #{product_id} " +
            "    GROUP BY user_id" +
            ") AS o ON c.user_id = o.user_id " +
            "LEFT JOIN t_product_rate AS r ON c.user_id = r.user_id AND c.product_id = r.product_id " +
            "LEFT JOIN t_user AS u ON c.user_id = u.id " +
            "WHERE c.product_id = #{product_id} AND (c.father_comm_id = 0 OR c.father_comm_id IS NULL) " +
            "ORDER BY c.love_list DESC, create_time DESC " // 次排序create_time
            )
    IPage<CommentOrderUserRateDto> selectByProductIdLeftJoinOrderByLike(Page<CommentOrderUserRateDto> page, Long product_id);

    IPage<CommentOrderUserRateDto> selectByProductIdLeftJoinOrderByLike2(Page<CommentOrderUserRateDto> page, Long product_id);

    /**
     * 按评分低排序 // 连表 Order2 和product_rate 和user // 左连接 右可空
     * @param product_id 商品id
     * @return List
     */
    @Select("SELECT c.*, o.total_spent, o.total_quantity, r.rate, u.name, u.wechat_nickname, u.wechat_headimgurl " +
            "FROM t_comment AS c " +
            "LEFT JOIN (" +
            "    SELECT user_id, SUM(purchase_total_price) AS total_spent, SUM(purchase_num) AS total_quantity " +
            "    FROM t_order2 " +
            "    WHERE product_id = #{product_id} " +
            "    GROUP BY user_id" +
            ") AS o ON c.user_id = o.user_id " +
            "LEFT JOIN t_product_rate AS r ON c.user_id = r.user_id AND c.product_id = r.product_id " +
            "LEFT JOIN t_user AS u ON c.user_id = u.id " +
            "WHERE c.product_id = #{product_id} AND (c.father_comm_id = 0 OR c.father_comm_id IS NULL) " +
            "ORDER BY COALESCE(r.rate, 999) ASC, c.create_time DESC " // 使用COALESCE为NULL提供默认值  // 次排序create_time
    )
    IPage<CommentOrderUserRateDto> selectByProductIdLeftJoinOrderByRateLow(Page<CommentOrderUserRateDto> page, Long product_id);
}
