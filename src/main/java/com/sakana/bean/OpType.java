package com.sakana.bean;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OpType {
    //对象（key , value）
    DEPOSITE("deposite", 1),
    WITHDRAWAL("withdraw", 2),
    TRANSFER("transfer", 3);

    private final String key;
    private final Integer value;

    OpType(String key, Integer value) {
        this.key = key;
        this.value = value;
    }
//根据key获取枚举值
    public static OpType fromKey(String key) {
        for (OpType type : values()) {
            if (type.key.equalsIgnoreCase(key)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知操作类型：" + key);
    }

    @JsonValue
    public String getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }
}
