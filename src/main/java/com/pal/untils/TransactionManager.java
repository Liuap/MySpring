package com.pal.untils;

import java.sql.SQLException;

/**
 * 手动事务管理
 * @author pal
 * @date 2020/9/1 4:45 下午
 */
public class TransactionManager {

    private TransactionManager(){

        }
        private static TransactionManager transactionManager = new TransactionManager();

        public static TransactionManager getInstance(){
            return transactionManager;
        }

    public void beginTransaction() throws SQLException {
        ConnectionUtils.getInstance().getCurrentThreadConn().setAutoCommit(false);
    }

    public void commit() throws SQLException {
        ConnectionUtils.getInstance().getCurrentThreadConn().commit();
    }

    public void rollback() throws SQLException {
        ConnectionUtils.getInstance().getCurrentThreadConn().rollback();
    }
}
