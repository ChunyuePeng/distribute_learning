package com.pcy.distribute_learning.distributedlock;


/**
 * @description:
 * @author: 彭椿悦
 * @data: 2021/4/21 17:21
 */
public class DefaultPostpone implements Postpone {
    private boolean needStopPostpone = false;
    @Override
    public boolean needStopPostPone() {
        return needStopPostpone;
    }

    @Override
    public void stopPostPone() {
        this.needStopPostpone = true;
    }
}
