package com.pcy.distribute_learning.distributedlock.service.impl;

import com.pcy.distribute_learning.RedisService;
import com.pcy.distribute_learning.distributedlock.DistributedLock;
import com.pcy.distribute_learning.distributedlock.Postpone;
import com.pcy.distribute_learning.distributedlock.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

/**
 * @description:
 * @author: 彭椿悦
 * @data: 2021/4/20 20:09
 */
@Service
public class OrderServiceImpl implements OrderService{
    RedisService redisService;
    DistributedLock distributedLock;
    private int sa = 0;

    @Autowired
    public void setDistributedLock(DistributedLock distributedLock) {
        this.distributedLock = distributedLock;
    }

    @Autowired
    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public String order() {
        int amount = 0;
        int retryTimes = 0;
        String uuid = UUID.randomUUID().toString();
        //有库存，且获取锁失败未超过三次
        while ((amount = Integer.valueOf(redisService.getValue("goods_1")))>0&&retryTimes<3){
            Boolean lock = distributedLock.lock("goods_1",
                    uuid);
            if (lock){
                try {
                    //如果还有库存，减少库存
                    if ((amount = Integer.valueOf(redisService.getValue("goods_1")))>0){
                        redisService.setKeyAndValue("goods_1",String.valueOf(amount-1));
                        sa++;
                        System.out.println("卖出商品数"+sa);
                        return "恭喜你，下单成功";
                    }
                }
                //无论如何都要释放掉锁
                finally {
                    //解决超时情况下的误删除问题
                    distributedLock.unlock("goods_1",uuid);
                }
            }
            retryTimes++;
        }
//        while ((amount = Integer.valueOf(redisService.getValue("goods_1")))>0){
//            String uuid = UUID.randomUUID().toString();
//            if (distributedLock.lock("goods_1",
//                    uuid)){
//                try {
//                    if ((amount = Integer.valueOf(redisService.getValue("goods_1")))>0){
//                        redisService.setKeyAndValue("goods_1",String.valueOf(amount-1));
//                        sa++;
//                        System.out.println("卖出商品数"+sa);
//                        return "恭喜你，下单成功";
//                    }
//                }  finally {
//                    //解决超时情况下的误删除问题
//                    distributedLock.unlock("goods_1",uuid);
//                }
//            }
//        }
        System.out.println("卖出商品数"+sa);
        return "下单失败，没有库存了";
    }

}
