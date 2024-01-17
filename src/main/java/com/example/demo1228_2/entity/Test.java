package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.List;

@Data //自动生成get set方法
@TableName(value = "t_test", autoResultMap = true) //实体绑定mysql的t_user表（用于mapper的mybatisplus）
public class Test {
    //@TableId(value = "id", type = IdType.AUTO) // 不设定值插入数据库时，自动自增// 不用这行业可以 关键是mysql要设置自增
    int id;

    @TableField(typeHandler = JacksonTypeHandler.class)
    List<User> address;
}
