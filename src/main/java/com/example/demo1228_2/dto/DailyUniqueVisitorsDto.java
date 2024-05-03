package com.example.demo1228_2.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DailyUniqueVisitorsDto {
    private LocalDate create_time;
    private int uuid_count;

    public DailyUniqueVisitorsDto(LocalDate create_time,int uuid_count){
        this.create_time = create_time;
        this.uuid_count = uuid_count;
    }
}
