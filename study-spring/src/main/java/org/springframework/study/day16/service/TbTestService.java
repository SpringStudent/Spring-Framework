package org.springframework.study.day16.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 周宁
 * @Date 2019-07-29 14:52
 */
@Transactional(propagation = Propagation.REQUIRED)
public interface TbTestService {
    void save(String context) throws Exception;

    void insert(String context) throws Exception;

    void create(String context) throws Exception;
}
