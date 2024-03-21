package com.example.demo1228_2.Vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

// 传输给前端的数据
@Data
public class Alipay {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long id;

    BigDecimal money;

    int num;

    String name;
}
