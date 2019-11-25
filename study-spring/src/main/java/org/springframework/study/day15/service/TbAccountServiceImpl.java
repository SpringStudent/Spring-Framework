package org.springframework.study.day15.service;

import com.gysoft.jdbc.bean.Criteria;
import com.gysoft.jdbc.bean.SQL;
import org.springframework.study.day15.dao.TbAccountDao;
import org.springframework.study.day15.pojo.TbAccount;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 周宁
 * @Date 2019-07-29 9:45
 */
public class TbAccountServiceImpl implements  TbAccountService{

    private TbAccountDao tbAccountDao;

    public void setTbAccountDao(TbAccountDao tbAccountDao) {
        this.tbAccountDao = tbAccountDao;
    }

    @Override
    public List<TbAccount> querAll() throws Exception {
        return tbAccountDao.queryAll();
    }

    @Override
    public List<TbAccount> queryByIds(List<Integer> ids) throws Exception {
        return tbAccountDao.queryWithCriteria(new Criteria().in("id",ids));
    }

    @Override
    public int count() throws Exception {
        return tbAccountDao.queryIntegerWithSql(new SQL().select("count(1)").from(TbAccount.class));
    }

    @Override
    public void insert(List<TbAccount> tbAccounts) throws Exception {
        tbAccountDao.batchSave(tbAccounts);
    }

    @Override
    public void update(TbAccount tbAccount) throws Exception {
        tbAccountDao.update(tbAccount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Integer id, String userName, String realName) throws Exception {
        tbAccountDao.insertWithSql(new SQL().insert_into(TbAccount.class,"id,userName,realName").values(id,userName,realName));
        throw new Exception("哈喽");
    }
}
