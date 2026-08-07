package com.sakana.service;

import com.sakana.bean.Account;
import com.sakana.bean.OpRecord;
import com.sakana.bean.OpType;
import com.sakana.dao.AccountDao;
import com.sakana.dao.OpRecordDao;
import com.sakana.exceptions.BalanceNotSufficientException;
import com.sakana.utils.PasswordEncoder;
import com.sakana.utils.PasswordStrengthValidator;
import com.sakana.web.vo.ResultCode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional    //给这个类加入事务.
@Log4j2
public class BankServiceImpl implements BankService {

    @Autowired
    private AccountDao accountdao;
    @Autowired
    private OpRecordDao opRecordDao;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordStrengthValidator passwordStrengthValidator;
    @Autowired
    private MailService mailService;


    @Override
    public Account openAccount(double money, String password, String email) {
        if (money < 1) {
            throw new IllegalArgumentException("金额不能小于1");
        }
        // 验证密码强度
        if (!passwordStrengthValidator.isStrongEnough(password)) {
            String message = passwordStrengthValidator.getStrengthMessage(password);
            throw new IllegalArgumentException(message);
        }
        // 密码哈希并存储
        String passwordHash = passwordEncoder.encode(password);
        int newAccountId = this.accountdao.insertWithPasswordAndEmail(money, passwordHash, email);
        //记录流水日志
        OpRecord op = new OpRecord();
        op.setAccountId(newAccountId);
        op.setOpMoney(money);
        op.setOpType(OpType.DEPOSITE);
        this.opRecordDao.insertOpRecord(op);

        Account a = this.findAccount(newAccountId);

        // 开户成功后发送邮件通知
        if (email != null && !email.isEmpty()) {
            sendNotificationEmail(a, "开户存款", money, null);
        }

        return a;
    }

    @Override
    public Account deposite(int accountid, double money, Integer otherAccountId, String remark) {
        Account a = null;
        try {
            a = this.accountdao.findById(accountid);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("查无此账户:" + accountid);
            throw new RuntimeException("查无此账户:" + accountid);
        }
        a.setBalance(a.getBalance() + money);
        this.accountdao.update(a.getAccountId(), a.getBalance());

        //记录流水日志
        OpRecord op = new OpRecord();
        op.setAccountId(accountid);
        op.setOpMoney(money);
        op.setOpType(OpType.DEPOSITE);
        op.setTransferId(otherAccountId);
        op.setRemark(remark);
        this.opRecordDao.insertOpRecord(op);

        // 发送账户变动邮件通知
        sendNotificationEmail(a, "存款", money, otherAccountId);

        return a;

    }

    //以下的问题: 先查accountid ->account  -> 判断余额
    //
    @Override
    public Account withdraw(int accountid, double money, Integer otherAccountId, String remark) {
        Account a = null;
        try {
            a = this.accountdao.findById(accountid);
        } catch (Exception ex) {
            log.error("查无此账户:" + accountid);
            throw new RuntimeException("查无此账户:" + accountid);
        }
        if (a.getBalance() < money) {
            throw new BalanceNotSufficientException(ResultCode.PARAM_ERROR);
        }
        a.setBalance(a.getBalance() - money);
        this.accountdao.update(accountid, a.getBalance());

        //记录流水日志
        OpRecord opRecord = new OpRecord();
        opRecord.setAccountId(accountid);
        opRecord.setOpMoney(-money);
        opRecord.setOpType(OpType.WITHDRAWAL);
        opRecord.setTransferId(otherAccountId);
        opRecord.setRemark(remark);
        this.opRecordDao.insertOpRecord(opRecord);

        // 发送账户变动邮件通知
        sendNotificationEmail(a, "取款", money, otherAccountId);

        return a;
    }

    @Override
    public Account transfer(int accountId, double money, int toAccountId, String remark) {
        // 通过代理对象调用，确保事务生效
        BankService bankService = applicationContext.getBean(BankService.class);

        // 获取转出账户信息（用于发送转账通知）
        Account fromAccount = this.findAccount(accountId);

        // 执行转入
        bankService.deposite(toAccountId, money, accountId, remark);
        // 执行转出
        Account result = bankService.withdraw(accountId, money, toAccountId, remark);

        // 发送转账通知邮件（给转出账户）
        sendNotificationEmail(fromAccount, "转账", money, toAccountId);

        return result;
    }

    @Transactional(readOnly = true)    //这是个只读事务.
    @Override
    public Account findAccount(int accountId) {
        return this.accountdao.findById(accountId);
    }

    @Override
    public List<OpRecord> findOpRecordByAccountId(int accountId) {
        return this.opRecordDao.findOpRecord(accountId);
    }

    /**
     * 发送账户变动邮件通知
     */
    private void sendNotificationEmail(Account account, String opType, double amount, Integer transferToAccountId) {
        try {
            if (transferToAccountId != null) {
                mailService.sendAccountChangeNotification(account, opType, amount, transferToAccountId.doubleValue());
            } else {
                mailService.sendAccountChangeNotification(account, opType, amount, null);
            }
        } catch (Exception e) {
            // 邮件发送失败不影响主业务，仅记录日志
            log.error("发送账户变动通知邮件失败: {}", e.getMessage());
        }
    }
}