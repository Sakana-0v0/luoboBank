package com.sakana.exceptions;

import com.sakana.web.vo.ResultVo;
import lombok.extern.java.Log;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.UncategorizedSQLException;
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

    @ExceptionHandler(DeadbeatException.class)
    public ResultVo<?> deadbeatException(DeadbeatException e) {
        log.log(Level.WARNING, "黑名单用户尝试操作：" + e.getMessage(), e);
        return ResultVo.fail(403, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResultVo<?> illegalArgument(IllegalArgumentException e) {
        log.log(Level.WARNING, "参数异常：" + e.getMessage(), e);
        return ResultVo.fail(400, e.getMessage());
    }

    @ExceptionHandler(UncategorizedSQLException.class)
    public ResultVo<?> sqlException(UncategorizedSQLException e) {
        String message = e.getMessage();
        log.log(Level.WARNING, "SQL异常：" + message, e);

        // 检查约束违反（如余额为负数）
        if (message != null && message.contains("Check constraint")) {
            return ResultVo.fail(400, "余额不足，无法完成操作");
        }

        return ResultVo.fail(500, "数据库操作失败，请稍后重试");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResultVo<?> dataIntegrityException(DataIntegrityViolationException e) {
        log.log(Level.WARNING, "数据完整性异常：" + e.getMessage(), e);
        return ResultVo.fail(400, "数据操作失败，请检查输入是否合法");
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