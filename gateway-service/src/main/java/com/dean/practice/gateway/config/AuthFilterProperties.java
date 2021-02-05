package com.dean.practice.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Dean
 * @date 2020-11-27
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth.filter")
public class AuthFilterProperties {

    /**
     * 优先级大于includePaths
     */
    private String[] excludePaths;
}
