package com.sakana.web.vo;


import lombok.Data;

import java.io.Serializable;


@Data
public class BankAccountVO implements Serializable {
    private Integer accountId;
    private Double balance;

    private Double opMoney;  //操作金额

    private Integer targetAccountId;  //接收方账号

    private String remark;   // 备注（选填）
}
