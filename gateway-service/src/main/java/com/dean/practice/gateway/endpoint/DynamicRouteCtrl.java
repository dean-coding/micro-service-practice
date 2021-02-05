package com.dean.practice.gateway.endpoint;

import com.dean.practice.gateway.dynamic.core.DynamicRouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * gateway 动态网关路由配置
 *
 * @author Dean
 * @date 2020-11-25
 */
@Slf4j
@RestController
@RequestMapping("/gateway")
public class DynamicRouteCtrl {

    @Resource
    private DynamicRouteService dynamicRouteService;

    @PostMapping("/add")
    public String create(@RequestBody RouteDefinition entity) {
        int result = dynamicRouteService.add(entity);
        return String.valueOf(result);
    }

    @PostMapping("/update")
    public String update(@RequestBody RouteDefinition entity) {
        int result = dynamicRouteService.update(entity);
        return String.valueOf(result);
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable String id) {
        return dynamicRouteService.delete(id);
    }

    @GetMapping("/routes")
    public Mono<List<Map<String, Object>>> routes() {
        return dynamicRouteService.routeList();
    }

}