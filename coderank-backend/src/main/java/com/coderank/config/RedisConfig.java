package com.coderank.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Cache Configuration.
 *
 * Configures Spring Cache to use Redis as the cache store,
 * with JSON serialization so values are human-readable in Redis,
 * and a default TTL of 10 minutes.
 *
 * Cache Names:
 *  - "dashboard"   → caches getDashboard() results per userId
 *  - "leaderboard" → caches getLeaderboard() results per category
 */
@Configuration
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                // Keys stored as plain strings: "dashboard::42"
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                // Values stored as JSON (readable in Redis CLI)
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                // Expire cache entries after 10 minutes
                .entryTtl(Duration.ofMinutes(10))
                // Do NOT cache null values (prevents empty result caching on errors)
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .build();
    }
}
