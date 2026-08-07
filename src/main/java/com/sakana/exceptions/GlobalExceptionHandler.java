package com.sakana.exceptions;

import com.sakana.web.vo.ResultVo;
import lombok.extern.java.Log;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.logging.Level;

@RestControllerAdvice
@Log
public class GlobalExceptionHandler {

    @ExceptionHandler(BalanceNotSufficientException.class)
    public ResultVo<?> businessException(BalanceNotSufficientException e) {
        log.log(Level.SEVERE, "业务异常：" + e.getMessage(), e);
        return ResultVo.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResultVo<?> illegalArgument(IllegalArgumentException e) {
        log.log(Level.WARNING, "参数异常：" + e.getMessage(), e);
        return ResultVo.fail(400, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResultVo<?> methodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.log(Level.WARNING, "请求方法不支持：" + e.getMessage(), e);
        return ResultVo.fail(405, "不支持的请求方法，允许的方法：" + String.join(", ", e.getSupportedMethods()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResultVo<?> noResourceFound(NoResourceFoundException e) {
        log.log(Level.WARNING, "请求资源不存在：" + e.getMessage(), e);
        return ResultVo.fail(404, "请求的资源不存在，请检查路径");
    }

    @ExceptionHandler(Exception.class)
    public ResultVo<?> exception(Exception e) {
        log.log(Level.SEVERE, "系统未捕获异常", e);
        return ResultVo.fail(500, "服务器繁忙，请稍后重试");
    }
}