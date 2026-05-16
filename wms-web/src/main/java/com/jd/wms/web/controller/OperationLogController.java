package com.jd.wms.web.controller;

import com.jd.wms.common.annotation.OperationLog;
import com.jd.wms.common.annotation.RateLimiter;
import com.jd.wms.common.vo.Result;
import com.jd.wms.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/log")public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")    @OperationLog(module = "系统管理", operation = "查询操作日志")
    public Result<List<Map<String, Object>>> getLogList(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false, defaultValue = "100") Integer limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("module", module);
        params.put("operation", operation);
        params.put("operatorId", operatorId);
        params.put("status", status);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("limit", limit);
        List<Map<String, Object>> logs = operationLogService.getLogList(params);
        return Result.success(logs);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = operationLogService.getLogStatistics();
        return Result.success(stats);
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "系统管理", operation = "删除操作日志")
    public Result<Void> deleteLog(@PathVariable Long id) {
        operationLogService.deleteLog(id);
        return Result.success("删除成功");
    }

    @DeleteMapping("/clear/{days}")
    @OperationLog(module = "系统管理", operation = "清理旧日志")
    public Result<Void> clearOldLogs(@PathVariable int days) {
        operationLogService.clearOldLogs(days);
        return Result.success("清理成功");
    }

}
