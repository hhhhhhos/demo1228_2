package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.demo1228_2.config.JsonToListTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 商品关联表（单个商品 选其他种类时跳转）
 * </p>
 *
 * @author yjz
 * @since 2024-05-05
 */
@Data
@TableName(value = "t_product_related_list", autoResultMap = true)
public class ProductRelatedList implements Serializable {


    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 相关联产品id（同一品类）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING) // Long异常的处理
    List<Long> product_id_list;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;


}
