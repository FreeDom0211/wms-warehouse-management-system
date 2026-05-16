package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.Location;
import com.jd.wms.service.LocationService;
import com.jd.wms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/location")public class LocationController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping
    public Result<IPage<Location>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String status) {
        QueryWrapper<Location> wrapper = new QueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq("warehouse_id", warehouseId);
        }
        if (zone != null && !zone.isEmpty()) {
            wrapper.eq("zone", zone);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        IPage<Location> resultPage = locationService.page(new Page<>(page, limit), wrapper);
        return Result.success(resultPage);
    }

    @GetMapping("/{id}")
    public Result<Location> getById(@PathVariable Long id) {
        Location location = locationService.getById(id);
        return Result.success(location);
    }

    @PostMapping
    public Result<Location> add(@RequestBody Location location) {
        locationService.addLocation(location);
        return Result.success("添加成功", location);    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Location location) {
        location.setId(id);
        locationService.updateLocation(location);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return Result.success("删除成功");
    }

    @PostMapping("/batch-generate")
    public Result<Void> batchGenerate(@RequestBody Map<String, Object> request) {
        Long warehouseId = Long.parseLong(request.get("warehouseId").toString());
        String zoneInfo = (String) request.get("zoneInfo");
        String rulePattern = (String) request.get("rulePattern");
        warehouseService.batchGenerateLocations(warehouseId, zoneInfo, rulePattern);
        return Result.success("货位批量生成成功");
    }

    @GetMapping("/warehouse/{warehouseId}")
    public Result<List<Location>> getByWarehouse(@PathVariable Long warehouseId) {
        QueryWrapper<Location> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", warehouseId);
        List<Location> list = locationService.list(wrapper);
        return Result.success(list);
    }

    @GetMapping("/warehouse/{warehouseId}/zones")
    public Result<List<String>> getZonesByWarehouse(@PathVariable Long warehouseId) {
        QueryWrapper<Location> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", warehouseId).select("zone").groupBy("zone");
        List<Location> list = locationService.list(wrapper);
        List<String> zones = list.stream().map(Location::getZone).collect(java.util.stream.Collectors.toList());
        return Result.success(zones);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Location location = locationService.getById(id);
        location.setStatus(request.get("status"));
        locationService.updateById(location);
        return Result.success("状态更新成功");    }

    @PostMapping("/move")
    public Result<Void> moveInventory(@RequestBody Map<String, Object> request) {
        Long fromLocationId = Long.parseLong(request.get("fromLocationId").toString());
        Long toLocationId = Long.parseLong(request.get("toLocationId").toString());
        Long productId = Long.parseLong(request.get("productId").toString());
        String batchNo = (String) request.get("batchNo");
        Integer quantity = Integer.parseInt(request.get("quantity").toString());
        
        locationService.moveInventory(fromLocationId, toLocationId, productId, batchNo, quantity);
        return Result.success("移库成功");
    }

    @GetMapping("/{id}/inventory")
    public Result<List<Map<String, Object>>> getLocationInventory(@PathVariable Long id) {
        List<Map<String, Object>> inventory = locationService.getLocationInventory(id);
        return Result.success(inventory);
    }

}