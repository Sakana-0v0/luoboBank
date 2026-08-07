package com.sakana.bean;

import lombok.Data;

@Data
public class OpRecord {
    private Integer id;
    private Integer accountId;
    private Double opMoney;
    private String opTime;    //操作时间

    private OpType opType;   //  操作类型：存款、取款、转账 deposite withdraw transfer

    private Integer transferId; //转账目标账号id

    private String remark;   // 备注（选填）
}
