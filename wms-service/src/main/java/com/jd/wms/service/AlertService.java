package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.InventoryAlert;

import java.util.List;
import java.util.Map;

public interface AlertService extends IService<InventoryAlert> {

    boolean setProductThreshold(Long productId, Integer minStock, Integer maxStock);

    Map<String, Object> getAlertOverview();

    List<Map<String, Object>> getAlertList(Map<String, Object> params);

    List<Map<String, Object>> getPendingAlerts();

    boolean handleAlert(Long alertId, Long handlerId, String handleResult);

    boolean dismissAlert(Long alertId);

    boolean checkInventoryAlerts();

    boolean checkShelfLifeAlerts();

    List<Map<String, Object>> getAlertStatistics();

    List<Map<String, Object>> getAlertTrend();

}