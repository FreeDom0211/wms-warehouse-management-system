package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.PerformanceStats;

import java.util.List;
import java.util.Map;

public interface PerformanceService extends IService<PerformanceStats> {

    Map<String, Object> getPerformanceOverview(Long operatorId);

    List<Map<String, Object>> getOperatorPerformanceList(Map<String, Object> params);

    Map<String, Object> getOperatorDetailPerformance(Long operatorId, String startDate, String endDate);

    boolean generateDailyStats();

    byte[] exportPerformanceReport(String startDate, String endDate);

    Map<String, Object> getWorkloadStats(Long operatorId, String startDate, String endDate);

    Map<String, Object> getAccuracyStats(Long operatorId, String startDate, String endDate);

    Map<String, Object> getHandleTimeStats(Long operatorId, String startDate, String endDate);

}