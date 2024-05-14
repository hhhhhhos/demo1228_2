package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.demo1228_2.config.JsonToListTypeHandler;
import com.example.demo1228_2.dto.OpenAiJsonMessageObject;
import com.example.demo1228_2.dto.OpenAiSendJson;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 存放和客服的聊天记录
 * </p>
 *
 * @author yjz
 * @since 2024-05-13
 */
@Data
@TableName(value = "t_kefu_chat_history", autoResultMap = true) //实体绑定mysql的t_user表（用于mapper的mybatisplus）
public class KefuChatHistory implements Serializable {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long user_id;

    @TableField(typeHandler = JacksonTypeHandler.class)
    OpenAiSendJson send_json;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;

    String kefu_name;

    Boolean is_finish;
}
