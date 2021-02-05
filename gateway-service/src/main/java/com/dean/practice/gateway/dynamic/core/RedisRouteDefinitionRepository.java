package com.dean.practice.gateway.dynamic.core;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * @author Dean
 * @date 2020-11-25
 */
@Slf4j
@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {

    /**
     * route key
     */
    private static final String ROUTE_KEY = "gateway_dynamic_route";

    private Gson gson = new Gson();

    private HashOperations<String, String, String> operations;

    public RedisRouteDefinitionRepository(RedisTemplate<String, String> redisTemplate) {
        this.operations = redisTemplate.opsForHash();
    }

    /**
     * 获取路由信息
     */
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> gatewayRouteEntityList = Lists.newArrayList();
        operations.values(ROUTE_KEY).forEach(routeStr -> {
            RouteDefinition result = gson.fromJson(routeStr, RouteDefinition.class);
            gatewayRouteEntityList.add(result);
        });
        return Flux.fromIterable(gatewayRouteEntityList);
    }

    /**
     * 新增route
     */
    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(routeDefinition -> {
            operations.put(ROUTE_KEY, routeDefinition.getId(), gson.toJson(routeDefinition));
            return Mono.empty();
        });
    }

    /**
     * 删除 route by id
     */
    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            if (Objects.equals(Boolean.TRUE,operations.hasKey(ROUTE_KEY, id))) {
                operations.delete(ROUTE_KEY, id);
                log.info("dynamic_route_delete_success id={}",id);
                return Mono.empty();
            }
            return Mono.defer(() -> Mono.error(new NotFoundException("route definition is not found, routeId:" + routeId)));
        });
    }
}