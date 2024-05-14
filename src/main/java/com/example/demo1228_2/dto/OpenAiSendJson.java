package com.example.demo1228_2.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OpenAiSendJson {

    String model;

    List<OpenAiJsonMessageObject> messages;

}
