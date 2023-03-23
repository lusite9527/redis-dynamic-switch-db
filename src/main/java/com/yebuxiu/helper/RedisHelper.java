package com.yebuxiu.helper;

import com.yebuxiu.template.DynamicRedisTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作工具类 集成封装一些常用方法
 */
public class RedisHelper implements InitializingBean {

    /**
     * 不设置过期时长
     */
    public static final long NOT_EXPIRE = -1;

    private final DynamicRedisTemplate<String, String> redisTemplate;
    public RedisHelper(DynamicRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(redisTemplate, "redisTemplate must not be null.");
    }


    public RedisTemplate<String, String> getRedisTemplate() {
        return redisTemplate;
    }

    public Map<Object, RedisTemplate<String, String>> getRedisTemplates() {
        return redisTemplate.getRedisTemplates();
    }

    protected ValueOperations<String, String> getValueOperations() {
        return redisTemplate.opsForValue();
    }

    /**
     * 设置当前线程操作 redis database，同一个线程内操作多次redis，不同database，
     * 需要调用 {@link RedisHelper#clearCurrentDatabase()} 清除当前线程redis database，从而使用默认的db.
     * 如果静态RedisHelper进行db切换，这是不被允许的，需要抛出异常
     *
     * @param database redis database
     */
    public void setCurrentDatabase(int database) {
//        logger.warn("Use default RedisHelper, you'd better use a DynamicRedisHelper instead.");
//        throw new RuntimeException("static redisHelper can't change db.");
        RedisDatabaseThreadLocalHelper.set(database);
    }

    /**
     * 清除当前线程 redis database.
     */
    public void clearCurrentDatabase() {
//        logger.warn("Use default RedisHelper, you'd better use a DynamicRedisHelper instead.");
        RedisDatabaseThreadLocalHelper.clear();
    }


    /**
     * String 设置值
     *
     * @param key    key
     * @param value  value
     * @param expire 过期时间
     */
    public void strSet(String key, String value, long expire, TimeUnit timeUnit) {
        getValueOperations().set(key, value);
        if (expire != NOT_EXPIRE) {
            setExpire(key, expire, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
        }
    }

    /**
     * String 获取值
     *
     * @param key key
     */
    public String strGet(String key) {
        return getValueOperations().get(key);
    }

    /**
     * 设置过期时间
     *
     * @param key      key
     * @param expire   存活时长
     * @param timeUnit 时间单位
     */
    public Boolean setExpire(String key, long expire, TimeUnit timeUnit) {
        return redisTemplate.expire(key, expire, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
    }

    public void strSetWithDb(int db, String key, String value, long expire, TimeUnit timeUnit) {
        try {
            setCurrentDatabase(db);
            strSet(key,value,expire,timeUnit);
        } finally {
            clearCurrentDatabase();
        }
    }

    public String strGetWithDb(int db, String key) {
        try {
            setCurrentDatabase(db);
            return strGet(key);
        } finally {
            clearCurrentDatabase();
        }
    }
}
