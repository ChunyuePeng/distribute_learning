package com.pcy.distribute_learning.dao.repo;

import com.pcy.distribute_learning.dao.entity.Goods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @description:
 * @author: 彭椿悦
 * @data: 2021/4/20 17:25
 */
@Repository
public interface GoodsRepo extends JpaRepository<Goods, Integer> {
}
