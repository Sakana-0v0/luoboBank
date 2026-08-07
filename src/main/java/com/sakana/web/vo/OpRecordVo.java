package com.sakana.web.vo;

import com.sakana.bean.OpType;
import lombok.Data;

import java.io.Serializable;

@Data
public class OpRecordVo implements Serializable {
    private Integer id;
    private Integer accountId;   // 账户ID
    private Double opMoney;
    private String opTime;    //操作时间

    private OpType opType;   //  *** 枚举类型，

    private Integer transferId;

    private String remark;   // 备注（选填）
}
