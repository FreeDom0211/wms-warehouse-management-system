package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.Goods;
import com.jd.wms.dao.mapper.GoodsMapper;
import com.jd.wms.service.GoodsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Override
    @Transactional
    public boolean addGoods(Goods goods) {
        goods.setStatus(1);
        goods.setCreateTime(new Date());
        goods.setUpdateTime(new Date());
        return save(goods);
    }

    @Override
    @Transactional
    public boolean updateGoods(Goods goods) {
        goods.setUpdateTime(new Date());
        return updateById(goods);
    }

    @Override
    @Transactional
    public boolean deleteGoods(Long id) {
        Goods goods = getById(id);
        if (goods == null) {
            throw new WmsException("商品不存在");
        }
        return removeById(id);
    }

}