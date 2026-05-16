package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.StockOut;
import com.jd.wms.service.StockOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock-out")
public class StockOutController {

    @Autowired
    private StockOutService stockOutService;

    @GetMapping
    public Result<IPage<StockOut>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String status) {
        QueryWrapper<StockOut> wrapper = new QueryWrapper<>();
        if (operatorId != null) {
            wrapper.eq("operator_id", operatorId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        IPage<StockOut> page = stockOutService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<StockOut> getById(@PathVariable Long id) {
        StockOut stockOut = stockOutService.getById(id);
        return Result.success(stockOut);
    }

    @PostMapping
    public Result<Void> add(@RequestBody StockOut stockOut) {
        stockOutService.addStockOut(stockOut);
        return Result.success("出库成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody StockOut stockOut) {
        stockOut.setId(id);
        stockOutService.updateStockOut(stockOut);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stockOutService.deleteStockOut(id);
        return Result.success("删除成功");
    }

    @GetMapping("/tasks/pending")
    public Result<List<StockOut>> getPendingTasks(@RequestParam Long operatorId) {
        List<StockOut> tasks = stockOutService.getPendingTasks(operatorId);
        return Result.success(tasks);
    }

    @GetMapping("/order-detail")
    public Result<Map<String, Object>> getOrderDetail(@RequestParam String orderNo) {
        Map<String, Object> order = stockOutService.simulateOrderDetail(orderNo);
        return Result.success(order);
    }

    @GetMapping("/recommend-locations")
    public Result<List<Map<String, Object>>> recommendLocations(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        List<Map<String, Object>> locations = stockOutService.recommendLocations(productId, quantity);
        return Result.success(locations);
    }

    @PostMapping("/task")    public Result<Map<String, Object>> createTask(@RequestBody Map<String, Object> request) {
        Long operatorId = Long.parseLong(request.get("operatorId").toString());
        String orderNo = (String) request.get("orderNo");
        Long productId = Long.parseLong(request.get("productId").toString());
        String batchNo = (String) request.get("batchNo");
        Long locationId = Long.parseLong(request.get("locationId").toString());
        Integer quantity = Integer.parseInt(request.get("quantity").toString());
        String remark = (String) request.get("remark");
        
        Map<String, Object> result = stockOutService.createStockOutTask(operatorId, orderNo, productId, 
                                                                      batchNo, locationId, quantity, remark);
        return Result.success(result);
    }

    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        stockOutService.completeStockOut(id);
        return Result.success("出库完成");
    }

    @GetMapping("/operator/{operatorId}")
    public Result<IPage<StockOut>> getByOperator(
            @PathVariable Long operatorId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<StockOut> wrapper = new QueryWrapper<>();
        wrapper.eq("operator_id", operatorId);
        IPage<StockOut> page = stockOutService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

}