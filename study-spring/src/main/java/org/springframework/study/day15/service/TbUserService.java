package org.springframework.study.day15.service;

import org.springframework.study.day15.pojo.TbUser;

import java.util.List;

/**
 * @author 周宁
 * @Date 2019-07-27 11:08
 */
public interface TbUserService {

    List<TbUser> getUsers();

    void delete();
}
