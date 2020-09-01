package com.pal.service.impl;

import com.pal.entity.User;
import com.pal.factory.BeanFactory;
import com.pal.mapper.UserMapper;
import com.pal.mapper.impl.UserMapperImpl;
import com.pal.service.UserService;
import org.apache.ibatis.annotations.Param;


import javax.annotation.Resource;
import java.util.List;

/**
 * (User)表服务实现类
 *
 * @author pal
 * @since 2020-09-01 10:51:05
 */
public class UserServiceImpl implements UserService {

    /**
     * 传统声明方式，这时UserServiceImpl就与UserMapperImpl强耦合，变更mapper就要都改
     */
    //private final UserMapper mapper= new UserMapperImpl();

    /**
     * 工厂模式调用对象，还是要写死ID
     */
    //private UserMapper userMapper = (UserMapper) BeanFactory.getBean("UserMapper");

    /**
     * 只声明接口，不传id，用set传id
     * 此时就要先声明工厂了
     */
    private UserMapper userMapper;
    public void setUserMapper(UserMapper mapper) {
        this.userMapper = mapper;
    }

    List<User> list;
    @Override
    public void queryAll() {
        list = userMapper.queryAll();
        System.out.println("当前状态：");
        for (User user : list) {
            System.out.println(user);
        }
    }

    @Override
    public void transfer(String fromUser, String toUser, int money) {
        int fromUserBalance = userMapper.getBalanceByUser(fromUser);
        int toUserBalance = userMapper.getBalanceByUser(toUser);

        //这样写会有事务问题
        userMapper.update(fromUser,fromUserBalance-money);
        userMapper.update(toUser,toUserBalance+money);

        list = userMapper.queryAll();
        System.out.println("交易完状态：");
        for (User user : list) {
            System.out.println(user);
        }
    }
}