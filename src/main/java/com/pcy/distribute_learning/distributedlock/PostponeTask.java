package com.pcy.distribute_learning.distributedlock;

/**
 * @description:
 * @author: 彭椿悦
 * @data: 2021/4/20 20:17
 */
public class PostponeTask implements Runnable {
    /**
     * 锁名
     */
    private String key;
    /**
     * 锁名所对应的值
     */
    private String value;
    /**
     * 设置的过期时间
     */
    private long expireTime;
    private Postpone postPone;
    private DistributedLock distributedLock;

    public PostponeTask(String key, String value, long expireTime,  DistributedLock distributedLock,Postpone postPone) {
        this.key = key;
        this.value = value;
        this.expireTime = expireTime;
        this.distributedLock = distributedLock;
        this.postPone = postPone;
    }

    @Override
    public void run() {
        //等待waitTime之后对锁续期
        long waitTime = expireTime * 1000 * 2 / 3;
        while (!postPone.needStopPostPone()) {
            try {
                Thread.sleep(waitTime);
                //延时成功
                if (distributedLock.postpone(key,value,expireTime)) {
                }
                //延时失败
                else {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
