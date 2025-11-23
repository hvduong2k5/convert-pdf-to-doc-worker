package com.laptrinhmang.service;

import com.google.gson.Gson;
import com.laptrinhmang.bean.FileEntity;
import com.laptrinhmang.util.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisService {
    public FileEntity pop(String key) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            String json = jedis.brpop(0, RedisUtil.getQueueName()).get(1);
            return new Gson().fromJson(json, FileEntity.class);
        }
    }
    public void push(FileEntity fileEntity) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            String json = new Gson().toJson(fileEntity);
            jedis.lpush(RedisUtil.getQueueName(), json);
        }
    }
    public void cacheSet(String key, FileEntity value) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            String json = new Gson().toJson(value);
            jedis.set(key, json);
        }
    }

    public void cacheSet(String key, FileEntity value, int expireSeconds) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            String json = new Gson().toJson(value);
            jedis.setex(key, expireSeconds, json);
        }
    }

    public FileEntity cacheGet(String key) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            String json = jedis.get(key);
            return new Gson().fromJson(json, FileEntity.class);
        }
    }

    public void cacheDelete(String key) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            jedis.del(key);
        }
    }

    public void publish(String channel, FileEntity message) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            String json = new Gson().toJson(message);
            jedis.publish(channel, json);
        }
    }

    // subscribe chạy thread riêng để không block
    public void subscribe(JedisPubSub listener, String... channels) {
        new Thread(() -> {
            try (Jedis jedis = RedisUtil.getPool().getResource()) {
                jedis.subscribe(listener, channels);
            }
        }).start();
    }

}
