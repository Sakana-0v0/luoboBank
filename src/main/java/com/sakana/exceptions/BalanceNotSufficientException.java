package com.sakana.exceptions;

import com.sakana.web.vo.ResultCode;


public class BalanceNotSufficientException extends RuntimeException {

    private Integer code;

    public BalanceNotSufficientException(Integer code, String message) {

        super(message);

        this.code = code;
    }

    public BalanceNotSufficientException(ResultCode code) {

        super(code.getMessage());

        this.code = code.getCode();
    }

    public Integer getCode() {
        return code;
    }


}
