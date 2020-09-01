package com.pal.factory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用反射根据xml生产对象
 * @author pal
 * @date 2020/9/1 1:51 下午
 */
public class BeanFactory {

    /**
     * 1. 读取xml，利用反射实例化对象，存储在map
     * 2. 对外提供获取实例化对象的接口（根据id）
     */

    private static Map<String,Object> map = new HashMap<>();

    static {
        // 完成第一步
        InputStream resourceAsStream = BeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");
        SAXReader saxReader = new SAXReader();
        // 获取数据，实例化对象
        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            //获取所有bean标签的元素形成list
            List<Element> beanList = rootElement.selectNodes("//bean");
            for (Element element : beanList) {
                // id = "UserMapper" class = "com.pal.mapper.impl.UserMapperImpl"
                String id = element.attributeValue("id");
                String aClass = element.attributeValue("class");

                //利用反射获取对象
                Class<?> aClass1 = Class.forName(aClass);
                Object instance = aClass1.newInstance();

                map.put(id,instance);
            }

            //实例化完成则检查xml中的依赖关系，将需要的依赖作为参数放入set达到注入
            List<Element> propertyList = rootElement.selectNodes("//property");
            //解析property获取父元素
            for (Element element : propertyList) { //<property name="UserMapper" ref="UserMapper"/>
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
                    if (method.getName().equalsIgnoreCase("set"+name)){
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
     * 根据id获取对象:DI
     * @param id
     * @return
     */
    public static Object getBean(String id){

        return map.get(id);
    }

}
