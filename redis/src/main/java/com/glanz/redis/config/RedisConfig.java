package com.glanz.redis.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;


@Configuration
public class RedisConfig {
    @Autowired
    private RedisProperties redisProperties;

    @Autowired
    private JedisProperties jedisProperties;

    @ConditionalOnProperty(value = "redis.data.model",havingValue = "standalone",matchIfMissing = true)
    @Bean
    public JedisPool redisPoolFactory() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(jedisProperties.getMaxIdle());
        jedisPoolConfig.setMaxWaitMillis(jedisProperties.getMaxWait());
        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
            return new JedisPool(jedisPoolConfig, redisProperties.getHost(), redisProperties.getPort(), redisProperties.getTimeout(), redisProperties.getPassword(), redisProperties.getDatabase());
        } else {
            return new JedisPool(jedisPoolConfig, redisProperties.getHost(), redisProperties.getPort(), redisProperties.getTimeout(), null, redisProperties.getDatabase());
        }
    }

    @ConditionalOnProperty(value = "redis.data.model",havingValue = "sentinel",matchIfMissing = false)
    @Bean
    public JedisSentinelPool jedisSentinelPool(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(jedisProperties.getMaxIdle());
        jedisPoolConfig.setMaxWaitMillis(jedisProperties.getMaxWait());
        // 截取集群节点
        String[] sentinels = redisProperties.getSentinel().getNodes().toArray(new String[0]);
        // 创建set集合
        Set<String> nodes = new HashSet<>();
        // 循环数组把集群节点添加到set集合中
        for (String node : sentinels) {
            //添加节点
            nodes.add(node);
        }
        if(StringUtils.isNotBlank(redisProperties.getSentinel().getPassword())){
            return new JedisSentinelPool(redisProperties.getSentinel().getMaster(),nodes,jedisPoolConfig, redisProperties.getTimeout(),redisProperties.getTimeout(),
                    redisProperties.getPassword(),redisProperties.getDatabase(),null,redisProperties.getTimeout(),redisProperties.getTimeout(),
                    redisProperties.getSentinel().getPassword(),null);
        }else{
            return new JedisSentinelPool(redisProperties.getSentinel().getMaster(),nodes,jedisPoolConfig,redisProperties.getTimeout(),redisProperties.getPassword() );
        }
    }

    @ConditionalOnProperty(value = "redis.data.model",havingValue = "cluster",matchIfMissing = false)
    @Bean
    public JedisCluster getJedisCluster() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(jedisProperties.getMaxIdle());
        poolConfig.setMaxWaitMillis(jedisProperties.getMaxWait());
        poolConfig.setMinIdle(jedisProperties.getMinIdle());
        poolConfig.setMaxTotal(jedisProperties.getMaxActive());
        // 截取集群节点
        String[] cluster = redisProperties.getCluster().getNodes().toArray(new String[0]);
        // 创建set集合
        Set<HostAndPort> nodes = new HashSet<>();
        // 循环数组把集群节点添加到set集合中
        for (String node : cluster) {
            String[] host = node.split(":");
            //添加集群节点
            nodes.add(new HostAndPort(host[0], Integer.parseInt(host[1])));
        }
        //需要密码连接的创建对象方式
        return new JedisCluster(nodes, redisProperties.getTimeout(), 2000, redisProperties.getCluster().getMaxRedirects(), redisProperties.getPassword(), poolConfig);
    }
}


