package com.my.service;

import com.spring.annotations.Autowired;
import com.spring.annotations.Component;
import com.spring.annotations.Scope;
import com.spring.interfaces.BeanNameAware;
import com.spring.interfaces.InitializingBean;

@Component("userService")
@Scope("singleton")
public class UserService implements InitializingBean, UserInterface, BeanNameAware {

    @Autowired
    private OrderService orderService;

    private String beanName;

    @Override
    public void setBeanName(String name) {
        System.out.println("UserService===开始执行beanName回调");
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("Userservice===初始化");
    }

    @Override
    public void test() {
        System.out.println();
        System.out.println("UserService===test");
        System.out.println("UserService===beanName:" + beanName);
        System.out.println("UserService===orderService:" + orderService);
    }
}
