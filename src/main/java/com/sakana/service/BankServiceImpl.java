package com.sakana.service;

import com.sakana.annotation.DeadbeatCheck;
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

    @DeadbeatCheck
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
    @DeadbeatCheck
    @Override
    public Account withdraw(int accountid, double money, Integer otherAccountId, String remark) {
        Account a = null;
        try {
            a = this.accountdao.findById(accountid);
        } catch (Exception ex) {
            log.error("查无此账户:" + accountid);
            throw new RuntimeException("查无此账户:" + accountid);
        }
        // 使用 BigDecimal 进行精确比较，避免浮点数精度问题
        java.math.BigDecimal balance = java.math.BigDecimal.valueOf(a.getBalance());
        java.math.BigDecimal withdrawAmount = java.math.BigDecimal.valueOf(money);
        if (balance.compareTo(withdrawAmount) < 0) {
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

    @DeadbeatCheck
    @Override
    public Account transfer(int accountId, double money, int toAccountId, String remark) {
        log.info("转账请求 - fromAccountId: {}, toAccountId: {}, money: {}", accountId, toAccountId, money);

        // 获取转出账户信息（用于发送转账通知）
        Account fromAccount = this.findAccount(accountId);
        if (fromAccount == null) {
            throw new IllegalArgumentException("转出账户不存在");
        }
        Account toAccount = this.findAccount(toAccountId);
        if (toAccount == null) {
            throw new IllegalArgumentException("转入账户不存在");
        }
        if (accountId == toAccountId) {
            throw new IllegalArgumentException("不能给自己转账");
        }
        if (money <= 0) {
            throw new IllegalArgumentException("转账金额必须大于0");
        }

        log.info("转账前 - fromBalance: {}, toBalance: {}", fromAccount.getBalance(), toAccount.getBalance());

        // 使用 String 方式创建 BigDecimal，避免 double 精度问题
        java.math.BigDecimal balance = new java.math.BigDecimal(String.valueOf(fromAccount.getBalance()));
        java.math.BigDecimal transferAmount = new java.math.BigDecimal(String.valueOf(money));
        log.info("BigDecimal - balance: {}, transferAmount: {}", balance, transferAmount);

        if (balance.compareTo(transferAmount) < 0) {
            throw new BalanceNotSufficientException(ResultCode.PARAM_ERROR);
        }

        // 使用 BigDecimal 进行精确计算并保留2位小数
        java.math.BigDecimal fromBalanceBd = new java.math.BigDecimal(String.valueOf(fromAccount.getBalance()));
        java.math.BigDecimal toBalanceBd = new java.math.BigDecimal(String.valueOf(toAccount.getBalance()));
        java.math.BigDecimal moneyBd = new java.math.BigDecimal(String.valueOf(money));

        // 计算转账后余额，确保不会因浮点数精度变成负数
        java.math.BigDecimal fromNewBalanceBd = fromBalanceBd.subtract(moneyBd);
        log.info("计算后 - fromNewBalanceBd(before check): {}", fromNewBalanceBd);

        // 如果结果为负数或极小值，设为0（防止精度问题）
        if (fromNewBalanceBd.compareTo(java.math.BigDecimal.ZERO) < 0) {
            log.warn("转账后余额为负数，强制设为0");
            fromNewBalanceBd = java.math.BigDecimal.ZERO;
        }
        double fromNewBalance = fromNewBalanceBd.setScale(2, java.math.RoundingMode.DOWN).doubleValue();
        log.info("转换后 - fromNewBalance: {}", fromNewBalance);

        java.math.BigDecimal toNewBalanceBd = toBalanceBd.add(moneyBd);
        double toNewBalance = toNewBalanceBd.setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();

        // 更新转出账户余额
        log.info("更新转出账户余额 - accountId: {}, newBalance: {}", accountId, fromNewBalance);

        // 更新前再次确认余额足够（防止并发问题）
        Account beforeUpdate = this.findAccount(accountId);
        log.info("更新前余额确认 - accountId: {}, currentBalance: {}", accountId, beforeUpdate.getBalance());
        if (beforeUpdate.getBalance() < money) {
            throw new BalanceNotSufficientException(ResultCode.PARAM_ERROR);
        }

        this.accountdao.update(accountId, fromNewBalance);

        // 记录转出流水 - 使用 TRANSFER 类型
        OpRecord withdrawRecord = new OpRecord();
        withdrawRecord.setAccountId(accountId);
        withdrawRecord.setOpMoney(-money);
        withdrawRecord.setOpType(OpType.TRANSFER);
        withdrawRecord.setTransferId(toAccountId);
        withdrawRecord.setRemark(remark);
        this.opRecordDao.insertOpRecord(withdrawRecord);

        // 更新转入账户余额
        this.accountdao.update(toAccountId, toNewBalance);

        // 记录转入流水 - 使用 TRANSFER 类型
        OpRecord depositRecord = new OpRecord();
        depositRecord.setAccountId(toAccountId);
        depositRecord.setOpMoney(money);
        depositRecord.setOpType(OpType.TRANSFER);
        depositRecord.setTransferId(accountId);
        depositRecord.setRemark(remark);
        this.opRecordDao.insertOpRecord(depositRecord);

        // 重新查询转出账户，确认余额更新成功
        Account verifiedFromAccount = this.findAccount(accountId);
        java.math.BigDecimal verifiedBalance = new java.math.BigDecimal(String.valueOf(verifiedFromAccount.getBalance()));
        java.math.BigDecimal expectedBalance = new java.math.BigDecimal(String.valueOf(fromNewBalance));
        if (verifiedFromAccount == null || verifiedBalance.compareTo(expectedBalance) != 0) {
            throw new RuntimeException("转账失败：余额更新异常");
        }

        // 发送转账通知邮件（给转出账户）
        sendNotificationEmail(verifiedFromAccount, "转账", money, toAccountId);

        return verifiedFromAccount;
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