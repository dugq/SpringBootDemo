package com.example.proxy.test;

import com.example.proxy.Person;
import com.example.proxy.Xiaoming;
import com.example.proxy.cglib.MyCglibAopProxyFactory;
import com.example.proxy.support.MyAdvisorSupport;
import com.example.proxy.support.MyAop;
import org.springframework.cglib.core.DebuggingClassWriter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dugq on 2018/4/27.
 */
public class TestCglibAopProxyFactory {
    public static void main(String[] args) {
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "D:\\class");
        MyAop aop1 = new MyAop() {
            @Override
            public void before(MyAdvisorSupport support) {
                System.out.println("i am 傻子");
            }
        };
        MyAop aop2 = new MyAop() {
            @Override
            public Object around(MyAdvisorSupport support, Method method, Object[] args) {
                System.out.println("around before ");
                Object around = super.around(support, method, args);
                System.out.println("around after ");
                return around;
            }
        };

        MyAop aop3 = new MyAop() {
            @Override
            public void after(MyAdvisorSupport support) {
                System.out.println("嗯哼~~~ ");
            }
        };

        List list = new ArrayList();
        list.add(aop1);
        list.add(aop2);
        list.add(aop3);
        Person xiaoming = new Xiaoming();
        xiaoming =new MyCglibAopProxyFactory<Xiaoming>(list,xiaoming).getProxy();
        xiaoming.say();
    }
}
