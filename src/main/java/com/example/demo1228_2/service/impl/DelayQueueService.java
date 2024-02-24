package com.example.demo1228_2.config;

import com.example.demo1228_2.dto.DelayedTaskDto;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.DelayQueue;

@Service
public class DelayQueueService {
    private DelayQueue<DelayedTask> delayQueue = new DelayQueue<>();

    @PostConstruct
    public void init() {
        // 启动一个线程来处理队列中到期的任务
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    DelayedTask task = delayQueue.take(); // 会阻塞直到有元素到期
                    task.executeTask();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


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

            // 在同一个循环中计算并设置剩余时间
            double remainTimeInSeconds = (dto.getStartTime() - System.currentTimeMillis()) / 1000.0; // 将毫秒转换为秒
            BigDecimal formattedTime = BigDecimal.valueOf(remainTimeInSeconds).setScale(2, RoundingMode.HALF_UP); // 保留两位小数
            dto.setRemainTime(formattedTime.doubleValue()); // 假设setRemainTime接受的是double类型

            delayedTaskDtosList.add(dto);
        }

        return delayedTaskDtosList;
    }


    public List<DelayedTask> getRawTasks() {
        return new ArrayList<>(delayQueue);
    }
}
