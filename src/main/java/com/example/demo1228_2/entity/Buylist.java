package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data //自动生成get set方法
@TableName("t_buylist") //实体绑定mysql的t_user表（用于mapper的mybatisplus）
public class Buylist {
    int id;
    int user_id;
    int product_id;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;
    int product_num;

}
