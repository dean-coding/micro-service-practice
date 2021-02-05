package com.dean.practice.gateway.dynamic.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dean
 * @date 2020-11-25
 */
@Slf4j
@Service
public class DynamicRouteService implements ApplicationEventPublisherAware {

    @Resource
    private RedisRouteDefinitionRepository redisRouteDefinitionRepository;

    private ApplicationEventPublisher applicationEventPublisher;

    private final RouteDefinitionLocator routeDefinitionLocator;

    private final RouteLocator routeLocator;

    public DynamicRouteService(RouteDefinitionLocator routeDefinitionLocator, RouteLocator routeLocator) {
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.routeLocator = routeLocator;
    }


    /**
     * 增加路由
     *
     * @param routeDefinition RouteDefinition
     * @return 1 success
     */
    public int add(RouteDefinition routeDefinition) {
        redisRouteDefinitionRepository.save(Mono.just(routeDefinition)).subscribe();
        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        return 1;
    }

    /**
     * 更新
     *
     * @param routeDefinition RouteDefinition
     * @return 1 success
     */
    public int update(RouteDefinition routeDefinition) {
        redisRouteDefinitionRepository.delete(Mono.just(routeDefinition.getId())).subscribe();
        redisRouteDefinitionRepository.save(Mono.just(routeDefinition)).subscribe();
        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        return 1;
    }

    /**
     * 删除
     *
     * @param id routeId
     * @return ResponseEntity
     */
    public Mono<ResponseEntity<Object>> delete(String id) {
        return redisRouteDefinitionRepository.delete(Mono.just(id)).then(Mono.defer(() -> Mono.just(ResponseEntity.ok().build())))
                // 没有找到默认OK
                .onErrorResume(t -> t instanceof NotFoundException, t -> {
                    log.warn("dynamic_route_delete_not_found id={}", id);
                    return Mono.just(ResponseEntity.ok().build());
                });
    }


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * routeList
     *
     * @return RouteDefinition Collection
     */
    public Mono<List<Map<String, Object>>> routeList() {
        Mono<Map<String, RouteDefinition>> routeDefs = this.routeDefinitionLocator
                .getRouteDefinitions().collectMap(RouteDefinition::getId);
        Mono<List<Route>> routes = this.routeLocator.getRoutes().collectList();
        return Mono.zip(routeDefs, routes).map(tuple -> {
            Map<String, RouteDefinition> defs = tuple.getT1();
            List<Route> routeList = tuple.getT2();
            List<Map<String, Object>> allRoutes = new ArrayList<>();

            routeList.forEach(route -> {
                HashMap<String, Object> r = new HashMap<>();
                r.put("route_id", route.getId());
                r.put("order", route.getOrder());

                if (defs.containsKey(route.getId())) {
                    r.put("route_definition", defs.get(route.getId()));
                } else {
                    HashMap<String, Object> obj = new HashMap<>();

                    obj.put("predicate", route.getPredicate().toString());

                    if (!route.getFilters().isEmpty()) {
                        ArrayList<String> filters = new ArrayList<>();
                        for (GatewayFilter filter : route.getFilters()) {
                            filters.add(filter.toString());
                        }

                        obj.put("filters", filters);
                    }

                    if (!obj.isEmpty()) {
                        r.put("route_object", obj);
                    }
                }
                allRoutes.add(r);
            });

            return allRoutes;
        });
    }
}