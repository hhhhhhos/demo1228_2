package com.example.demo1228_2.service.impl;

import com.example.demo1228_2.config.DelayedTask;
import com.example.demo1228_2.dto.DelayedTaskDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.DelayQueue;

@Slf4j
@Service
public class DelayQueueService {
    private DelayQueue<DelayedTask> delayQueue = new DelayQueue<>();

    public void addTask(DelayedTask task) {
        delayQueue.put(task);
    }

    public boolean removeTask(DelayedTask task) {
        return delayQueue.remove(task);
    }

    public List<DelayedTaskDto> getTasks() {
        List<DelayedTask> delayedTasksList = new ArrayList<>(delayQueue); // 假设这是你从DelayQueue获取的列表
        List<DelayedTaskDto> delayedTaskDtosList = new ArrayList<>();

        for(DelayedTask delayedTask : delayedTasksList) {
            DelayedTaskDto dto = new DelayedTaskDto();
            dto.setTaskName(delayedTask.getTaskName());
            dto.setStartTime(delayedTask.getStartTime());
            dto.setMap(delayedTask.getMap());
            // 在同一个循环中计算并设置剩余时间
            double remainTimeInSeconds = (dto.getStartTime() - System.currentTimeMillis()) / 1000.0; // 将毫秒转换为秒
            BigDecimal formattedTime = BigDecimal.valueOf(remainTimeInSeconds).setScale(2, RoundingMode.HALF_UP); // 保留两位小数
            dto.setRemainTime(formattedTime.doubleValue()); // 假设setRemainTime接受的是double类型
            // 设置到时时分秒
            Date date = new Date(delayedTask.getStartTime());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            String formattedDate = formatter.format(date);
            dto.setEndDate(formattedDate);

            delayedTaskDtosList.add(dto);
        }

        return delayedTaskDtosList;
    }


    public List<DelayedTask> getRawTasks() {
        return new ArrayList<>(delayQueue);
    }
}
