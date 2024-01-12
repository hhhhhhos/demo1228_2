package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data //自动生成get set方法
@TableName("t_product") //实体绑定mysql的t_user表（用于mapper的mybatisplus）
public class Product {
    int id;
    String name;
    BigDecimal price;
    String photo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;

    String type;
}
