package com.sakana.web.vo;

public enum ResultCode {

    SUCCESS(200, "业务成功"),
    FAIL(500, "系统异常"),
    PARAM_ERROR(400, "参数错误"),
    NOT_LOGIN(401, "未登录"),
    NO_PERMISSION(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),
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
