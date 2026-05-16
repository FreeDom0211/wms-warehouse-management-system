package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.Inventory;

public interface InventoryService extends IService<Inventory> {

    Inventory getByProductAndLocation(Long productId, Long locationId);

    boolean addInventory(Inventory inventory);

    boolean updateInventory(Inventory inventory);

    boolean deleteInventory(Long id);

}