package com.yebuxiu.template;

import com.yebuxiu.config.DynamicRedisTemplateFactory;
import com.yebuxiu.helper.RedisDatabaseThreadLocalHelper;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 动态 RedisTemplate ，以支持动态切换 redis database
 */
public class DynamicRedisTemplate<K, V> extends AbstractRoutingRedisTemplate<K, V> {

    /**
     * 动态RedisTemplate工厂，用于创建管理动态DynamicRedisTemplate
     */
    private final DynamicRedisTemplateFactory<K, V> dynamicRedisTemplateFactory;

    public DynamicRedisTemplate(DynamicRedisTemplateFactory<K, V> dynamicRedisTemplateFactory) {
        this.dynamicRedisTemplateFactory = dynamicRedisTemplateFactory;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return RedisDatabaseThreadLocalHelper.get();
    }

    /**
     * 通过制定的db创建RedisTemplate
     *
     * @param lookupKey db号
     * @return org.springframework.data.redis.core.RedisTemplate<K, V>
     */
    @Override
    public RedisTemplate<K, V> createRedisTemplateOnMissing(Object lookupKey) {
        return dynamicRedisTemplateFactory.createRedisTemplate((Integer) lookupKey);
    }

}
