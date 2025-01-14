简单代码模拟Spring的Bean加载流程，主要功能点如下：

  1.支持使用@ComponentScan配置 EasySpring 需要扫描的 Bean 路径

  2.支持使用@Component配置需要注册到 EasySpring 的 Bean，并且支持配置 Bean 的注册名称

  3.支持使用@Scope配置 Bean 的作用域，包括 singleton 和 prototype

  4.支持使用@Autowired实现 Bean 的依赖注入

  5.支持使用BeanNameAware接口实现 setBeanName 的回调

  6.支持使用BeanPostProcessor接口实现初始化前和初始化后的逻辑
  
  7.支持使用InitializingBean接口实现初始化逻辑
