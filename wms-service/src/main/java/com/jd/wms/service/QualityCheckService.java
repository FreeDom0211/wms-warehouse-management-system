package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.QualityCheck;

import java.util.List;
import java.util.Map;

public interface QualityCheckService extends IService<QualityCheck> {

    Map<String, Object> createInboundCheck(Long productId, String batchNo, Long locationId,
                                           Integer sampleQuantity, Integer qualifiedQuantity,
                                           Integer unqualifiedQuantity, String qualityIssue);

    Map<String, Object> createPeriodicCheck(Long productId, Long locationId);

    List<Map<String, Object>> getPendingChecks();

    List<Map<String, Object>> getCheckList(Map<String, Object> params);

    Map<String, Object> getCheckDetails(Long checkId);

    boolean submitCheckResult(Long checkId, String checkResult, String qualityIssue);

    boolean auditCheck(Long checkId, String auditorResult, String auditorRemark);

    boolean initiateReturnProcess(Long checkId);

    boolean initiateScrapProcess(Long checkId);

}