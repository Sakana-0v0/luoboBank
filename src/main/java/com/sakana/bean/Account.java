package com.sakana.bean;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

// PO类 ->数据库表
@Data
public class Account implements Serializable {
    private Integer accountId;
    private Double balance;
    private String email;

    @JsonIgnore
    private String passwordHash;

    /**
     * 账户状态：0-正常，1-黑名单
     */
    private Integer status = 0;
}
