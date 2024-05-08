package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *  评论
 * </p>
 *
 * @author yjz
 * @since 2024-05-07
 */
@Data
@TableName(value = "t_comment", autoResultMap = true) // 不加autoResultMap无法映射json
public class Comment implements Serializable {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long id;

    /**
     * 哪个商品的评论
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long product_id;

    /**
     * 父评论id（如果是子评论）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long father_comm_id;

    /**
     * 用户id（实时获取最新用户信息）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long user_id;

    /**
     * 评论内容
     */
    private String info;

    /**
     * 创建时间 记得转换为datetime名字给前端
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;

    /**
     * 点赞用户id表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING) // Long异常的处理
    private List<Long> love_list;

    /**
     * 是否是置顶
     */
    private Boolean is_top;

    /**
     * 地点
     */
    private String ip_location;

    /**
     * 用户ip
     */
    private String ip;

    /**
     * 观看人数
     */
    private Integer saw_num;

    /**
     * 子评论数（如果是父评论）
     */
    private Integer sub_num;

    Boolean pass_examination;


}
