package com.pcy.distribute_learning.distributedlock;

import com.pcy.distribute_learning.RedisService;
import com.pcy.distribute_learning.distributedlock.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @description:
 * @author: 彭椿悦
 * @data: 2021/4/20 18:44
 */
@RestController
public class DistributedLockController {
    OrderService orderService;

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @RequestMapping("order1")
    public String order1() {
        return orderService.order();
    }
}
