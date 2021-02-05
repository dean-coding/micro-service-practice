package com.dean.practice.gateway.filter;

import com.dean.practice.gateway.util.PathUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 在 spring.gateway.filters 指定时候，取前缀，示例：
 * CustomerGatewayFilterFactory
 * <p>
 * 配置为 Customer=true
 * <p>
 * CustomerGatewayFilterFactory.Config 个性化特定参数
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
            // is enable？ or is exclude path？
            String[] excludePaths = Objects.isNull(config.getExcludePaths()) ?
                    new String[]{} : config.getExcludePaths();
            String requestUri = exchange.getRequest().getPath().value();
            if (!config.isEnabled() || PathUtils.matchPath(requestUri, excludePaths)) {
                return chain.filter(exchange);
            }
            // statistic ttl time and params(query & payload)
            ServerHttpRequest request = exchange.getRequest();
            exchange.getAttributes().put(COUNT_START_TIME, System.currentTimeMillis());
            return chain.filter(exchange).then(
                    Mono.fromRunnable(() -> {
                        Long startTime = exchange.getAttribute(COUNT_START_TIME);
                        if (Objects.nonNull(startTime)) {
                            StringBuilder sb = new StringBuilder(request.getURI().getPath())
                                    .append(": ")
                                    .append(System.currentTimeMillis() - startTime)
                                    .append("(ms) ");
                            sb.append("params:").append(request.getQueryParams());
                            // retrieve request body
                            if (HttpMethod.POST == request.getMethod() || HttpMethod.PUT == request.getMethod()) {
                                sb.append(",body:").append(getBodyContent(request));
                            }
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

        private String[] excludePaths;

    }

    private String getBodyContent(ServerHttpRequest request) {
        try {
            Flux<DataBuffer> body = request.getBody();
            AtomicReference<String> bodyRef = new AtomicReference<>();
            // 缓存读取的request body信息
            body.subscribe(dataBuffer -> {
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
                DataBufferUtils.release(dataBuffer);
                bodyRef.set(charBuffer.toString());
            });
            //获取request body
            return bodyRef.get().replaceAll("\\n", "");
        } catch (Exception e) {
            log.error("parse request payload is null:", e.getMessage());
            return null;
        }
    }
}