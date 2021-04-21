package com.pcy.distribute_learning;

import com.google.common.collect.Lists;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @description:
 * @author: 彭椿悦
 * @data: 2021/4/20 18:22
 */
@Component
public class RedisService {
    private RedisTemplate<String, String> redisTemplate;
    public static final Long RELEASE_SUCCESS = 1L;
    private static final Long POSTPONE_SUCCESS = 1L;
    public static final String SET_IF_NOT_EXIST = "NX";
    public static final String SET_WITH_EXPIRE_TIME = "EX";
    private static final String RELEASE_LOCK_SCRIPT =
            "if redis.call('get',KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del',KEYS[1]) " +
                    "else return 0 " +
                    "end";
    private static final String POSTPONE_LOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('expire', KEYS[1], ARGV[2]) " +
                    "else return 0 " +
                    "end";

    @Resource
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisTemplate.setKeySerializer(new StringRedisSerializer());
        this.redisTemplate.setValueSerializer(new StringRedisSerializer());
    }

    public String getValue(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value;
    }

    public boolean setKeyAndValue(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 锁前缀
     */
    static public String LOCK_PREFIX = "DCS_LOCK_";

    /**
     * 设置分布式锁
     *
     * @param key         锁标识
     * @param value       内容
     * @param lockTimeOut 锁有效时间（秒）
     * @param postPone
     * @return
     */
//    public Boolean lock(String key, String value, Long lockTimeOut, PostPone postPone) {
//        // "SET" "${key}" "\"${value}\"" "EX" "${lockTimeOut}" "NX"
//        // setIfAbsent 对应redis命令如上，通过set命令的参数NX，完成排斥锁和原子性
//        Boolean locked = redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + key, value, lockTimeOut,
//                TimeUnit.SECONDS);
//        //如果获取锁成功则启动一个延时线程
//        if (locked) {
//            //如果获取到锁了，启动一个延时线程，防止业务逻辑未执行完毕就因锁超时而使锁释放
//            Thread postponeThread = new Thread(new PostponeTask(LOCK_PREFIX + key, value, lockTimeOut, postPone, this));
//            //将该线程设置为守护线程
//            postponeThread.setDaemon(Boolean.TRUE);
//            postponeThread.start();
//        }
//        return locked;
//    }

    /**
     * 释放锁如果持有锁
     *
     * @param key
     * @param value
     * @return
     */
    public boolean unlock(String key, String value) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            Object result = jedis.eval(RELEASE_LOCK_SCRIPT, Collections.singletonList(LOCK_PREFIX + key),
                    Collections.singletonList(value));
            if (RELEASE_SUCCESS.equals(result)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        });
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 对锁进行延时如果此线程还占有该锁
     *
     * @param key
     * @param value
     * @param expireTime
     * @return
     */
    public Boolean postpone(String key, String value, long expireTime) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            Object result = jedis.eval(POSTPONE_LOCK_SCRIPT, Lists.newArrayList(key), Lists.newArrayList(value,
                    String.valueOf(expireTime)));
            if (POSTPONE_SUCCESS.equals(result)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        });
    }
}
