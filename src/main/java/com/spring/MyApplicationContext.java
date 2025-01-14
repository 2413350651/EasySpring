package com.spring;

import com.spring.annotations.Autowired;
import com.spring.annotations.Component;
import com.spring.annotations.ComponentScan;
import com.spring.annotations.Scope;
import com.spring.interfaces.BeanNameAware;
import com.spring.interfaces.BeanPostProcessor;
import com.spring.interfaces.InitializingBean;
import com.spring.pojo.BeanDefinition;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyApplicationContext {

    private Class configClass;
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    private Map<String, Object> singletonObjects = new HashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public MyApplicationContext(Class configClass) throws Exception {
        this.configClass = configClass;

        boolean componentScanFlag = configClass.isAnnotationPresent(ComponentScan.class);
        if (componentScanFlag) {
            scan();
        }

        // 创建Bean
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            BeanDefinition beanDefinition = beanDefinitionEntry.getValue();
            // 单例模式的Bean在获取MyApplicationContext时创建，原型模式的Bean在使用时创建
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    /**
     * Bean的扫描与注册
     */
    private void scan() {
        // 获取@ComponentScan配置的Bean扫描路径
        String componentScanPath = ((ComponentScan) configClass.getAnnotation(ComponentScan.class)).value(); // == com.my.service
        String componentScanLocalPath = componentScanPath.replace(".", "/"); // == com/my/service

        // 获取自定义类加载器
        ClassLoader appClassLoader = MyApplicationContext.class.getClassLoader();

        // 类路径拼接上componentScanLocalPath，获取class文件的实际路径
        URL resource = appClassLoader.getResource(componentScanLocalPath);
        File classFilePath = new File(resource.getFile()); // == D:\study\mycode\GitCode\EasySpring\target\classes\com\my\service

        if (classFilePath.isDirectory()) {
            File[] classFiles = classFilePath.listFiles();
            for (File classFile : classFiles) {
                String classFileName = classFile.getName().replaceAll(".class", ""); // UserService.class -> UserService

                // 对目录下的类进行加载
                Class<?> loadClass = null;
                try {
                    loadClass = appClassLoader.loadClass(componentScanPath + "." + classFileName);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                // 判断是否需要注册
                if (loadClass.isAnnotationPresent(Component.class)) {

                    // 判断被加载的类是否实现了BeanPostProcessor接口，如果实现了则将其添加到List中用于后续初始化前后操作
                    if (BeanPostProcessor.class.isAssignableFrom(loadClass)) {
                        BeanPostProcessor instance = null;
                        try {
                            instance = (BeanPostProcessor) loadClass.getConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        beanPostProcessorList.add(instance);
                    }

                    // 默认的beanName为类名的首字母小写，支持使用自定义的BeanName
                    String beanName = classFileName.substring(0, 1).toLowerCase() + classFileName.substring(1);
                    if (!loadClass.getAnnotation(Component.class).value().isEmpty()) {
                        beanName = loadClass.getAnnotation(Component.class).value();
                    }

                    // 对Bean进行定义，封装对应的POJO类
                    BeanDefinition beanDefinition = new BeanDefinition();
                    beanDefinition.setType(loadClass);

                    // Bean默认设置为单例模式，支持自定义使用原型模式
                    beanDefinition.setScope("singleton");
                    beanDefinition.setLazy(false);

                    // 判断Bean是否配置了@Scope注解
                    boolean scopeFlag = loadClass.isAnnotationPresent(Scope.class);
                    if (scopeFlag) {
                        Scope scopeAnnotation = loadClass.getAnnotation(Scope.class);
                        if (!"singleton".equals(scopeAnnotation.value())) {
                            beanDefinition.setScope(scopeAnnotation.value());
                            beanDefinition.setLazy(true);
                        }
                    }

                    // 将注册完成的Bean放入beanDefinitionMap
                    beanDefinitionMap.put(beanName, beanDefinition);
                }
            }
        }

    }

    /**
     * 实现Bean的创建，并模拟Spring实现Bean创建的生命周期
     * 实例化——>依赖注入——>Aware回调——>初始化前——>初始化——>初始化后——>动态代理（AOP）
     *
     * @param beanName
     * @param beanDefinition
     * @return
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class beanType = beanDefinition.getType();
        Object object = null;

        try {
            object = beanType.getConstructor().newInstance();
            Field[] declaredFields = beanType.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                // 如果配置了@Autowired注解则实现依赖注入功能
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    declaredField.setAccessible(true);
                    Object autoWiredBean = getBean(declaredField.getName());
                    declaredField.set(object, autoWiredBean);
                    System.out.println("开始进行依赖注入" + autoWiredBean);
                }
            }

            // 调用Aware回调方法
            if (object instanceof BeanNameAware) {
                ((BeanNameAware) object).setBeanName(beanName);
            }

            // 调用初始化前方法，自定义的BeanPostProcessor无需调用
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                if (!"myBeanPostProcessor".equals(beanName)) {
                    object = beanPostProcessor.postProcessBeforeInitialization(object, beanName);
                }
            }

            // 调用初始化接口
            if (object instanceof InitializingBean) {
                ((InitializingBean) object).afterPropertiesSet();
            }

            // 调用初始化后，自定义的BeanPostProcessor无需调用
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                if (!"myBeanPostProcessor".equals(beanName)) {
                    object = beanPostProcessor.postProcessAfterInitialization(object, beanName);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return object;
    }

    /**
     * 从EasySpring中获取Bean
     *
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) {
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new NullPointerException();
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if ("singleton".equals(beanDefinition.getScope())) {
            Object singletonBean = singletonObjects.get(beanName);
            if (singletonBean == null) {
                //如果@Autowired的Bean从singletonObjects获取不到，则对其进行创建
                singletonBean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, singletonBean);
            }
            return singletonBean;
        } else {
            return createBean(beanName, beanDefinition);
        }
    }

}
