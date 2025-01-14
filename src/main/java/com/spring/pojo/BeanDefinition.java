package com.spring.pojo;

public class BeanDefinition {

    private Class type; //需要注册的类
    private String scope; //类的作用域
    private boolean isLazy; //是否懒加载

    public boolean isLazy() {
        return isLazy;
    }

    public void setLazy(boolean lazy) {
        isLazy = lazy;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "type=" + type +
                ", scope='" + scope + '\'' +
                ", isLazy=" + isLazy +
                '}';
    }
}
