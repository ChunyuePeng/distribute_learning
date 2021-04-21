package com.pcy.distribute_learning.dao.service.impl;

import com.pcy.distribute_learning.dao.entity.Goods;
import com.pcy.distribute_learning.dao.repo.GoodsRepo;
import com.pcy.distribute_learning.dao.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: 彭椿悦
 * @data: 2021/4/20 17:29
 */
@Service
public class GoodsServiceImpl implements GoodsService {
    GoodsRepo repo;

    @Autowired
    public void setRepo(GoodsRepo repo) {
        this.repo = repo;
    }

    @Override
    public Goods save(Goods goods) {
        return repo.save(goods);
    }
}
