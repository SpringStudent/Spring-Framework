package org.springframework.study.day15.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.study.day15.mapper.UserRowMapper;
import org.springframework.study.day15.pojo.TbUser;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author 周宁
 * @Date 2019-07-27 11:08
 */
public class TbUserServiceImpl implements TbUserService {

    private JdbcTemplate jdbcTemplate;

    public  void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    @Override
    public List<TbUser> getUsers() {
        return jdbcTemplate.query("select * from tb_user",new UserRowMapper());
    }

    @Override
    public void delete() {
        jdbcTemplate.update("delete from tb_user where id = '1'");
        jdbcTemplate.update("delete from tb_user where id = '2'");
    }
}
