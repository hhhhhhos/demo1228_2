package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.demo1228_2.dto.BuylistDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data //自动生成get set方法
@TableName(value = "t_test", autoResultMap = true) //实体绑定mysql的t_user表（用于mapper的mybatisplus）
public class Test {
    //@TableId(value = "id", type = IdType.AUTO) // 不设定值插入数据库时，自动自增// 不用这行业可以 关键是mysql要设置自增
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long id;

    @TableField(typeHandler = JacksonTypeHandler.class)
    List<User> address;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<BuylistDto> info;

    @TableField(typeHandler = JacksonTypeHandler.class)
    List<Long> id_list;

    private LocalDateTime create_time;

    int num;

    @Version
    private Integer version; // 乐观锁版本号


}
