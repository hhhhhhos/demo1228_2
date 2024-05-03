package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.example.demo1228_2.mapper.DataResultMapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 服务器数据汇总 都汇总到id 45698吧
 * </p>
 *
 * @author yjz
 * @since 2024-04-22
 */
@Data
@TableName("t_data_result")
public class DataResult implements Serializable {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private Integer home_visitors;

    private Integer back_visitors;

    private Integer comment_total_num;

    private Integer product_total_num;

    private Integer order_total_num;

    private Integer sells_total_num;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;

    @Version
    private Integer version;

}
