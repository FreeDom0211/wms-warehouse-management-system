package com.jd.wms.web.controller;

import com.jd.wms.common.vo.Result;
import com.jd.wms.service.InventoryMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/inventory")
public class InventoryMonitorController {

    @Autowired
    private InventoryMonitorService inventoryMonitorService;

    @GetMapping("/overview")    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = inventoryMonitorService.getInventoryOverview();
        return Result.success(overview);
    }

    @GetMapping("/by-warehouse")    public Result<List<Map<String, Object>>> getByWarehouse() {
        try {
            List<Map<String, Object>> data = inventoryMonitorService.getInventoryByWarehouse();
            return Result.success(data);
        } catch (Exception e) {
            return Result.success(new java.util.ArrayList<>());
        }
    }

    @GetMapping("/by-zone")
    public Result<List<Map<String, Object>>> getByZone(@RequestParam Long warehouseId) {
        List<Map<String, Object>> data = inventoryMonitorService.getInventoryByZone(warehouseId);
        return Result.success(data);
    }

    @GetMapping("/by-category")
    public Result<List<Map<String, Object>>> getByCategory() {
        List<Map<String, Object>> data = inventoryMonitorService.getInventoryByCategory();
        return Result.success(data);
    }

    @GetMapping("/by-product")
    public Result<List<Map<String, Object>>> getByProduct(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId) {
        List<Map<String, Object>> data = inventoryMonitorService.getInventoryByProduct(warehouseId, productId);
        return Result.success(data);
    }

    @GetMapping("/location/{locationId}")
    public Result<Map<String, Object>> getLocationInventory(@PathVariable Long locationId) {
        Map<String, Object> data = inventoryMonitorService.getLocationInventory(locationId);
        return Result.success(data);
    }

    @GetMapping("/top-products")
    public Result<List<Map<String, Object>>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> data = inventoryMonitorService.getTopProducts(limit);
        return Result.success(data);
    }

    @GetMapping("/low-stock")
    public Result<Map<String, Object>> getLowStockProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> data = inventoryMonitorService.getLowStockProducts();
        
        int start = (page - 1) * limit;
        int end = Math.min(start + limit, data.size());
        List<Map<String, Object>> pageData = start < data.size() ? data.subList(start, end) : new java.util.ArrayList<>();
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("code", 0);
        result.put("msg", "");
        result.put("count", data.size());
        result.put("data", pageData);
        
        return Result.success(result);
    }

    @GetMapping("/trend")    public Result<Map<String, Object>> getInventoryTrend(
            @RequestParam(defaultValue = "week") String dateRange) {
        Map<String, Object> data = inventoryMonitorService.getInventoryTrend(dateRange);
        return Result.success(data);
    }

}
