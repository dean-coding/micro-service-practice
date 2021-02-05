## cloud gateway Started


## 环境

| 工具    | 版本或描述                |
| ----- | -------------------- |
| JDK   | 1.8                  |
| Maven | 3.x                  |
| spring-cloud-dependencies | Greenwich.SR2 |
| spring-boot-starter | 2.1.6.RELEASE |
| spring-cloud-starter-netflix-eureka-client | 2.1.2.RELEASE |
| spring-cloud-starter-gateway | 2.1.2.RELEASE |


## 一 动态网关配置

### 1 maven 依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-consul-discovery</artifactId>
    </dependency>
     <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
</dependencies>
```

### 2 Route配置形式


#### 2.1 代码中配置(静态网关)

```
@Bean
@ConditionalOnProperty(name = "gateway.route.config.type", havingValue = "byCode", matchIfMissing = true)
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
            .route("resource", r -> r.path("/resource/**")
                    .filters(f -> f.stripPrefix(1))//去掉第一层前缀如果是/api/oauth这种 就stripPrefix(2)
                    .uri("lb://producer-service"))// Prevents cookie being sent downstream
            .build();
}
```

#### 2.2 yml中配置(静态网关)
```yaml
spring:
 ## 路由配置方式
    gateway:
      routes:
        - id: resource
          uri: lb://producer-service
          filters:
            - StripPrefix=1
          predicates:
            - Path=/resource/**
```

#### 2.3 默认动态路由配置(内存操作)

```yaml
## 启用 {@See org.springframework.cloud.gateway.actuate.GatewayControllerEndpoint}
management:
  endpoints:
    web:
      exposure:
        include: gateway,env
        
```
```
常用路由配置：
1 /gateway/routes 查询所有路由信息
2 /gateway/routes/{id} 根据路由id查询单个信息
3 /gateway/routes/{id} @PostMapping 新增一个路由信息
4 /gateway/routes/{id} @DeleteMapping 删除一个路由信息
```
> 示例： $ curl -X GET http://127.0.0.1:8068/actuator/gateway/routes

#### 查看全局过滤器
> http://127.0.0.1:8068/actuator/gateway/globalfilters

```json
{
    "org.springframework.cloud.gateway.filter.ForwardPathFilter@138a85d3": 0,
    "org.springframework.cloud.gateway.filter.ForwardRoutingFilter@6326c5ec": 2147483647,
    "org.springframework.cloud.gateway.filter.RemoveCachedBodyFilter@191f4d65": -2147483648,
    "org.springframework.cloud.gateway.filter.LoadBalancerClientFilter@7ab33ca8": 10100,
    "org.springframework.cloud.gateway.filter.AdaptCachedBodyGlobalFilter@108b121f": -2147482648,
    "org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter@7a2a7492": 10000,
    "org.springframework.cloud.gateway.filter.NettyWriteResponseFilter@21e484b": -1,
    "org.springframework.cloud.gateway.filter.WebsocketRoutingFilter@709d86a2": 2147483646,
    "org.springframework.cloud.gateway.filter.NettyRoutingFilter@704d3b00": 2147483647,
    "AuthorizeFilter@33e50ff2": 0,
    "org.springframework.cloud.gateway.filter.GatewayMetricsFilter@59baf2c7": 0
}
```

#### 2.4 自定义动态路由配置(持久化操作)


> 自定义route存储： RedisRouteDefinitionRepository implements RouteDefinitionRepository

> 自定义route管理： DynamicRouteService

## 二 聚合swagger

## 三 配置Filter

#### 简介

1 SpringCloud Gateway的Filter分类
 
 从作用范围可分为另外两种GatewayFilter 与 GlobalFilter

    GatewayFilter：应用到单个路由或者一个分组的路由上。自定义GatewayFilter又有两种实现方式：
        - 1 实现GatewayFilter接口
        - 2 自定义过滤器工厂（继承AbstractGatewayFilterFactory类）, 选择自定义过滤器工厂的方式，可以在配置文件中配置过滤器
    
    GlobalFilter：应用到所有的路由上
    
2 过滤器允许以某种方式修改传入的HTTP请求或传出的HTTP响应。过滤器的作用域为特定路由,Spring Cloud Gateway包含许多内置的GatewayFilter工厂。


#### 示例：

#### 1 GatewayFilterFactory配置

```java

/**
 * 在 spring.gateway.filters 指定时候，取前缀，示例：
 * CustomerGatewayFilterFactory
 * <p>
 * 配置为 Customer=true
 *
 * @author Dean
 * @date 2020-11-27
 */
@Slf4j
@Component
public class CustomerGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomerGatewayFilterFactory.Config> {

    private static final String COUNT_START_TIME = "countStartTime";

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("enabled");
    }

    public CustomerGatewayFilterFactory() {
        super(Config.class);
        log.info("Loaded GatewayFilterFactory [CustomerGatewayFilterFactory]");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!config.isEnabled()) {
                return chain.filter(exchange);
            }
            exchange.getAttributes().put(COUNT_START_TIME, System.currentTimeMillis());
            return chain.filter(exchange).then(
                    Mono.fromRunnable(() -> {
                        Long startTime = exchange.getAttribute(COUNT_START_TIME);
                        if (Objects.nonNull(startTime)) {
                            StringBuilder sb = new StringBuilder(exchange.getRequest().getURI().getRawPath())
                                    .append(": ")
                                    .append(System.currentTimeMillis() - startTime)
                                    .append("ms");
                            sb.append(" params:").append(exchange.getRequest().getQueryParams());
                            log.info(sb.toString());
                        }
                    })
            );
        };
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class Config {
        /**
         * 控制是否开启统计
         */
        private boolean enabled;

    }

}

```

> 在yml配置中，或者动态路由配置中配置filter，如下示例： `Customer=true`

```yaml
  ## 路由配置方式
    gateway:
      routes:
        - id: resource
          uri: lb://producer-service
          filters:
            - StripPrefix=1
            - Customer=true
          predicates:
            - Path=/resource/**
```
> 多个个性化配置参数配置，如下示例： `name：& args：`


```yaml
 ## 路由配置方式
    gateway:
      routes:
        - id: resource
          uri: lb://producer-service
          filters:
            - StripPrefix=1
            - name: Customer
              args:
                enabled: true
                excludePaths: /webjars/**,/swagger-ui.html/**,/swagger-ui/*,/swagger-resources/**,/v2/api-docs,/v3/api-docs

```



#### 2 GlobalFilter配置

```java

/**
 * Token 校验全局过滤器
 *
 * @author Dean
 * @date 2020-11-27
 */
@Slf4j
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {


    private static final String AUTHORIZE_TOKEN = "token";
    private final AuthFilterProperties authFilterProperties;
    private PathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    public AuthorizeFilter(AuthFilterProperties authFilterProperties) {
        this.authFilterProperties = authFilterProperties;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String[] excludePaths = Objects.isNull(authFilterProperties.getExcludePaths()) ?
                new String[]{} : authFilterProperties.getExcludePaths();
        String requestUri = exchange.getRequest().getPath().value();
        if (matchPath(requestUri, excludePaths)) {
            log.info("ignore request [uri={}] auth filter", requestUri);
            return chain.filter(exchange);
        }
        String token = exchange.getRequest().getQueryParams().getFirst(AUTHORIZE_TOKEN);
        if (StringUtils.isEmpty(token)) {
            log.error("token is empty ...");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }



    private boolean matchPath(String requestUri, String... patterns) {
        return Stream.of(patterns).anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }
}


// 路径过滤
@Data
@Component
@ConfigurationProperties(prefix = "auth.filter")
public class AuthFilterProperties {

    /**
     * 优先级大于includePaths
     */
    private String[] excludePaths;
}

```


## 四 配置限流

### 1 maven 依赖(补充)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifatId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

### 2 配置文件


```yaml
 ## 路由配置方式
    gateway:
      routes:
        - id: resource
          uri: lb://producer-service
          filters:
            - StripPrefix=1
            - name: Customer
              args:
                enabled: true
                excludePaths: /webjars/**,/swagger-ui.html/**,/swagger-ui/*,/swagger-resources/**,/v2/api-docs,/v3/api-docs
            ## 支持重定向配置
#            - RewritePath=/foo/(?<segment>.*), /$\{segment}
            ## redis reactive rate limit
            - name: RequestRateLimiter
              args:
                key-resolver: '#{@hostAddrKeyResolver}'
                redis-rate-limiter.replenishRate: 1
                redis-rate-limiter.burstCapacity: 3
          predicates:
            - Path=/resource/**

```

配置转变为java代码：

```
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
            .route("resource", r -> r.path("/resource/**")
                    .filters(f ->
                            //去掉第一层前缀如果是/api/oauth这种 就stripPrefix(2)
                            f.stripPrefix(1)
                                    .requestRateLimiter()
                                    .rateLimiter(RedisRateLimiter.class, c ->
                                            c.setBurstCapacity(3).setReplenishRate(1))
                                    .configure(c -> c.setKeyResolver(hostAddrKeyResolver)))
                    .uri("lb://producer-service"))
            .build();
}
```



> 在上面的配置文件，配置RequestRateLimiter的限流过滤器，该过滤器需要配置三个参数：

    burstCapacity，令牌桶总容量
    replenishRate，令牌桶每秒填充平均速率
    key-resolver，用于限流的键的解析器的Bean对象的名字。它使用 SpEL 表达式根据#{@beanName}从 Spring 容器中获取 Bean 对象。
    
> KeyResolver需要实现resolve方法，比如根据Hostname,HostAddr,Uri等进行限流，则需要用hostAddress去判断。实现完KeyResolver之后，需要将这个类的Bean注册到Ioc容器中。


```java

/**
 * @author Dean
 * @date 2020-11-28
 */
@Configuration
public class RateLimiterConfiguration {

    @Bean(value = "hostAddrKeyResolver")
    public KeyResolver hostAddrKeyResolver() {
        return exchange -> Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
    }

}

```

> 当每秒请求数大于令牌桶容量，请求失败，通过redis客户端去查看redis中存在的key。如下：

```
127.0.0.1:6379[12]> keys *
1) "request_rate_limiter.{127.0.0.1}.timestamp"
2) "request_rate_limiter.{127.0.0.1}.tokens"
```
                
