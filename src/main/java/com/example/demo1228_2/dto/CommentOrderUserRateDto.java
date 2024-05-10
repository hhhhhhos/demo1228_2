package com.example.demo1228_2.dto;


import com.example.demo1228_2.entity.Comment;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommentOrderUserRateDto extends Comment {
    private BigDecimal rate;

    int total_quantity;
    private BigDecimal total_spent;

    private String name;

    String wechat_nickname;

    String wechat_headimgurl;
}
