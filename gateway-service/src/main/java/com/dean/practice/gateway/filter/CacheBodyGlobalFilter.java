package com.dean.practice.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.AdaptCachedBodyGlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * 解决body不能重复读的问题
 * 实际上这里没必要把body的内容放到attribute中去，因为从attribute取出body内容还是需要强转成
 * Flux<DataBuffer>,然后转换成String,和直接读取body没有什么区别
 *
 * 该方案的缺陷：request body获取不完整（因为异步原因），只能获取1024B的数据。
 * 并且请求体超过1024B，会出现响应超慢（TODO 待测试）
 *
 * @see AdaptCachedBodyGlobalFilter
 * @author Dean
 * @date 2020-11-27
 */
@Slf4j
@Component
public class CacheBodyGlobalFilter implements Ordered, GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        if (!Objects.equals(HttpMethod.POST, request.getMethod())) {
            return chain.filter(exchange);
        }

        if (HttpMethod.POST == request.getMethod() || HttpMethod.PUT == request.getMethod()) {
            return DataBufferUtils.join(request.getBody())
                    .flatMap(dataBuffer -> {
                        DataBufferUtils.retain(dataBuffer);
                        Flux<DataBuffer> cachedFlux = Flux
                                .defer(() -> Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));
                        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(
                                request) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                return cachedFlux;
                            }
                        };
                        return chain.filter(exchange.mutate().request(mutatedRequest)
                                .build());
                    });
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}