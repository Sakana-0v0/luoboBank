package com.sakana.dao;


import com.sakana.bean.OpRecord;
import com.sakana.bean.OpType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;


@Repository   //IOC
public class OpRecordDaoImpl implements OpRecordDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired   //  此时dataSource是由springboot自动注入的
    public OpRecordDaoImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public int insertOpRecord(OpRecord opRecord) {
        String sql = "insert into oprecord (accountId,opmoney,  opTime, opType,transferid, remark) values (?, ?, now(), ?, ?, ?)";
        return this.jdbcTemplate.update(sql,
                opRecord.getAccountId(), opRecord.getOpMoney(),

                opRecord.getOpType().getKey(),    // opType是枚举类型，需要获取key值,它是字符串，是数据库中的字段

                opRecord.getTransferId(),
                opRecord.getRemark());
    }

    @Override
    public List<OpRecord> findOpRecord(int accountid) {
        List<OpRecord> list = this.jdbcTemplate.query(
                "select * from oprecord where accountId=?",
                (resultSet, rowNum) -> {
                    OpRecord opRecord = new OpRecord();
                    opRecord.setAccountId(resultSet.getInt("accountId"));
                    opRecord.setOpMoney(resultSet.getDouble("opMoney"));
                    opRecord.setOpTime(resultSet.getString("opTime"));

                    //            DEPOSITE            deposite
                    opRecord.setOpType(
                            OpType.fromKey(resultSet.getString("opType"))
                    );

                    //                                   "deposite"
                    //opRecord.setOpType(OpType.valueOf(resultSet.getString("opType")));

                    Integer transferId = resultSet.getObject("transferId", Integer.class);
                    opRecord.setTransferId(transferId);

                    opRecord.setRemark(resultSet.getString("remark"));
                    return opRecord;
                }, accountid);
        return list;
    }

    @Override
    public List<OpRecord> findOpRecord(int accountId, String opType) {
        List<OpRecord> list = this.jdbcTemplate.query(
                "select * from oprecord where accountId=? and opType=?",
                (resultSet, rowNum) -> {
                    OpRecord opRecord = new OpRecord();
                    opRecord.setAccountId(resultSet.getInt("accountId"));
                    opRecord.setOpMoney(resultSet.getDouble("opMoney"));
                    opRecord.setOpTime(resultSet.getString("opTime"));


                    opRecord.setOpType(
                            OpType.fromKey(resultSet.getString("opType"))
                    );
                    //opRecord.setOpType(OpType.valueOf(resultSet.getString("opType")));

                    Integer transferId = resultSet.getObject("transferId", Integer.class);
                    opRecord.setTransferId(transferId);

                    opRecord.setRemark(resultSet.getString("remark"));
                    return opRecord;
                }, accountId, opType);
        return list;

    }

    @Override
    public List<OpRecord> findOpRecord(OpRecord opRecord) {
        return null;
    }
}
