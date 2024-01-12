package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data //自动生成get set方法
@TableName("t_user") //实体绑定mysql的t_user表（用于mapper的mybatisplus）
public class User {
    private int id;
    private String name;
    private String age;
    private String sex;
    private String address;
    private String phone;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;
    private String password;

}
