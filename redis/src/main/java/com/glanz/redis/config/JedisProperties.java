package com.glanz.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Data
@Configuration
@Component
@ConfigurationProperties(prefix = "spring.data.redis.jedis.pool")
public class JedisProperties {
    private Integer maxIdle;

    private Integer maxWait;

    private Integer minIdle;

    private Integer maxActive;
}


