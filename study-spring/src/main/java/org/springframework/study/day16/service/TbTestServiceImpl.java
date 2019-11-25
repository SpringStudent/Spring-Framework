package org.springframework.study.day16.service;

import com.gysoft.jdbc.bean.SQL;
import org.springframework.study.day16.dao.TbTestDao;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.naming.NoPermissionException;

/**
 * @author 周宁
 * @Date 2019-07-29 14:53
 */
public class TbTestServiceImpl implements TbTestService {

    private TbTestDao tbTestDao;

    private TransactionTemplate transactionTemplate;

    public void setTbTestDao(TbTestDao tbTestDao) {
        this.tbTestDao = tbTestDao;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void save(String context) throws Exception {
        tbTestDao.insertWithSql(new SQL().insert("id", "context").values(null, context));
        throw new RuntimeException("gun");
    }

    @Override
    public void insert(String context) throws Exception {
        tbTestDao.insertWithSql(new SQL().insert("id", "context").values(null, context));
        throw new RuntimeException("干");
    }

    @Override
    public void create(final String context) throws Exception {
        transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {

                try {
                    tbTestDao.insertWithSql(new SQL().insert("id", "context").values(null, context));
                    System.out.println(1/0);
                    return null;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    return null;
                }
            }
        });
    }
}
