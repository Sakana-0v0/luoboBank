package com.sakana.dao;

import com.sakana.bean.Account;

import java.util.List;


public interface AccountDao {


    public int insert(double money);

    public int insertWithPassword(double money, String passwordHash);

    public int insertWithPasswordAndEmail(double money, String passwordHash, String email);


    public void update(int accountId, double money);


    public void delete(int accountId);


    public int findCount();


    public List<Account> findAll();


    public Account findById(int accountId);
}
