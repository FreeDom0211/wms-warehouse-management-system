package com.jd.wms.web.controller;

import com.jd.wms.common.vo.Result;
import com.jd.wms.service.QualityCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/quality")
public class QualityCheckController {

    @Autowired
    private QualityCheckService qualityCheckService;

    @PostMapping("/inbound")
    public Result<Map<String, Object>> createInboundCheck(@RequestBody Map<String, Object> request) {
        Long productId = Long.parseLong(request.get("productId").toString());
        String batchNo = (String) request.get("batchNo");
        Long locationId = request.get("locationId") != null ? Long.parseLong(request.get("locationId").toString()) : null;
        Integer sampleQuantity = Integer.parseInt(request.get("sampleQuantity").toString());
        Integer qualifiedQuantity = Integer.parseInt(request.get("qualifiedQuantity").toString());
        Integer unqualifiedQuantity = Integer.parseInt(request.get("unqualifiedQuantity").toString());
        String qualityIssue = (String) request.get("qualityIssue");

        Map<String, Object> result = qualityCheckService.createInboundCheck(
                productId, batchNo, locationId, sampleQuantity,
                qualifiedQuantity, unqualifiedQuantity, qualityIssue);
        return Result.success(result);
    }

    @PostMapping("/periodic")
    public Result<Map<String, Object>> createPeriodicCheck(@RequestBody Map<String, Object> request) {
        Long productId = Long.parseLong(request.get("productId").toString());
        Long locationId = request.get("locationId") != null ? Long.parseLong(request.get("locationId").toString()) : null;

        Map<String, Object> result = qualityCheckService.createPeriodicCheck(productId, locationId);
        return Result.success(result);
    }

    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> getPendingChecks() {
        List<Map<String, Object>> checks = qualityCheckService.getPendingChecks();
        return Result.success(checks);
    }

    @GetMapping("/list")    public Result<Map<String, Object>> getCheckList(
            @RequestParam(required = false) String checkType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long productId) {
        try {
            Map<String, Object> params = new java.util.HashMap<>();
            params.put("checkType", checkType);
            params.put("status", status);
            params.put("productId", productId);
            List<Map<String, Object>> checks = qualityCheckService.getCheckList(params);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", 0);
            result.put("msg", "");
            result.put("count", checks.size());
            result.put("data", checks);
            
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

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getCheckDetails(@PathVariable Long id) {
        Map<String, Object> details = qualityCheckService.getCheckDetails(id);
        return Result.success(details);
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submitCheckResult(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        String checkResult = (String) request.get("checkResult");
        String qualityIssue = (String) request.get("qualityIssue");

        qualityCheckService.submitCheckResult(id, checkResult, qualityIssue);
        return Result.success("检查结果已提交");
    }

    @PostMapping("/{id}/audit")
    public Result<Void> auditCheck(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        String auditorResult = (String) request.get("auditorResult");
        String auditorRemark = (String) request.get("auditorRemark");
        Long auditorId = request.get("auditorId") != null ? Long.parseLong(request.get("auditorId").toString()) : null;

        qualityCheckService.auditCheck(id, auditorResult, auditorRemark);
        return Result.success("审核完成");
    }

    @PostMapping("/{id}/return")
    public Result<Void> initiateReturn(@PathVariable Long id) {
        qualityCheckService.initiateReturnProcess(id);
        return Result.success("退货流程已发起");
    }

    @PostMapping("/{id}/scrap")
    public Result<Void> initiateScrap(@PathVariable Long id) {
        qualityCheckService.initiateScrapProcess(id);
        return Result.success("报废流程已执");    }

}