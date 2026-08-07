package com.sakana.web.vo;

public enum ResultCode {

    SUCCESS(1, "业务成功"),
    FAIL(-1, "系统异常"),
    PARAM_ERROR(-2, "参数错误"),
    NOT_LOGIN(-3, "未登录"),
    NO_PERMISSION(-4, "没有权限"),
    NOT_FOUND(-5, "资源不存在"),
    DEAD_BEAT(-6, "老赖");


    private final Integer code;

    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
