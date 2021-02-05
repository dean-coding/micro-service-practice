# micro-service-practice
微服务实践/micro service practice for: gateway ,oauth2,monitor,sinature and so on

### 规划图
<img src="https://note.youdao.com/yws/api/personal/file/WEBa356eb3b872d3f9480d3538b90f5d3ef?method=download&shareKey=80572e030106a59eaa6fb400f0d64ef4" width="780">


## 环境

| 工具  | 版本或描述 |
| ----- | ---|
| JDK   | 1.8  |
| IDE   |  `IntelliJ` IDEA |
| Maven | 3.x                  |
| spring-cloud-dependencies | Greenwich.SR2 |
| spring-boot-starter | 2.1.6.RELEASE |
| spring-cloud-starter-netflix-eureka-client | 2.1.2.RELEASE |
| spring-cloud-starter-netflix-eureka-server | 2.1.2.RELEASE |
| spring-cloud-starter-gateway | 2.1.2.RELEASE |

## 功能

| 服务  | 描述 |
| ----- | ---|
| [gateway-service](gateway-service/README.md)  | 网关服务：动态路由，限流 |
| [eureka-registry](eureka-registry/README.md)  | eureka注册中心 |

