package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.StockOut;

import java.util.List;
import java.util.Map;

public interface StockOutService extends IService<StockOut> {

    StockOut getByStockOutNo(String stockOutNo);

    boolean addStockOut(StockOut stockOut);

    boolean updateStockOut(StockOut stockOut);

    boolean deleteStockOut(Long id);

    List<StockOut> getPendingTasks(Long operatorId);

    Map<String, Object> simulateOrderDetail(String orderNo);

    List<Map<String, Object>> recommendLocations(Long productId, Integer quantity);

    Map<String, Object> createStockOutTask(Long operatorId, String orderNo, Long productId, 
                                           String batchNo, Long locationId, Integer quantity, String remark);

    boolean completeStockOut(Long id);

}