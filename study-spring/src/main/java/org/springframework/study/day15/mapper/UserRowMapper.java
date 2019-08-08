package org.springframework.study.day15.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.study.day15.pojo.TbUser;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author 周宁
 * @Date 2019-07-27 11:04
 */
public class UserRowMapper implements RowMapper {
    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        TbUser tbUser = new TbUser();
        tbUser.setId(rs.getString("id"));
        tbUser.setAge(rs.getInt("age"));
        tbUser.setBirth(rs.getDate("birth"));
        tbUser.setCareer(rs.getString("career"));
        tbUser.setMobile(rs.getString("mobile"));
        tbUser.setEmail(rs.getString("email"));
        tbUser.setIsActive(rs.getInt("isActive"));
        tbUser.setName(rs.getString("name"));
        tbUser.setRealName(rs.getString("realName"));
        tbUser.setPwd(rs.getString("pwd"));
        return tbUser;
    }
}
