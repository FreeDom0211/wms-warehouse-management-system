package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.Inventory;
import com.jd.wms.dao.mapper.InventoryMapper;
import com.jd.wms.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Override
    public Inventory getByProductAndLocation(Long productId, Long locationId) {
        return baseMapper.selectByProductAndLocation(productId, locationId);
    }

    @Override
    @Transactional
    public boolean addInventory(Inventory inventory) {
        inventory.setCreateTime(new Date());
        inventory.setUpdateTime(new Date());
        return save(inventory);
    }

    @Override
    @Transactional
    public boolean updateInventory(Inventory inventory) {
        inventory.setUpdateTime(new Date());
        return updateById(inventory);
    }

    @Override
    @Transactional
    public boolean deleteInventory(Long id) {
        Inventory inventory = getById(id);
        if (inventory == null) {
            throw new WmsException("库存记录不存在");
        }
        return removeById(id);
    }

}