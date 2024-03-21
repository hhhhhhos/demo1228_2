package com.example.demo1228_2.dto;

import lombok.Data;

@Data
public class DelayedTaskDto {
    private String taskName;
    private long startTime;
    Double remainTime; // 剩余时间
}
