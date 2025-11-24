package com.laptrinhmang.service;

import com.google.gson.Gson;
import com.laptrinhmang.bean.FileEntity;
import com.laptrinhmang.util.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisService {
    public FileEntity pop() {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            String json = jedis.brpop(0, RedisUtil.getQueueName()).get(1);
            return new Gson().fromJson(json, FileEntity.class);
        } catch (Exception e) {
            System.err.println("Pop error with : "+e.getMessage());
            return null;
        }
    }
    public void push(FileEntity fileEntity) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            String json = new Gson().toJson(fileEntity);
            jedis.lpush(RedisUtil.getQueueName(), json);
        } catch (Exception e) {
            System.err.println("push error with :"+e.getMessage());
        }
    }
    public void cacheSet(String key, FileEntity value) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            String json = new Gson().toJson(value);
            jedis.set(key, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cacheSet(String key, FileEntity value, int expireSeconds) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            String json = new Gson().toJson(value);
            jedis.setex(key, expireSeconds, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FileEntity cacheGet(String key) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            String json = jedis.get(key);
            return new Gson().fromJson(json, FileEntity.class);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void cacheDelete(String key) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            jedis.del(key);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(String channel, FileEntity message) {
        try (Jedis jedis = RedisUtil.getPool().getResource()) {
            String json = new Gson().toJson(message);
            jedis.publish(channel, json);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // subscribe chạy thread riêng để không block
    public void subscribe(JedisPubSub listener, String... channels) {
        new Thread(() -> {
            try (Jedis jedis = RedisUtil.getPool().getResource()) {
                jedis.subscribe(listener, channels);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
