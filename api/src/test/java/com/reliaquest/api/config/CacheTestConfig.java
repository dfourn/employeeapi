package com.reliaquest.api.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestConfiguration
@EnableCaching
public class CacheTestConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("employeeById");
    }
}
