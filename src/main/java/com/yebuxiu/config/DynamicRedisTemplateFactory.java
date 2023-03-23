package com.yebuxiu.config;

import com.yebuxiu.config.properties.MyRedisProperties;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 动态 RedisTemplate 工厂类，用于创建和管理RedisTemplate
 */
public class DynamicRedisTemplateFactory<K, V> {

    // ==============================================================================================================
    // 从data-redis源码得知，构建lettuce客户端配置（LettuceConnectionConfigure）
    // 需要如下参数，并且从spring自动配置模块的源码可以看到，这些属性会由springboot自动配置帮我们注入到容器中，所以这里可以通过构造器
    // 将这些属性传递进来，并保存到属性上以便后面使用。
    // ==============================================================================================================

    /**
     * Redis配置信息
     */
    private final MyRedisProperties myRedisProperties;
    /**
     * lettuce配置定制
     */
    private final List<LettuceClientConfigurationBuilderCustomizer> lettuceBuilderCustomizers;


    /**
     * 这些参数由springboot自动配置帮我们自动配置并注入到容器
     * ObjectProvider更加宽松的依赖注入
     */
    public DynamicRedisTemplateFactory(MyRedisProperties myRedisProperties,
                                       List<LettuceClientConfigurationBuilderCustomizer> lettuceBuilderCustomizers) {
        this.myRedisProperties = myRedisProperties;
        this.lettuceBuilderCustomizers = lettuceBuilderCustomizers;
    }

    /**
     * 为指定的db创建RedisTemplate，用于操作Redis
     *
     * @param database redis db
     * @return org.springframework.data.redis.core.RedisTemplate<K, V>
     * @author Mr_wenpan@163.com 2021/8/7 1:47 下午
     */
    public RedisTemplate<K, V> createRedisTemplate(int database) {
        RedisConnectionFactory redisConnectionFactory = null;
        // 根据Redis客户端类型创建Redis连接工厂（用于创建RedisTemplate）
        // 使用指定的db创建lettuce redis连接工厂(创建方式参照源码：LettuceConnectionConfiguration)
        LettuceConnectionConfigure lettuceConnectionConfigure = new LettuceConnectionConfigure(
                myRedisProperties, lettuceBuilderCustomizers, database);
        redisConnectionFactory = lettuceConnectionConfigure.redisConnectionFactory();
        Assert.notNull(redisConnectionFactory, "redisConnectionFactory is null.");
        // 通过Redis连接工厂创建RedisTemplate
        return createRedisTemplate(redisConnectionFactory);
    }

    /**
     * 通过Redis连接工厂来创建一个redisTemplate用于操作Redis db
     */
    private RedisTemplate<K, V> createRedisTemplate(RedisConnectionFactory factory) {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        RedisTemplate<K, V> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setStringSerializer(stringRedisSerializer);
        redisTemplate.setDefaultSerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);
        // 设置Redis连接工厂用于创建连接
        redisTemplate.setConnectionFactory(factory);
        // 调用afterPropertiesSet方法，在属性设置完成后做一些检查和额外工作
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


}
