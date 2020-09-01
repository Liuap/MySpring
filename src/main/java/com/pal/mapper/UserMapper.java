package com.pal.mapper;

import com.pal.entity.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (User)表数据库访问层
 *
 * @author pal
 * @since 2020-09-01 10:52:05
 */
public interface UserMapper {

    /**
     *
     * @return 对象列表
     */
    List<User> queryAll();

    /**
     * 新增数据
     *
     * @param user 实例对象
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 更新余额
     * @param username
     * @param balance
     * @return
     */
    int update(@Param("username") String username,@Param("balance") int balance);

    /**
     * 根据用户名查余额
     * @param username
     * @return
     */
    int getBalanceByUser(@Param("username") String username);


}