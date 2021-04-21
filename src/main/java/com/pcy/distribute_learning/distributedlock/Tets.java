package com.pcy.distribute_learning.distributedlock;

import com.pcy.distribute_learning.DistributeLearningApplication;
import com.pcy.distribute_learning.RedisService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @description:
 * @author: 彭椿悦
 * @data: 2021/4/20 17:31
 */
@SpringBootTest(classes = DistributeLearningApplication.class)
@RunWith(SpringRunner.class)
public class Tets {
    @Autowired
    RedisService redisService;

//    @Test
//    public void test() {
//        System.out.println(redisService.getValue("goods_1"));
//        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
//        threadPoolTaskExecutor.setCorePoolSize(2);
//        threadPoolTaskExecutor.initialize();
//        threadPoolTaskExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                int amount = 0;
//                while ((amount = (int)redisService.getValue("goods_1")) > 0) {
//                    redisService.setKeyAndValue("goods_1",String.valueOf(amount-1));
//                    System.out.println("线程一将商品数量减一");
//                }
//            }
//        });
//        threadPoolTaskExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                int amount = 0;
//                while ((amount = (int)redisService.getValue("goods_1")) > 0) {
//                    redisService.setKeyAndValue("goods_1",String.valueOf(amount-1));
//                    System.out.println("线程二将商品数量减一");
//                }
//            }
//        });
//        threadPoolTaskExecutor.shutdown();
//    }
}
