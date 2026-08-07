package com.sakana.dao;

import com.sakana.bean.OpRecord;

import java.util.List;


public interface OpRecordDao {

    //设计日志的添加接口方法: TODO: 参数
    public int insertOpRecord(OpRecord opRecord);


    public List<OpRecord> findOpRecord(int accountId);

    public List<OpRecord> findOpRecord(int accountId, String opType);


    public List<OpRecord> findOpRecord(OpRecord opRecord);

}
