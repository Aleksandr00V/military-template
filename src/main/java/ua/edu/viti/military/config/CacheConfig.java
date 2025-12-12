package ua.edu.viti.military.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Конфігурація Redis кешування.
 * 
 * @EnableCaching активує підтримку кешування через анотації:
 * - @Cacheable - зберегти результат в кеші
 * - @CacheEvict - видалити з кешу
 * - @CachePut - оновити кеш
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        
        // Налаштування ObjectMapper з підтримкою Java 8 date/time
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        
        GenericJackson2JsonRedisSerializer jsonSerializer = 
            new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // Базова конфігурація (за замовчуванням 1 година)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()
                )
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
            )
            .disableCachingNullValues();
        
        // Специфічні налаштування для різних кешів
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Категорії транспорту - кешуємо на 24 години (рідко змінюються)
        cacheConfigurations.put("vehicleCategories", defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // Водії - кешуємо на 12 годин
        cacheConfigurations.put("drivers", defaultConfig.entryTtl(Duration.ofHours(12)));
        
        // Транспортні засоби - кешуємо на 30 хвилин (частіше змінюються)
        cacheConfigurations.put("vehicles", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Призначення - кешуємо на 5 хвилин (часто змінюються)
        cacheConfigurations.put("assignments", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
