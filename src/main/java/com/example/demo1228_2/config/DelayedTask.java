package com.example.demo1228_2.config;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Data
public class DelayedTask implements Delayed {
    private String taskName;
    private long startTime; // 任务开始时间


    public DelayedTask(String taskName, long delayInMilliseconds) {
        this.taskName = taskName;
        this.startTime = System.currentTimeMillis() + delayInMilliseconds;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (this.startTime < ((DelayedTask) o).startTime) {
            return -1;
        }
        if (this.startTime > ((DelayedTask) o).startTime) {
            return 1;
        }
        return 0;
    }

    public void executeTask() {
        System.out.println("任务："+taskName + " 终止于 " + Tool.getDateTime());
    }
}

