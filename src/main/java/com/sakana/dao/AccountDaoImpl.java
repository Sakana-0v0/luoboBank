package com.sakana.dao;

import com.sakana.bean.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class AccountDaoImpl implements AccountDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public AccountDaoImpl(DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

    @Override
    public int insert(double money) {
        String sql = "insert into accounts (balance) values (?)";
        //需求: 我们想获取最新的自增id，而不是影响的行数
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"accountid"}); //这里是主键的名字
            ps.setDouble(1, money);   //这里指的是?参数的设置
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int insertWithPassword(double money, String passwordHash) {
        String sql = "insert into accounts (balance, password_hash) values (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"accountid"});
            ps.setDouble(1, money);
            ps.setString(2, passwordHash);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int insertWithPasswordAndEmail(double money, String passwordHash, String email) {
        String sql = "insert into accounts (balance, password_hash, email) values (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"accountid"});
            ps.setDouble(1, money);
            ps.setString(2, passwordHash);
            ps.setString(3, email);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public void update(int accountId, double money) {
        // 业务上理解的话, money指的是最新金额( 它到底是存，还是取，应该在业务层决定 )
        this.jdbcTemplate.update("update accounts set balance=? where accountid=?", money, accountId);
    }

    @Override
    public void delete(int accountId) {
        this.jdbcTemplate.update("delete from accounts where accountid=?", accountId);
    }

    @Override
    public int findCount() {
        return this.jdbcTemplate.queryForObject("select count(*) from accounts", Integer.class);  //Integer.class指的是返回值类型
    }

    @Override
    public List<Account> findAll() {
        List<Account> list = this.jdbcTemplate.query(
                "select * from accounts ",
                (resultSet, rowNum) -> {
                    Account account = new Account();
                    account.setAccountId(resultSet.getInt("accountid"));
                    account.setBalance(resultSet.getDouble("balance"));
                    account.setPasswordHash(resultSet.getString("password_hash"));
                    account.setEmail(resultSet.getString("email"));
                    account.setStatus(resultSet.getObject("status") != null ? resultSet.getInt("status") : 0);
                    return account;
                });
        return list;
    }

    @Override
    public Account findById(int accountId) {
        return this.jdbcTemplate.queryForObject("select * from accounts where accountid=?",
                (resultSet, rowNum) -> {
                    Account account = new Account();
                    account.setAccountId(resultSet.getInt("accountid"));
                    account.setBalance(resultSet.getDouble("balance"));
                    account.setPasswordHash(resultSet.getString("password_hash"));
                    account.setEmail(resultSet.getString("email"));
                    account.setStatus(resultSet.getObject("status") != null ? resultSet.getInt("status") : 0);
                    return account;
                }, accountId);

    }
}
