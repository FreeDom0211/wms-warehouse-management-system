package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.Warehouse;

public interface WarehouseService extends IService<Warehouse> {

    Warehouse getByName(String name);

    boolean addWarehouse(Warehouse warehouse);

    boolean updateWarehouse(Warehouse warehouse);

    boolean deleteWarehouse(Long id);

    boolean batchGenerateLocations(Long warehouseId, String zoneInfo, String rulePattern);

    boolean updateWarehouseId(Long oldId, Long newId);

}