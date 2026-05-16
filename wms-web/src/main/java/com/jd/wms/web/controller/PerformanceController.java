package com.jd.wms.web.controller;

import com.jd.wms.common.vo.Result;
import com.jd.wms.service.PerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/performance")public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @GetMapping("/overview")    public Result<Map<String, Object>> getOverview(
            @RequestParam(required = false) Long operatorId) {
        Map<String, Object> overview = performanceService.getPerformanceOverview(operatorId);
        return Result.success(overview);
    }

    @GetMapping("/list")    public Result<Map<String, Object>> getPerformanceList(
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> params = new java.util.HashMap<>();
            params.put("operatorId", operatorId);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            List<Map<String, Object>> list = performanceService.getOperatorPerformanceList(params);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", 0);
            result.put("msg", "");
            result.put("count", list.size());
            result.put("data", list);
            
            return Result.success(result);
        } catch (Exception e) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", 0);
            result.put("msg", e.getMessage());
            result.put("count", 0);
            result.put("data", new java.util.ArrayList<>());
            return Result.success(result);
        }
    }

    @GetMapping("/detail/{operatorId}")
    public Result<Map<String, Object>> getDetailPerformance(
            @PathVariable Long operatorId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> detail = performanceService.getOperatorDetailPerformance(operatorId, startDate, endDate);
        return Result.success(detail);
    }

    @GetMapping("/workload")
    public Result<Map<String, Object>> getWorkloadStats(
            @RequestParam Long operatorId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> stats = performanceService.getWorkloadStats(operatorId, startDate, endDate);
        return Result.success(stats);
    }

    @GetMapping("/accuracy")
    public Result<Map<String, Object>> getAccuracyStats(
            @RequestParam Long operatorId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> stats = performanceService.getAccuracyStats(operatorId, startDate, endDate);
        return Result.success(stats);
    }

    @GetMapping("/handle-time")
    public Result<Map<String, Object>> getHandleTimeStats(
            @RequestParam Long operatorId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> stats = performanceService.getHandleTimeStats(operatorId, startDate, endDate);
        return Result.success(stats);
    }

    @PostMapping("/generate-daily")
    public Result<Void> generateDailyStats() {
        performanceService.generateDailyStats();
        return Result.success("日统计生成成");    }

    @GetMapping("/export")
    public Result<byte[]> exportReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        byte[] excelData = performanceService.exportPerformanceReport(startDate, endDate);
        return Result.success(excelData);
    }

}