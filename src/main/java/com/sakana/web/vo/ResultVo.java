package com.sakana.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder   // 将当前类转为构造器模式，可以使用链式方式调用
public class ResultVo<T> {
    private Integer code;  //业务状态码
    private String msg;   //业务状态描述
    private T data;      //业务数据

    public static <T> ResultVo<T> success(T data) {
        return new ResultVo<>(200, "success", data);
    }

    public static <T> ResultVo<T> fail(Integer code, String msg) {
        return new ResultVo<>(code, msg, null);
    }
}
