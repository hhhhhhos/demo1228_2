package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.demo1228_2.Vo.Address;
import com.example.demo1228_2.config.JsonToListTypeHandler;
import com.example.demo1228_2.dto.BuylistDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data //自动生成get set方法
@TableName(value = "t_order", autoResultMap = true) //实体绑定mysql的t_user表（用于mapper的mybatisplus）
public class Order {
    // 主键自动回显
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long user_id;


    // 直接用List对接mysql的json 会使得日期序列化失效 成数组 原因未知
    //@TableField(typeHandler = JacksonTypeHandler.class)
    // Gpt说让我自定义序列化器
    @TableField(typeHandler = JsonToListTypeHandler.class)// Gpt定义序的列化器
    private List<BuylistDto> info;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;

    private String status;

    BigDecimal TotalMoney;
    int TotalNum;

    @TableField(typeHandler = JacksonTypeHandler.class)
    Address address;
    // 防止is被自动省略
}
