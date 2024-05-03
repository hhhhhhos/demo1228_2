package com.example.demo1228_2.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DelayedTaskDto {
    private String taskName;
    private long startTime;
    private Map map;
    Double remainTime; // 剩余时间

    private String endDate; // 到期年月日
}
