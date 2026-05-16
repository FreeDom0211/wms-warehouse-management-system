package com.jd.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.StockIn;

import java.util.List;
import java.util.Map;

public interface StockInService extends IService<StockIn> {

    StockIn getByStockInNo(String stockInNo);

    boolean addStockIn(StockIn stockIn);

    boolean updateStockIn(StockIn stockIn);

    boolean deleteStockIn(Long id);

    List<StockIn> getPendingTasks(Long operatorId);

    Map<String, Object> createStockInTask(Long operatorId, Long productId, String batchNo, 
                                          Long locationId, Integer quantity, String remark);

    boolean completeStockIn(Long id);

    List<Map<String, Object>> getFreeLocations(Long warehouseId);

}