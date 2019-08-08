package org.springframework.study.day15.dao;

import com.gysoft.jdbc.dao.EntityDaoImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.study.day15.pojo.TbAccount;

/**
 * @author 周宁
 * @Date 2019-07-29 9:44
 */
public class TbAccountDaoImpl  extends EntityDaoImpl<TbAccount,Integer> implements TbAccountDao {

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
}
