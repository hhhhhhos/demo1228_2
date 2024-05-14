package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data //自动生成get set方法
@JsonIgnoreProperties(ignoreUnknown = true) // json遇到实体没有的属性 不映射
@TableName("t_product") //实体绑定mysql的t_user表（用于mapper的mybatisplus）
public class Product {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long id;
    String name;
    BigDecimal price;
    int num;
    String info;
    String photo;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;

    String type;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long user_id;

    int visited_num;

    int sold_num;

    BigDecimal rate;

    int rate_num;

    //@JsonFormat(shape = JsonFormat.Shape.STRING)
    //Long product_related_list_id;

    String type2;
    @Version
    private Integer version; // 乐观锁版本号

    @TableField(typeHandler = JacksonTypeHandler.class)
    List<String> photo_list;
}
