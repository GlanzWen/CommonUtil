package com.glanz.redis.manage.Impl;

import com.glanz.redis.manage.RedisManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;


@Component
@ConditionalOnProperty(value = "redis.data.model",havingValue = "sentinel",matchIfMissing = false)
@Slf4j
public class RedisManagerJedisSentinel implements RedisManager {
    private int expire = 0;

    @Autowired(required = false)
    private JedisSentinelPool jedisPool;

    @Override
    public byte[] get(byte[] key) {
        byte[] value = null;
        Jedis jedis =null;
        try {
            jedis = jedisPool.getResource();
            value = jedis.get(key);
        } catch (Exception e) {
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
            e.printStackTrace();
        } finally {
            jedis.close();
            //返还到连接池
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
        return value;
    }
    @Override
    public String get(String key) {
        String value = null;
        Jedis jedis = jedisPool.getResource();
        try {
            value = jedis.get(key);
        } catch (Exception e) {
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
            e.printStackTrace();
        } finally {
            //返还到连接池
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
        return value;
    }
    @Override
    public byte[] set(byte[] key, byte[] value) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.set(key, value);
            if (this.expire != 0) {
                jedis.expire(key, this.expire);
            }
        } catch (Exception e) {
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
            e.printStackTrace();
        } finally {
            //返还到连接池
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
        return value;
    }
    @Override
    public String set(String key, String value) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.set(key, value);
            if (this.expire != 0) {
                jedis.expire(key, this.expire);
            }
        } catch (Exception e) {
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
            e.printStackTrace();
        } finally {
            //返还到连接池
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
        return value;
    }
    @Override
    public byte[] set(byte[] key, byte[] value, int expire) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.set(key, value);
            if (expire != 0) {
                jedis.expire(key, expire);
            }
        } catch (Exception e) {
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
            e.printStackTrace();
        } finally {
            //返还到连接池
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
        return value;
    }
    @Override
    public String set(String key, String value, int expire) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.set(key, value);
            if (expire != 0) {
                jedis.expire(key, expire);
            }
        } catch (Exception e) {
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
            e.printStackTrace();
        } finally {
            //返还到连接池
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
        return value;
    }
    @Override
    public void del(byte[] key) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.del(key);
        } catch (Exception e) {
            //释放redis对象
            jedisPool.returnBrokenResource(jedis);
            e.printStackTrace();
        } finally {
            //返还到连接池
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
    }
    @Override
    public void del(String key) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.del(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

}


