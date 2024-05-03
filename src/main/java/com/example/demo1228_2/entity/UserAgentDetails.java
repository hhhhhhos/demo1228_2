package com.example.demo1228_2.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import nl.basjes.parse.useragent.UserAgent;

import java.time.LocalDateTime;

@Data //自动生成get set方法
@TableName(value = "t_user_agent_details", autoResultMap = true)
public class UserAgentDetails {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String deviceClass;
    private String deviceName;
    private String deviceBrand;
    private String deviceFirmwareVersion;
    private String deviceVersion;
    private String operatingSystemClass;
    private String operatingSystemName;
    private String operatingSystemVersion;
    private String operatingSystemVersionMajor;
    private String operatingSystemNameVersion;
    private String operatingSystemNameVersionMajor;
    private String layoutEngineClass;
    private String layoutEngineName;
    private String layoutEngineVersion;
    private String layoutEngineVersionMajor;
    private String layoutEngineNameVersion;
    private String layoutEngineNameVersionMajor;
    private String agentClass;
    private String agentName;
    private String agentVersion;
    private String agentVersionMajor;
    private String agentNameVersion;
    private String agentNameVersionMajor;
    private String agentInformationEmail;
    private String webviewAppName;
    private String webviewAppVersion;
    private String webviewAppVersionMajor;
    private String webviewAppNameVersion;
    private String webviewAppNameVersionMajor;
    private String networkType;
    private boolean syntaxError;

    // 6个其他属性
    private String realIp;

    private String forwardedProto; // 协议？http wss

    private String originalURI;

    private String Method;

    private String user_uuid;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long user_id;

    private String city;

    private String visitor_name;

    String wechat_nickname;

    String wechat_unionid;

    String wechat_headimgurl;


    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime create_time;

    public UserAgentDetails(){}

    public UserAgentDetails(UserAgent userAgent) {
        this.deviceClass = userAgent.getValue("DeviceClass");
        this.deviceName = userAgent.getValue("DeviceName");
        this.deviceBrand = userAgent.getValue("DeviceBrand");
        this.deviceFirmwareVersion = userAgent.getValue("DeviceFirmwareVersion");
        this.deviceVersion = userAgent.getValue("DeviceVersion");
        this.operatingSystemClass = userAgent.getValue("OperatingSystemClass");
        this.operatingSystemName = userAgent.getValue("OperatingSystemName");
        this.operatingSystemVersion = userAgent.getValue("OperatingSystemVersion");
        this.operatingSystemVersionMajor = userAgent.getValue("OperatingSystemVersionMajor");
        this.operatingSystemNameVersion = userAgent.getValue("OperatingSystemNameVersion");
        this.operatingSystemNameVersionMajor = userAgent.getValue("OperatingSystemNameVersionMajor");
        this.layoutEngineClass = userAgent.getValue("LayoutEngineClass");
        this.layoutEngineName = userAgent.getValue("LayoutEngineName");
        this.layoutEngineVersion = userAgent.getValue("LayoutEngineVersion");
        this.layoutEngineVersionMajor = userAgent.getValue("LayoutEngineVersionMajor");
        this.layoutEngineNameVersion = userAgent.getValue("LayoutEngineNameVersion");
        this.layoutEngineNameVersionMajor = userAgent.getValue("LayoutEngineNameVersionMajor");
        this.agentClass = userAgent.getValue("AgentClass");
        this.agentName = userAgent.getValue("AgentName");
        this.agentVersion = userAgent.getValue("AgentVersion");
        this.agentVersionMajor = userAgent.getValue("AgentVersionMajor");
        this.agentNameVersion = userAgent.getValue("AgentNameVersion");
        this.agentNameVersionMajor = userAgent.getValue("AgentNameVersionMajor");
        this.agentInformationEmail = userAgent.getValue("AgentInformationEmail");
        this.webviewAppName = userAgent.getValue("WebviewAppName");
        this.webviewAppVersion = userAgent.getValue("WebviewAppVersion");
        this.webviewAppVersionMajor = userAgent.getValue("WebviewAppVersionMajor");
        this.webviewAppNameVersion = userAgent.getValue("WebviewAppNameVersion");
        this.webviewAppNameVersionMajor = userAgent.getValue("WebviewAppNameVersionMajor");
        this.networkType = userAgent.getValue("NetworkType");
        this.syntaxError = Boolean.parseBoolean(userAgent.getValue("__SyntaxError__"));
    }
}
