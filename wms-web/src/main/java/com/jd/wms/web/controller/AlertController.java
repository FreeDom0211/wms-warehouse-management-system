package com.jd.wms.web.controller;

import com.jd.wms.common.vo.Result;
import com.jd.wms.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/alert")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @PutMapping("/threshold")
    public Result<Void> setThreshold(@RequestBody Map<String, Object> request) {
        Long productId = Long.parseLong(request.get("productId").toString());
        Integer minStock = request.get("minStock") != null ? Integer.parseInt(request.get("minStock").toString()) : null;
        Integer maxStock = request.get("maxStock") != null ? Integer.parseInt(request.get("maxStock").toString()) : null;

        alertService.setProductThreshold(productId, minStock, maxStock);
        return Result.success("阈值设置成功");    }

    @GetMapping("/overview")    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = alertService.getAlertOverview();
        return Result.success(overview);
    }

    @GetMapping("/list")    public Result<Map<String, Object>> getAlertList(
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long productId) {
        try {
            Map<String, Object> params = new java.util.HashMap<>();
            params.put("alertType", alertType);
            params.put("status", status);
            params.put("productId", productId);
            List<Map<String, Object>> alerts = alertService.getAlertList(params);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", 0);
            result.put("msg", "");
            result.put("count", alerts.size());
            result.put("data", alerts);
            
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

    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> getPendingAlerts() {
        List<Map<String, Object>> alerts = alertService.getPendingAlerts();
        return Result.success(alerts);
    }

    @PostMapping("/{id}/handle")
    public Result<Void> handleAlert(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Long handlerId = Long.parseLong(request.get("handlerId").toString());
        String handleResult = (String) request.get("handleResult");

        alertService.handleAlert(id, handlerId, handleResult);
        return Result.success("预警已处理");    }

    @PostMapping("/{id}/dismiss")
    public Result<Void> dismissAlert(@PathVariable Long id) {
        alertService.dismissAlert(id);
        return Result.success("预警已忽略");    }

    @PostMapping("/check-inventory")
    public Result<Void> checkInventoryAlerts() {
        alertService.checkInventoryAlerts();
        return Result.success("库存预警检查完成");    }

    @PostMapping("/check-shelf-life")
    public Result<Void> checkShelfLifeAlerts() {
        alertService.checkShelfLifeAlerts();
        return Result.success("保质期预警检查完成");    }

    @GetMapping("/statistics")
    public Result<List<Map<String, Object>>> getStatistics() {
        List<Map<String, Object>> stats = alertService.getAlertStatistics();
        return Result.success(stats);
    }

    @GetMapping("/trend")    public Result<List<Map<String, Object>>> getTrend() {
        List<Map<String, Object>> trend = alertService.getAlertTrend();
        return Result.success(trend);
    }

}