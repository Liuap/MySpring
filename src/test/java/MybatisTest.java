import com.pal.entity.User;
import com.pal.factory.BeanFactory;
import com.pal.mapper.UserMapper;
import com.pal.service.UserService;
import com.pal.service.impl.UserServiceImpl;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;


/**
 * @author pal
 * @date 2020/9/1 11:02 上午
 */
public class MybatisTest {

    @Test
    public void connectTest() throws IOException {
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        //List<User> list = sqlSession.selectList("com.pal.mapper.UserMapper.queryAll");
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        List<User> list = mapper.queryAll();
        for (User user : list) {
            System.out.println(user);
        }
        sqlSession.close();
    }

    @Test
    public void mapperTest(){
        // 再service层 BeanFactory.getBean("UserMapper")显式get时
        // UserService service = new UserServiceImpl();

        // 用只声明对象时，就要在此get Service对象，此时会根据xml注入mapper给service，相当于在更高层注入service
        UserService service = (UserService) BeanFactory.getBean("UserService");
        try {
            service.transfer("Tim","Bob",100);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
