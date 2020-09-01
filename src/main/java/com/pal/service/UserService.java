package com.pal.service;

import com.pal.entity.User;
import java.util.List;

/**
 * (User)表服务接口
 *
 * @author makejava
 * @since 2020-09-01 10:50:59
 */
public interface UserService {

    /**
     * 查询所有
     */
    void queryAll();

    /**
     * 转账实现方法
     * @param fromUser
     * @param toUser
     * @param money
     * @throws Exception
     */
    void transfer(String fromUser,String toUser,int money) throws Exception;

}