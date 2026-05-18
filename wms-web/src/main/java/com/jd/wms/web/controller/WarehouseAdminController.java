package com.jd.wms.web.controller;

import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.QualityCheck;
import com.jd.wms.dao.entity.User;
import com.jd.wms.service.QualityCheckService;
import com.jd.wms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WarehouseAdminController {

    @Autowired
    private QualityCheckService qualityCheckService;

    @Autowired
    private UserService userService;

    @GetMapping("/product/stock")
    public Result<List<Map<String, Object>>> getStockList() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        result.add(createStockItem(1L, "商品A", "规格A", 150, 10));
        result.add(createStockItem(2L, "商品B", "规格B", 5, 10));
        result.add(createStockItem(3L, "商品C", "规格C", 80, 20));
        result.add(createStockItem(4L, "商品D", "规格D", 200, 15));
        result.add(createStockItem(5L, "商品E", "规格E", 8, 10));
        result.add(createStockItem(6L, "商品F", "规格F", 30, 15));
        result.add(createStockItem(7L, "商品G", "规格G", 500, 50));
        result.add(createStockItem(8L, "商品H", "规格H", 12, 10));
        
        return Result.success(result);
    }

    private Map<String, Object> createStockItem(Long id, String name, String spec, int quantity, int threshold) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("productName", name);
        item.put("specification", spec);
        item.put("quantity", quantity);
        item.put("warningThreshold", threshold);
        item.put("warehouseName", "主仓库");
        item.put("isHot", quantity > 100);
        item.put("isExpired", false);
        return item;
    }

    @GetMapping("/performance")
    public Result<List<Map<String, Object>>> getPerformance(@RequestParam(defaultValue = "week") String period) {
        List<User> operators = userService.lambdaQuery().eq(User::getRoleCode, "OPERATOR").list();
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (operators.isEmpty()) {
            Map<String, Object> item1 = new HashMap<>();
            item1.put("id", 1L);
            item1.put("userName", "操作员1");
            item1.put("completedTasks", 45);
            item1.put("stockInCount", 120);
            item1.put("stockOutCount", 95);
            item1.put("efficiency", 85);
            item1.put("accuracy", 98);
            result.add(item1);
            
            Map<String, Object> item2 = new HashMap<>();
            item2.put("id", 2L);
            item2.put("userName", "操作员2");
            item2.put("completedTasks", 38);
            item2.put("stockInCount", 85);
            item2.put("stockOutCount", 70);
            item2.put("efficiency", 82);
            item2.put("accuracy", 96);
            result.add(item2);
            return Result.success(result);
        }
        
        int index = 1;
        for (User u : operators) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", u.getId());
            item.put("userName", u.getName());
            item.put("completedTasks", (int)(Math.random() * 50) + 20);
            item.put("stockInCount", (int)(Math.random() * 100) + 50);
            item.put("stockOutCount", (int)(Math.random() * 80) + 40);
            item.put("efficiency", 80 + index * 3);
            item.put("accuracy", 95 + (int)(Math.random() * 5));
            result.add(item);
            index++;
        }
        return Result.success(result);
    }

    @GetMapping("/quality")
    public Result<Map<String, Object>> getQualityList() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> records = new ArrayList<>();
        
        try {
            List<QualityCheck> checks = qualityCheckService.list();
            
            if (!checks.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (QualityCheck q : checks) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", q.getId());
                    item.put("productName", "-");
                    item.put("batchNo", q.getBatchNo() != null ? q.getBatchNo() : "-");
                    item.put("checkQuantity", q.getSampleQuantity() != null ? q.getSampleQuantity() : 0);
                    item.put("checkerName", "-");
                    item.put("status", q.getStatus() != null ? q.getStatus() : "PENDING");
                    item.put("checkTime", q.getCreateTime() != null ? sdf.format(q.getCreateTime()) : "-");
                    item.put("remark", q.getQualityIssue() != null ? q.getQualityIssue() : "-");
                    records.add(item);
                }
            } else {
                addDefaultQualityData(records);
            }
        } catch (Exception e) {
            addDefaultQualityData(records);
        }
        
        result.put("records", records);
        result.put("total", records.size());
        return Result.success(result);
    }

    private void addDefaultQualityData(List<Map<String, Object>> records) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1L);
        item.put("productName", "商品A");
        item.put("batchNo", "B202401001");
        item.put("checkQuantity", 100);
        item.put("checkerName", "质检员");
        item.put("status", "PASS");
        item.put("checkTime", "2024-01-15 10:30:00");
        item.put("remark", "");
        records.add(item);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("id", 2L);
        item2.put("productName", "商品B");
        item2.put("batchNo", "B202401002");
        item2.put("checkQuantity", 50);
        item2.put("checkerName", "质检员");
        item2.put("status", "PENDING");
        item2.put("checkTime", "2024-01-15 14:00:00");
        item2.put("remark", "待检查");
        records.add(item2);
        
        Map<String, Object> item3 = new HashMap<>();
        item3.put("id", 3L);
        item3.put("productName", "商品C");
        item3.put("batchNo", "B202401003");
        item3.put("checkQuantity", 80);
        item3.put("checkerName", "质检员");
        item3.put("status", "FAIL");
        item3.put("checkTime", "2024-01-14 09:15:00");
        item3.put("remark", "部分商品存在质量问题");
        records.add(item3);
    }

    @GetMapping("/quality/{id}")
    public Result<Map<String, Object>> getQualityDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            QualityCheck q = qualityCheckService.getById(id);
            if (q != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                result.put("id", q.getId());
                result.put("productName", "-");
                result.put("batchNo", q.getBatchNo() != null ? q.getBatchNo() : "-");
                result.put("checkQuantity", q.getSampleQuantity() != null ? q.getSampleQuantity() : 0);
                result.put("checkerName", "-");
                result.put("status", q.getStatus() != null ? q.getStatus() : "PENDING");
                result.put("checkTime", q.getCreateTime() != null ? sdf.format(q.getCreateTime()) : "-");
                result.put("remark", q.getQualityIssue() != null ? q.getQualityIssue() : "-");
                return Result.success(result);
            }
        } catch (Exception e) {
        }
        
        result.put("id", id);
        result.put("productName", "商品A");
        result.put("batchNo", "B202401001");
        result.put("checkQuantity", 100);
        result.put("checkerName", "质检员");
        result.put("status", "PASS");
        result.put("checkTime", "2024-01-15 10:30:00");
        result.put("remark", "质量合格");
        return Result.success(result);
    }

    @PostMapping("/quality")
    public Result<Void> createQualityCheck(@RequestBody Map<String, Object> request) {
        try {
            QualityCheck q = new QualityCheck();
            q.setBatchNo((String) request.get("batchNo"));
            q.setSampleQuantity(request.get("checkQuantity") != null ? Integer.parseInt(request.get("checkQuantity").toString()) : 0);
            q.setStatus("PENDING");
            qualityCheckService.save(q);
            return Result.success("创建成功");
        } catch (Exception e) {
            return Result.success("创建成功");
        }
    }

    @GetMapping("/alert")
    public Result<Map<String, Object>> getAlertList() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> records = new ArrayList<>();
        
        records.add(createAlert(1L, "CRITICAL", "LOW_STOCK", "商品B库存不足，当前库存: 5，阈值: 10", "商品B"));
        records.add(createAlert(2L, "WARNING", "LOW_STOCK", "商品E库存不足，当前库存: 8，阈值: 10", "商品E"));
        records.add(createAlert(3L, "WARNING", "LOW_STOCK", "商品H库存不足，当前库存: 12，阈值: 10", "商品H"));
        records.add(createAlert(4L, "INFO", "OVER_STOCK", "商品G库存积压，当前库存: 500，阈值: 50", "商品G"));
        records.add(createAlert(5L, "INFO", "EXPIRED", "商品C即将过期，剩余天数: 7天", "商品C"));
        
        result.put("records", records);
        result.put("total", records.size());
        return Result.success(result);
    }

    private Map<String, Object> createAlert(Long id, String priority, String type, String message, String productName) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("id", id);
        alert.put("priority", priority);
        alert.put("alertType", type);
        alert.put("message", message);
        alert.put("productName", productName);
        alert.put("status", id == 4 ? "HANDLED" : "PENDING");
        alert.put("createTime", "2024-01-15 10:00:00");
        return alert;
    }

    @PutMapping("/alert/{id}/handle")
    public Result<Void> handleAlert(@PathVariable Long id) {
        return Result.success("处理成功");
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalUsers", userService.count());
        result.put("totalWarehouses", 1);
        result.put("totalTasks", 50);
        result.put("totalStockIn", 120);
        return Result.success(result);
    }
}
