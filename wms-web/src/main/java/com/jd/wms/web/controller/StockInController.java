package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.StockIn;
import com.jd.wms.service.StockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock-in")
public class StockInController {

    @Autowired
    private StockInService stockInService;

    @GetMapping
    public Result<IPage<StockIn>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String status) {
        QueryWrapper<StockIn> wrapper = new QueryWrapper<>();
        if (operatorId != null) {
            wrapper.eq("operator_id", operatorId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        IPage<StockIn> page = stockInService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<StockIn> getById(@PathVariable Long id) {
        StockIn stockIn = stockInService.getById(id);
        return Result.success(stockIn);
    }

    @PostMapping
    public Result<Void> add(@RequestBody StockIn stockIn) {
        stockInService.addStockIn(stockIn);
        return Result.success("入库成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody StockIn stockIn) {
        stockIn.setId(id);
        stockInService.updateStockIn(stockIn);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stockInService.deleteStockIn(id);
        return Result.success("删除成功");
    }

    @GetMapping("/tasks/pending")
    public Result<List<StockIn>> getPendingTasks(@RequestParam Long operatorId) {
        List<StockIn> tasks = stockInService.getPendingTasks(operatorId);
        return Result.success(tasks);
    }

    @PostMapping("/task")    public Result<Map<String, Object>> createTask(@RequestBody Map<String, Object> request) {
        Long operatorId = Long.parseLong(request.get("operatorId").toString());
        Long productId = Long.parseLong(request.get("productId").toString());
        String batchNo = (String) request.get("batchNo");
        Long locationId = Long.parseLong(request.get("locationId").toString());
        Integer quantity = Integer.parseInt(request.get("quantity").toString());
        String remark = (String) request.get("remark");
        
        Map<String, Object> result = stockInService.createStockInTask(operatorId, productId, batchNo, 
                                                                     locationId, quantity, remark);
        return Result.success(result);
    }

    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        stockInService.completeStockIn(id);
        return Result.success("入库完成");
    }

    @GetMapping("/free-locations")
    public Result<List<Map<String, Object>>> getFreeLocations(@RequestParam Long warehouseId) {
        List<Map<String, Object>> locations = stockInService.getFreeLocations(warehouseId);
        return Result.success(locations);
    }

    @GetMapping("/operator/{operatorId}")
    public Result<IPage<StockIn>> getByOperator(
            @PathVariable Long operatorId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<StockIn> wrapper = new QueryWrapper<>();
        wrapper.eq("operator_id", operatorId);
        IPage<StockIn> page = stockInService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

}