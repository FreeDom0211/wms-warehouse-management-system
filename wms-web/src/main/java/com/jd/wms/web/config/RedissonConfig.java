package com.jd.wms.web.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${redisson.address:redis://localhost:6379}")
    private String address;

    @Value("${redisson.database:0}")
    private int database;

    @Value("${redisson.password:}")
    private String password;

    @Value("${redisson.timeout:10000}")
    private int timeout;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.setNettyThreads(32);
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setPassword(password.isEmpty() ? null : password)
                .setTimeout(timeout)
                .setConnectionPoolSize(32)
                .setConnectionMinimumIdleSize(8);

        return Redisson.create(config);
    }
}