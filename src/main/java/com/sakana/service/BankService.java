package com.sakana.service;

import com.sakana.bean.Account;
import com.sakana.bean.OpRecord;

import java.util.List;


public interface BankService {

    //开户接口方法
    public Account openAccount(double money, String password, String email);

    //存款接口方法
    public Account deposite(int accountid, double money, Integer otherAccountId, String remark);

    //取款接口方法
    public Account withdraw(int accountid, double money, Integer otherAccountId, String remark);

    //转账接口方法
    public Account transfer(int accountId, double money, int toAccountId, String remark);
    //查询账户接口方法
    public Account findAccount(int accountId);

    //查询操作记录接口方法
    public List<OpRecord> findOpRecordByAccountId(int accountId);

}
