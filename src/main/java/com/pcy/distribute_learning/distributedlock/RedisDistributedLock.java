package com.pcy.distribute_learning.distributedlock;

import com.google.common.collect.Lists;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: 彭椿悦
 * @data: 2021/4/21 16:35
 */
@Component
public class RedisDistributedLock implements DistributedLock,LockPostpone {
    private static final Long DEFAULT_EXPIRED_TIME = 1L;
    private static final int DEFAULT_TIMEOUT = 200;
    private boolean needPostpone = true;
    private boolean needStopPostpone = false;
    private RedisTemplate<String, String> redisTemplate;
    public static final Long RELEASE_SUCCESS = 1L;
    private static final Long POSTPONE_SUCCESS = 1L;
    public static final String SET_IF_NOT_EXIST = "NX";
    public static final String SET_WITH_EXPIRE_TIME = "EX";
    private static Map<String, Postpone> postponeMap = new ConcurrentHashMap<>();
    /**
     * 锁前缀
     */
    static public String LOCK_PREFIX = "DCS_LOCK_";
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
    @Override
    public Boolean lock(String lockName, String uniqueStr) {
        return this.lock(lockName,uniqueStr,DEFAULT_EXPIRED_TIME);
    }

    @Override
    public Boolean lock(String lockName, String uniqueStr, int timeout) {
        return lock(lockName,uniqueStr,DEFAULT_EXPIRED_TIME,timeout);
    }

    @Override
    public Boolean lock(String lockName, String uniqueStr, Long expireTime) {
        return lock(lockName,uniqueStr,expireTime,DEFAULT_TIMEOUT);
    }

    @Override
    public Boolean lock(String lockName, String uniqueStr, Long expireTime, int timeout) {
        Boolean locked = false;
        long begin = System.currentTimeMillis();
        while ((System.currentTimeMillis()-begin)<=timeout){
            locked = redisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + lockName, uniqueStr, expireTime,
                    TimeUnit.SECONDS);
            if (locked){
                break;
            }
        }
        // SET key value EX lockTimeOut NX
        //如果获取锁成功则启动一个延时线程
        if (locked) {
            //如果获取到锁了，启动一个延时线程，防止业务逻辑未执行完毕就因锁超时而使锁释放
            Postpone postpone = new DefaultPostpone();
            postponeMap.put(uniqueStr,postpone);
            Thread postponeThread = new Thread(new PostponeTask(LOCK_PREFIX + lockName, uniqueStr, expireTime,
                    this,postpone));
            //将该线程设置为守护线程
            postponeThread.setDaemon(Boolean.TRUE);
            postponeThread.start();
        }
        return locked;
    }

    @Override
    public Boolean unlock(String lockName, String uniqueStr) {
        Postpone postpone = postponeMap.get(uniqueStr);
        postpone.stopPostPone();
        postponeMap.remove(uniqueStr);
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            Object result = jedis.eval(RELEASE_LOCK_SCRIPT, Collections.singletonList(LOCK_PREFIX + lockName),
                    Collections.singletonList(uniqueStr));
            if (RELEASE_SUCCESS.equals(result)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        });
    }

    @Override
    public Boolean postpone(String lockName, String uniqueStr, long expireTime) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            Object result = jedis.eval(POSTPONE_LOCK_SCRIPT, Lists.newArrayList(lockName), Lists.newArrayList(uniqueStr,
                    String.valueOf(expireTime)));
            if (POSTPONE_SUCCESS.equals(result)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        });
    }

    @Override
    public boolean needPostpone() {
        return needPostpone;
    }


}
