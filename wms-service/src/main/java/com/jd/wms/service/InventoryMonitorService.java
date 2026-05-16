package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.InventoryAlert;

import java.util.List;
import java.util.Map;

public interface InventoryMonitorService extends IService<InventoryAlert> {

    Map<String, Object> getInventoryOverview();

    List<Map<String, Object>> getInventoryByWarehouse();

    List<Map<String, Object>> getInventoryByZone(Long warehouseId);

    List<Map<String, Object>> getInventoryByCategory();

    List<Map<String, Object>> getInventoryByProduct(Long warehouseId, Long productId);

    Map<String, Object> getLocationInventory(Long locationId);

    List<Map<String, Object>> getTopProducts(int limit);

    List<Map<String, Object>> getLowStockProducts();

    Map<String, Object> getInventoryTrend(String dateRange);

}