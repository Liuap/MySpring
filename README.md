# 手写实现IOC与AOP思想

> 源码参照github：https://github.com/Liuap/MySpring

程序入口:MybatisTest.mapperTest()

## 1. IOC

- 在IOC实现时，对beans.xml文件的扫描与将对象放入容器（Map）是自上而下进行，更关键的是先'bean'标签级别，然后再是property级别

- 先在容器(Map)里放入bean级别的反射对象，然后根据property获取ref所指的对象(map.get(ref))注入(set方法)到父级bean

  ```java
  private static Map<String,Object> map = new HashMap<>();
  
  static {
      // 完成第一步
      InputStream resourceAsStream = BeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");
      SAXReader saxReader = new SAXReader();
  
      try {
          Document document = saxReader.read(resourceAsStream);
          Element rootElement = document.getRootElement();
          //获取所有bean标签的元素形成list
          List<Element> beanList = rootElement.selectNodes("//bean");
          for (Element element : beanList) {
              // id = "userMapper" class = "com.pal.mapper.impl.UserMapperImpl"
              String id = element.attributeValue("id");
              String aClass = element.attributeValue("class");
  
              //利用反射获取对象
              Class<?> aClass1 = Class.forName(aClass);
              Object instance = aClass1.newInstance();
  
              // 此时id为小写首字母，为了和set方法的对象对应，instance是根据class反射得到的对象
              map.put(id,instance);
          }
  
          //实例化完成则检查xml中的依赖关系，将需要的依赖作为参数放入set达到注入
          List<Element> propertyList = rootElement.selectNodes("//property");
          //解析property获取父元素 <property name="UserMapper" ref="UserMapper"/>
          for (Element element : propertyList) { 
              String name = element.attributeValue("name");
              String ref = element.attributeValue("ref");
  
              //需要被注入的父级bean
              Element parent = element.getParent();
              //根据ID调用父级对象
              String parentId = parent.attributeValue("id");
              Object parentObject = map.get(parentId);
              //反射得到方法数组,然后找到对应name的父对象的set方法，反射调用,参数为对象，从前边够早的对象map获取
              Method[] methods = parentObject.getClass().getMethods();
              for (Method method : methods) {
                  //找到set方法
                  if (method.getName().equalsIgnoreCase("set"+name)){
                      //这时，反射调用父级bean的set方法，然后从已有bean级别的map中找ref的对象，然后注入父级bean
                      method.invoke(parentObject,map.get(ref));
                  }
              }
  
              map.put(parentId,parentObject);
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
  /**
   * 根据id获取对象:注意这里的id要与beans.xml的id对应，大小写敏感
   * @param id
   * @return
   */
  public static Object getBean(String id){
      return map.get(id);
  }
  ```

- Beans.xml文件要与BeanFactory和各个类中的set注入方式相对应

  ```xml
  <beans>
      <!--id标识对象，class是类的全限定类名-->
      <!--  id和ref必须小写，因为，最开始将id作为map的id来封装容器，然后根据property在对依赖进行注入，此时有map.get(ref)来对注入对象进行检索，大写找不到，就没法更新注入  -->
      <bean id="connectionUtils" class="com.pal.untils.ConnectionUtils"/>
  
      <bean id="userMapper" class="com.pal.mapper.impl.UserMapperImpl">
          <property name="ConnectionUtils" ref="connectionUtils"/>
      </bean>
  
      <bean id="userService" class="com.pal.service.impl.UserServiceImpl">
          <!-- name用来定位set方法，ref为真正的引用，即set方法的参数 -->
          <property name="UserMapper" ref="userMapper"/>
      </bean>
  
      <bean id="transactionManager" class="com.pal.untils.TransactionManager">
          <property name="ConnectionUtils" ref="connectionUtils"/>
      </bean>
  
      <bean id="proxyFactory" class="com.pal.factory.ProxyFactory">
          <property name="TransactionManager" ref="transactionManager"/>
      </bean>
  </beans>
  ```

  ```java
  /**
   * 注入方式
   */
  public class TransactionManager {
  
      private ConnectionUtils connectionUtils;
  
      public void setConnectionUtils(ConnectionUtils connectionUtils) {
          this.connectionUtils = connectionUtils;
      }
    // 单例模式相对跟耦合
  //    private TransactionManager(){
  //
  //        }
  //        private static TransactionManager transactionManager = new TransactionManager();
  //
  //        public static TransactionManager getInstance(){
  //            return transactionManager;
  //        }
   ...
  }
  ```

- 因为反射会实例化成员变量（未验证），所以要注入的对象不能在一级bean类中使用非方法体里直接使用，

  ```java
  /**
   *    这个类正常是不需要的，只是为了做不想再写一次mybatis底层才这么写
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
        // 方法体里不会报错
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
  
  ...
  
  
  }
  
  ```

## 2. AOP

- 核心思想：动态代理（JDK或Cglib）

- 以Service层实现事务控制为例，不改变原有业务代码的情况下实现横切增强

  - 事务问题，两次update之间出现异常或者线程切换时，前一次如果默认提交事务，后一次未执行就会造成数据不一致

  ```
  //这样写会有事务问题
  userMapper.update(fromUser,fromUserBalance-money);
  // int i = 1/0;
  userMapper.update(toUser,toUserBalance+money);
  ```

  - 1. 先使线程绑定Connection，本方法内的所有事务都由一个Connection完成

    ```java
    // 存储当前线程的连接
    private ThreadLocal<Connection> threadLocal = new ThreadLocal<>();
    
    /**
     * 以下用mybatis获取连接仅为方便，逻辑上是不该这么写的
     */
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
    
    /**
     * 从当前线程获取连接
     * Mapper使用 SqlSession sqlSession = sqlSessionFactory.openSession(connectionUtils.getCurrentThreadConn());
     */
    public Connection getCurrentThreadConn() {
        /**
         * 判断当前线程中是否已经绑定连接，如果没有绑定，需要从连接池获取一个连接绑定到当前线程
         */
        Connection connection = threadLocal.get();
        if(connection == null) {
            // 从连接池拿连接并绑定到线程
            connection = sqlSession.getConnection();
            // 绑定到当前线程
            threadLocal.set(connection);
        }
        return connection;
    }
    ```

  - 2. 常规事务控制写法：这样多个业务方法就有很多冗余代码

    ```java
    try{
      //关闭自动提交事务
    	connectionUtils.getCurrentThreadConn().setAutoCommit(false);
    	//业务代码，增、改、删
    	...
    	//提交
    	connectionUtils.getCurrentThreadConn().commit();
    } catch (Exception throwables) {
      //出错：回滚
      connectionUtils.getCurrentThreadConn().rollback();
      throw throwables;
    }
    ```

  - 3. 使用动态代理实现横切增强（方法执行前后增强）

    ```java
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
     * 使用cglib动态代理生成代理对象,不需要对象的接口
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
    ```

    


