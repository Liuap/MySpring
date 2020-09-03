package com.pal.mapper.impl;

import com.pal.entity.User;
import com.pal.mapper.UserMapper;
import com.pal.untils.ConnectionUtils;
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

    private ConnectionUtils connectionUtils;

    public void setConnectionUtils(ConnectionUtils connectionUtils) {
        this.connectionUtils = connectionUtils;
    }

    //反射获取对象时，会实例化类中对象，由于UserMapperImpl是bean级别引用，所以此时ConnectionUtils还没注入，直接用就报错
//    InputStream resourceAsStream;
//
//    {
//        try {
//            resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 构建sqlSessionFactory时可以设置连接，以此控制事务
//     */
//    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
//    SqlSession sqlSession = sqlSessionFactory.openSession(connectionUtils.getCurrentThreadConn());
//    UserMapper mapper = sqlSession.getMapper(UserMapper.class);

    @Override
    public List<User> queryAll() {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        SqlSession sqlSession = sqlSessionFactory.openSession(connectionUtils.getCurrentThreadConn());
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        List<User> users = mapper.queryAll();
        return users;
    }

    @Override
    public int insert(User user) {
        return 0;
    }

    @Override
    public int update(String username, int balance) {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        SqlSession sqlSession = sqlSessionFactory.openSession(connectionUtils.getCurrentThreadConn());
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        int update = mapper.update(username, balance);
        return update;
    }

    @Override
    public int getBalanceByUser(String username) {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        SqlSession sqlSession = sqlSessionFactory.openSession(connectionUtils.getCurrentThreadConn());
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        return mapper.getBalanceByUser(username);
    }



}
