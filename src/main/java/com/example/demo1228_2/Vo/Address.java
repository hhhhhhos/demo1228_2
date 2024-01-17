package com.example.demo1228_2.Vo;

import lombok.Data;

@Data // VO-数据库不存在的数据(以json形式存放在User的addresses里)
public class Address {
    // 所在地区
    String info;
    // 详细地址
    String detail;
    String name;
    String phone;
    boolean is_default;

    // 必须重写get方法 不然is前缀会导致失效
    public boolean getIs_default() {
        return is_default;
    }

    public void setIs_default(boolean is_default) {
        this.is_default = is_default;
    }
}
