package com.yebuxiu.config;

import com.yebuxiu.config.properties.MyRedisProperties;
import com.yebuxiu.helper.RedisHelper;
import com.yebuxiu.template.DynamicRedisTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * stone-redis自动配置，必须要容器中有RedisConnectionFactory才启动该配置类
 */
@Configuration
@EnableConfigurationProperties(MyRedisProperties.class)
@ConditionalOnClass(name = {"org.springframework.data.redis.connection.RedisConnectionFactory"})
public class EnhanceDataRedisAutoConfiguration {


    /**
     * 注入RedisTemplate，key-value都使用string类型
     * RedisConnectionFactory由对应的spring-boot-autoconfigure自动配置到容器
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        buildRedisTemplate(redisTemplate, redisConnectionFactory);
        return redisTemplate;
    }

//    /**
//     * 注入StringRedisTemplate，key-value序列化器都使用String序列化器
//     */
//    @Bean
//    @ConditionalOnMissingBean(StringRedisTemplate.class)
//    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        StringRedisTemplate redisTemplate = new StringRedisTemplate();
//        buildRedisTemplate(redisTemplate, redisConnectionFactory);
//        return redisTemplate;
//    }

    /**
     * 通过Redis连接工厂构建一个RedisTemplate
     */
    private static void buildRedisTemplate(RedisTemplate<String, String> redisTemplate,
                                           RedisConnectionFactory redisConnectionFactory) {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setStringSerializer(stringRedisSerializer);
        redisTemplate.setDefaultSerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);
        redisTemplate.setConnectionFactory(redisConnectionFactory);
    }


    /**
     * 默认数据源的动态redisHelper
     * 这些参数由lettuce客户端帮我们自动配置并注入到容器
     */
    @Primary
    @Bean(name = {"redisHelper", "default", "default-helper"})
    public RedisHelper dynamicRedisHelper(StringRedisTemplate redisTemplate,
                                          MyRedisProperties myRedisProperties,
                                          ObjectProvider<List<LettuceClientConfigurationBuilderCustomizer>> builderCustomizers) {

        // 构建动态RedisTemplate工厂
        DynamicRedisTemplateFactory<String, String> dynamicRedisTemplateFactory =
                new DynamicRedisTemplateFactory<>(myRedisProperties,
                        builderCustomizers.getIfAvailable());
        // ======================================================================================================
        // 这里在注入的时候默认值注入一个默认的redisTemplate，以及将这个redisTemplate放入到map中，该redisTemplate
        // 操作的是配置文件中使用spring.redis.database属性指定的db（若不显示指定，则使用的0号db）
        // 注入的时候值放入这个默认的的redisTemplate到map中，在使用的时候如果需要动态切换库那么会通过连接工厂
        // 重新去创建一个redisTemplate，并缓存到map中（懒加载模式），并不会一次性创建出多个redisTemplate然后缓存起来（连接很昂贵）
        // ======================================================================================================

        DynamicRedisTemplate<String, String> dynamicRedisTemplate = new DynamicRedisTemplate<>(dynamicRedisTemplateFactory);
        // 当不指定库时，默认使用的RedisTemplate来操作Redis(直接获取容器中的)
        dynamicRedisTemplate.setDefaultRedisTemplate(redisTemplate);
        Map<Object, RedisTemplate<String, String>> map = new HashMap<>(8);
        // 配置文件中指定使用几号db
        map.put(myRedisProperties.getRedisProperties().getDatabase(), redisTemplate);
        // 将redisTemplate缓存起来
        dynamicRedisTemplate.setRedisTemplates(map);

        return new RedisHelper(dynamicRedisTemplate);
    }

}
