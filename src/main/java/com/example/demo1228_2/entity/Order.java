package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data //自动生成get set方法
@TableName("t_order") //实体绑定mysql的t_user表（用于mapper的mybatisplus）
public class Order {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long user_id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long product_id;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;
    int product_num;
    // 防止is被自动省略
}
