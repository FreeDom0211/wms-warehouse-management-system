package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.Warehouse;
import com.jd.wms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/warehouse")public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<Warehouse> page = new Page<>(pageNum, pageSize);
        IPage<Warehouse> resultPage = warehouseService.page(page);
        for (Warehouse warehouse : resultPage.getRecords()) {
            if (warehouse.getWarehouseName() == null || warehouse.getWarehouseName().isEmpty()) {
                warehouse.setWarehouseName(warehouse.getName());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "");
        result.put("count", resultPage.getTotal());
        result.put("data", resultPage.getRecords());
        return result;
    }

    @GetMapping("/{id}")
    public Result<Warehouse> getById(@PathVariable Long id) {
        Warehouse warehouse = warehouseService.getById(id);
        return Result.success(warehouse);
    }

    @PostMapping
    public Result<Warehouse> add(@RequestBody Warehouse warehouse) {
        warehouseService.addWarehouse(warehouse);
        return Result.success("添加成功", warehouse);    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        if (warehouse.getId() != null && warehouse.getId() != id) {
            warehouseService.updateWarehouseId(id, warehouse.getId());
        }
        warehouse.setId(id);
        warehouseService.updateWarehouse(warehouse);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return Result.success("删除成功");
    }

    @GetMapping("/all")
    public Result<List<Warehouse>> getAll() {
        List<Warehouse> list = warehouseService.list();
        return Result.success(list);
    }

    @GetMapping("/{id}/zones")
    public Result<Map<String, Object>> getZones(@PathVariable Long id) {
        Warehouse warehouse = warehouseService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("warehouseId", id);
        result.put("warehouseName", warehouse.getName());
        result.put("zoneInfo", warehouse.getZoneInfo());
        if (warehouse.getZoneInfo() != null) {
            String[] zones = warehouse.getZoneInfo().split(",");
            Map<String, String> zoneMap = new HashMap<>();
            for (String zone : zones) {
                String[] parts = zone.split(":");
                if (parts.length == 2) {
                    zoneMap.put(parts[0].trim(), parts[1].trim());
                }
            }
            result.put("zones", zoneMap);
        }
        return Result.success(result);
    }

    @PutMapping("/{id}/zones")
    public Result<Void> updateZones(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Warehouse warehouse = warehouseService.getById(id);
        String zoneInfo = (String) request.get("zoneInfo");
        warehouse.setZoneInfo(zoneInfo);
        warehouseService.updateById(warehouse);
        return Result.success("库区配置更新成功");
    }

}