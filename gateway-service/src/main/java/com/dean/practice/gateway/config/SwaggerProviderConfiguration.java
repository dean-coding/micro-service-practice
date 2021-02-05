package com.dean.practice.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * swagger 聚合配置
 *
 * @author Dean
 * @date 2020-11-26
 */
@Component
@Primary
@RequiredArgsConstructor
public class SwaggerProviderConfiguration implements SwaggerResourcesProvider {

    // swagger api 的路径
    private static final String API_URI = "/v2/api-docs";

    private final RouteLocator routeLocator;

    private final RouteDefinitionLocator routeDefinitionLocator;

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        List<String> routes = new ArrayList<>();
        // 取路由Id
        routeLocator.getRoutes().subscribe(route -> routes.add(route.getId()));
        // 取路由信息
        this.routeDefinitionLocator.getRouteDefinitions().collectList().subscribe(list -> list.stream()
                .filter(routeDefinition -> routes.contains(routeDefinition.getId()))
                .forEach(routeDefinition -> routeDefinition.getPredicates().stream()
                        // predicate 过滤定义：Path Host Header 等
                        .filter(predicateDefinition -> "Path".equalsIgnoreCase(predicateDefinition.getName()))
                        .forEach(predicateDefinition -> resources
                                .add(swaggerResource(routeDefinition.getId(), predicateDefinition.getArgs()
                                        .get(NameUtils.GENERATED_NAME_PREFIX + "0").replace("/**", API_URI)))))

        );
        return resources;
    }

    /**
     * 配置swagger resource
     *
     * @param name route_id
     * @param location /route_id/v2/api-docs
     * @return SwaggerResource
     */
    private SwaggerResource swaggerResource(String name, String location) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }

}