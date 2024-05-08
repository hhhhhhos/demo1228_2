package com.example.demo1228_2.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author yjz
 * @since 2024-05-08
 */
@Data
@TableName(value = "t_order2", autoResultMap = true)
public class Order2 implements Serializable {

    /**
     * 为了评论 显示已购买 搞的表
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long product_id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long user_id;

    private Integer purchase_num;

    private BigDecimal purchase_total_price;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;


}
