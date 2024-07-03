package com.glanz.redis.manage;


public interface RedisManager {

    byte[] get(byte[] key);
    String get(String key);
    byte[] set(byte[] key, byte[] value);
    String set(String key, String value);
    byte[] set(byte[] key, byte[] value, int expire);
    String set(String key, String value, int expire);
    void del(byte[] key);
    void del(String key);

}


