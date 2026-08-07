package com.sakana.web.controller;

import cn.hutool.core.bean.BeanUtil;
import com.sakana.bean.Account;
import com.sakana.bean.OpRecord;
import com.sakana.service.BankService;
import com.sakana.web.vo.BankAccountVO;
import com.sakana.web.vo.OpRecordVo;
import com.sakana.web.vo.ResultVo;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log
public class BankController {
    @Autowired
    private BankService bankService;


    @PostMapping("/account/open")
    public ResultVo<Account> open(@RequestParam("money") double money,
                                   @RequestParam("password") String password,
                                   @RequestParam(value = "email", required = false) String email) {
        Account newAccount = bankService.openAccount(money, password, email);
        log.info("开户成功:" + newAccount);
        return ResultVo.success(newAccount);
    }


    @PostMapping("/account/deposite")
    public ResultVo<Account> deposite(@RequestBody BankAccountVO vo) {
        return ResultVo.success(bankService.deposite(vo.getAccountId(), vo.getOpMoney(), null, vo.getRemark()));
    }

    @PostMapping("/account/withdraw")
    public ResultVo<Account> withdraw(@RequestBody BankAccountVO vo) {
        return ResultVo.success(bankService.withdraw(vo.getAccountId(), vo.getOpMoney(), null, vo.getRemark()));
    }

    @PostMapping("/account/transfer")
    public ResultVo<Account> transfer(@RequestBody BankAccountVO vo) {
        return ResultVo.success(bankService.transfer(vo.getAccountId(), vo.getOpMoney(), vo.getTargetAccountId(), vo.getRemark()));
    }

    @GetMapping("/account/{accountId}")
    public ResultVo<Account> getAccount(@PathVariable("accountId") Integer accountId) {
        return ResultVo.success(this.bankService.findAccount(accountId));
    }

    @GetMapping("/account/oprecords/{accountId}")
    public ResultVo<List<OpRecordVo>> findOprecords(@PathVariable("accountId") Integer accountId) {
        List<OpRecord> list = this.bankService.findOpRecordByAccountId(accountId);
        // POJO转为VO
        List<OpRecordVo> voList =
                BeanUtil.copyToList(list, OpRecordVo.class);
        return ResultVo.success(voList);

    }

}
