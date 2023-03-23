package com.yebuxiu.config;

import com.yebuxiu.config.properties.MyRedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Base Redis connection configuration.
 * 参考：spring-boot-autoconfigure 下的 RedisConnectionConfiguration ，参考版本2.2.7
 * 原：从redisProperties 中读取database
 * 修改后：手动指定database
 *
 * @author Mark Paluch
 * @author Stephane Nicoll
 * @author Alen Turkovic
 */
public abstract class RedisConnectionConfiguration {

    /**
     * redis配置properties
     */
    private final MyRedisProperties myRedisProperties;


    /**
     * redis所使用的库，为指定的db动态的创建RedisTemplate
     */
    private final int database;

    protected RedisConnectionConfiguration(MyRedisProperties myRedisProperties,
                                           int database) {
        this.myRedisProperties = myRedisProperties;
        this.database = database;
    }

    /**
     * redis 单机模式配置信息
     */
    protected final RedisStandaloneConfiguration getStandaloneConfig() {
        RedisProperties gps = myRedisProperties.getRedisProperties();
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        if (StringUtils.hasText(gps.getUrl())) {
            ConnectionInfo connectionInfo = parseUrl(gps.getUrl());
            config.setHostName(connectionInfo.getHostName());
            config.setPort(connectionInfo.getPort());
            config.setPassword(RedisPassword.of(connectionInfo.getPassword()));
        } else {
            config.setHostName(gps.getHost());
            config.setPort(gps.getPort());
            config.setPassword(RedisPassword.of(gps.getPassword()));
        }
        // 使用自定义db
        config.setDatabase(database);
        return config;
    }


    protected final RedisProperties getProperties() {
        return myRedisProperties.getRedisProperties();
    }


    /**
     * 解析Redis url连接，创建连接信息
     */
    protected static ConnectionInfo parseUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"redis".equals(scheme) && !"rediss".equals(scheme)) {
                // 无效的或畸形的url
                throw new RedisUrlSyntaxException(url);
            }
            boolean useSsl = ("rediss".equals(scheme));
            String password = null;
            if (uri.getUserInfo() != null) {
                String candidate = uri.getUserInfo();
                int index = candidate.indexOf(':');
                if (index >= 0) {
                    password = candidate.substring(index + 1);
                } else {
                    password = candidate;
                }
            }
            return new ConnectionInfo(uri, useSsl, password);
        } catch (URISyntaxException ex) {
            // 无效的或畸形的url
            throw new RedisUrlSyntaxException("Malformed url '" + url + "'", ex);
        }
    }

    /**
     * Redis连接信息
     */
    static class ConnectionInfo {

        private final URI uri;
        private final boolean useSsl;
        private final String password;

        ConnectionInfo(URI uri, boolean useSsl, String password) {
            this.uri = uri;
            this.useSsl = useSsl;
            this.password = password;
        }

        boolean isUseSsl() {
            return useSsl;
        }

        String getHostName() {
            return uri.getHost();
        }

        int getPort() {
            return uri.getPort();
        }

        String getPassword() {
            return password;
        }

    }

}
