package com.pal.mapper.impl;

import com.pal.entity.User;
import com.pal.mapper.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *    因为Mybatis用动态代理实现数据库操作，不需要实现类，因为没有是手写spring，所以没法注入myabtis接口，只能写"实现类"
 *    这个类正常是不需要的，只是为了做IOC才这么写
 * @author pal
 * @date 2020/9/1 11:21 上午
 */
public class UserMapperImpl implements UserMapper{
    InputStream resourceAsStream;

    {
        try {
            resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);

    @Override
    public List<User> queryAll() {
        List<User> users = mapper.queryAll();
        sqlSession.close();
        return users;
    }

    @Override
    public int insert(User user) {
        return 0;
    }

    @Override
    public int update(String username, int balance) {
        int update = mapper.update(username, balance);
        sqlSession.commit();
        return update;
    }

    @Override
    public int getBalanceByUser(String username) {

        return mapper.getBalanceByUser(username);
    }



}
