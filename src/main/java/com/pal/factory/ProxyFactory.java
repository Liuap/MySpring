package com.pal.factory;

import com.pal.entity.User;
import com.pal.untils.TransactionManager;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author pal
 *
 * 代理对象工厂：生成代理对象的
 */

public class ProxyFactory {

    private TransactionManager transactionManager;

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

//    private ProxyFactory(){
//
//    }
//
//    private static ProxyFactory proxyFactory = new ProxyFactory();
//
//    public static ProxyFactory getInstance() {
//        return proxyFactory;
//    }




    /**
     * Jdk动态代理
     * @param obj  委托对象
     * @return   代理对象
     */
    public Object getJdkProxy(Object obj) {

        // 获取代理对象
        return  Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    //真正在执行的时候，service.transfer("Tim","Bob",100); proxy = service,method = transfer
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object result = null;
                        try {
                            transactionManager.beginTransaction();

                            result = method.invoke(obj, args);

                            transactionManager.commit();


                        } catch (Exception throwables) {
                            transactionManager.rollback();
                            throw throwables;
                        }
                        return result;
                    }

                });

    }


    /**
     * 使用cglib动态代理生成代理对象
     * @param obj 委托对象
     * @return
     */
    public Object getCglibProxy(Object obj) {
        return  Enhancer.create(obj.getClass(), new MethodInterceptor() {
            @Override
            // 参数：代理对象的引用、动态执行的方法、参数、方法封装 
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                Object result = null;
                try {
                    transactionManager.beginTransaction();

                    result = method.invoke(obj, objects);
                    transactionManager.commit();


                } catch (Exception throwables) {
                    transactionManager.rollback();
                    throw throwables;
                }
                return result;
            }
        });
    }
}
