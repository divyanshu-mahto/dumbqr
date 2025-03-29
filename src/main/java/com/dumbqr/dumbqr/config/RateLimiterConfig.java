package com.dumbqr.dumbqr.config;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Supplier;

@Configuration
public class RateLimiterConfig {

    @Value("${spring.redis.host}")
    private String bucket4jRedisHost;

    @Value("${spring.redis.port}")
    private int bucket4jRedisPort;

    @Value("${spring.redis.password}")
    private String bucket4jPassword;

    @Value("${rate.limit.capacity}")
    private int limitCapacity;

    @Value("${rate.limit.refill}")
    private int limitRefill;

    @Value("${rate.limit.refill.duration}")
    private int limitRefillDuration;

    @Bean
    public RedisClient lettuceRedisClient() {
        String redisUri = String.format("redis://:%s@%s:%d", bucket4jPassword, bucket4jRedisHost, bucket4jRedisPort);
        return RedisClient.create(redisUri);
    }

    @Bean
    public ProxyManager<String> lettuceBasedProxyManager(RedisClient redisClient) {
        StatefulRedisConnection<String,byte[]> redisConnection = redisClient.
                connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
        return LettuceBasedProxyManager.builderFor(redisConnection)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(5L)))
                .build();
    }

    @Bean
    public Supplier<BucketConfiguration> bucketConfiguration() {
        return ()-> BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(limitCapacity).refillGreedy(limitRefill, Duration.ofMinutes(limitRefillDuration)))
                .build();
    }

}
