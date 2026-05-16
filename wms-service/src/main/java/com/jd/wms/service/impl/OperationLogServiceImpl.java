package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.dao.entity.OperationLog;
import com.jd.wms.dao.mapper.OperationLogMapper;
import com.jd.wms.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public List<Map<String, Object>> getLogList(Map<String, Object> params) {
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();

        if (params.get("module") != null && !params.get("module").toString().isEmpty()) {
            wrapper.eq("module", params.get("module"));
        }
        if (params.get("operation") != null && !params.get("operation").toString().isEmpty()) {
            wrapper.eq("operation", params.get("operation"));
        }
        if (params.get("operatorId") != null) {
            wrapper.eq("operator_id", params.get("operatorId"));
        }
        if (params.get("status") != null && !params.get("status").toString().isEmpty()) {
            wrapper.eq("status", params.get("status"));
        }
        if (params.get("startTime") != null) {
            wrapper.ge("create_time", params.get("startTime"));
        }
        if (params.get("endTime") != null) {
            wrapper.le("create_time", params.get("endTime"));
        }

        wrapper.orderByDesc("create_time");

        if (params.get("limit") != null) {
            wrapper.last("LIMIT " + params.get("limit"));
        }

        List<OperationLog> logs = list(wrapper);
        return logs.stream().map(this::convertLogToMap).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getLogStatistics() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();

        long total = count(wrapper);
        wrapper.eq("status", "SUCCESS");
        long successCount = count(wrapper);

        wrapper.clear();
        wrapper.eq("status", "FAILED");
        long failedCount = count(wrapper);

        wrapper.clear();
        wrapper.select("module", "COUNT(*) as count")
               .groupBy("module");
        List<Map<String, Object>> moduleStats = listMaps(wrapper);

        wrapper.clear();
        wrapper.select("operation", "COUNT(*) as count")
               .groupBy("operation")
               .orderByDesc("count")
               .last("LIMIT 10");
        List<Map<String, Object>> operationStats = listMaps(wrapper);

        result.put("total", total);
        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        result.put("successRate", total > 0 ? (successCount * 100.0 / total) : 100.0);
        result.put("moduleStats", moduleStats);
        result.put("operationStats", operationStats);

        return result;
    }

    @Override
    public boolean deleteLog(Long id) {
        return removeById(id);
    }

    @Override
    public boolean clearOldLogs(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        Date threshold = calendar.getTime();

        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
        wrapper.le("create_time", threshold);
        return remove(wrapper);
    }

    private Map<String, Object> convertLogToMap(OperationLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("module", log.getModule());
        map.put("operation", log.getOperation());
        map.put("method", log.getMethodName());
        map.put("requestUrl", log.getRequestUrl());
        map.put("requestMethod", log.getRequestMethod());
        map.put("params", truncateString(log.getRequestParams(), 200));
        map.put("responseResult", truncateString(log.getResponseResult(), 200));
        map.put("operatorId", log.getOperatorId());
        map.put("operator", log.getOperatorName());
        map.put("ip", log.getOperatorIp());
        map.put("executionTime", log.getExecutionTime());
        map.put("status", log.getStatus());
        map.put("errorMessage", log.getErrorMessage());
        map.put("createTime", log.getCreateTime());
        return map;
    }

    private String truncateString(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }

}
