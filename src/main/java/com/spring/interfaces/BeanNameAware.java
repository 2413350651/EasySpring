package com.spring.interfaces;

public interface BeanNameAware {

    /**
     * 回调设置BeanName属性
     *
     * @param name
     */
    void setBeanName(String name);

}
