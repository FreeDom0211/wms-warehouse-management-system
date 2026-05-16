package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.InventoryCheck;

import java.util.List;
import java.util.Map;

public interface InventoryCheckService extends IService<InventoryCheck> {

    InventoryCheck getByCheckNo(String checkNo);

    boolean addInventoryCheck(InventoryCheck check);

    boolean updateInventoryCheck(InventoryCheck check);

    boolean deleteInventoryCheck(Long id);

    Map<String, Object> createCheckTask(Long operatorId, String zone);

    List<Map<String, Object>> getCheckList(Long operatorId);

    Map<String, Object> getCheckDetails(Long checkId);

    boolean submitCheckResult(Long checkId, List<Map<String, Object>> actualQuantities);

}