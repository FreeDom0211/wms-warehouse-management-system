package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.Goods;

public interface GoodsService extends IService<Goods> {

    boolean addGoods(Goods goods);

    boolean updateGoods(Goods goods);

    boolean deleteGoods(Long id);

}