package com.dean.practice.gateway.filter;

import com.dean.practice.gateway.config.AuthFilterProperties;
import com.dean.practice.gateway.util.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

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

    @Autowired
    public AuthorizeFilter(AuthFilterProperties authFilterProperties) {
        this.authFilterProperties = authFilterProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // config exclude path
        String[] excludePaths = Objects.isNull(authFilterProperties.getExcludePaths()) ?
                new String[]{} : authFilterProperties.getExcludePaths();
        String requestUri = exchange.getRequest().getPath().value();
        if (PathUtils.matchPath(requestUri, excludePaths)) {
            log.info("ignore request [uri={}] auth filter", requestUri);
            return chain.filter(exchange);
        }
        // check token
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




}