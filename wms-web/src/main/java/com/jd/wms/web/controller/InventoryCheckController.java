package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.InventoryCheck;
import com.jd.wms.service.InventoryCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/check")public class InventoryCheckController {

    @Autowired
    private InventoryCheckService inventoryCheckService;

    @GetMapping
    public Result<IPage<InventoryCheck>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String status) {
        QueryWrapper<InventoryCheck> wrapper = new QueryWrapper<>();
        if (operatorId != null) {
            wrapper.eq("operator_id", operatorId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        IPage<InventoryCheck> page = inventoryCheckService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<InventoryCheck> getById(@PathVariable Long id) {
        InventoryCheck check = inventoryCheckService.getById(id);
        return Result.success(check);
    }

    @PostMapping
    public Result<InventoryCheck> add(@RequestBody InventoryCheck check) {
        inventoryCheckService.addInventoryCheck(check);
        return Result.success("创建成功", check);    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody InventoryCheck check) {
        check.setId(id);
        inventoryCheckService.updateInventoryCheck(check);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        inventoryCheckService.deleteInventoryCheck(id);
        return Result.success("删除成功");
    }

    @PostMapping("/task")    public Result<Map<String, Object>> createTask(@RequestBody Map<String, Object> request) {
        Long operatorId = Long.parseLong(request.get("operatorId").toString());
        String zone = (String) request.get("zone");
        
        Map<String, Object> result = inventoryCheckService.createCheckTask(operatorId, zone);
        return Result.success(result);
    }

    @GetMapping("/operator/{operatorId}")
    public Result<IPage<InventoryCheck>> getCheckList(
            @PathVariable Long operatorId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<InventoryCheck> wrapper = new QueryWrapper<>();
        wrapper.eq("operator_id", operatorId);
        IPage<InventoryCheck> page = inventoryCheckService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @GetMapping("/{id}/details")
    public Result<Map<String, Object>> getCheckDetails(@PathVariable Long id) {
        Map<String, Object> details = inventoryCheckService.getCheckDetails(id);
        return Result.success(details);
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submitCheckResult(@PathVariable Long id, @RequestBody List<Map<String, Object>> actualQuantities) {
        inventoryCheckService.submitCheckResult(id, actualQuantities);
        return Result.success("盘点完成");
    }

}