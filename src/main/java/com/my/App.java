package com.my;

import com.my.config.AppConfig;
import com.my.service.UserInterface;
import com.spring.MyApplicationContext;

/**
 * Hello MySpring
 */
public class App {
    public static void main(String[] args) throws Exception {
        MyApplicationContext myApplicationContext = new MyApplicationContext(AppConfig.class);
        UserInterface userService = (UserInterface) myApplicationContext.getBean("userService");
        userService.test();
    }
}
