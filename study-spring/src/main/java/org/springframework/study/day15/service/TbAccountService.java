package org.springframework.study.day15.service;

import org.springframework.study.day15.pojo.TbAccount;

import java.util.List;

/**
 * @author 周宁
 * @Date 2019-07-29 9:44
 */
public interface TbAccountService {

    List<TbAccount> querAll()throws Exception;

    List<TbAccount> queryByIds(List<Integer> ids)throws Exception;

    int count()throws Exception;

    void insert(List<TbAccount> tbAccounts)throws Exception;

    void update(TbAccount tbAccount)throws Exception;

    void save(Integer id,String userName,String realName)throws Exception;

}
