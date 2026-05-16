package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.ExceptionReport;
import com.jd.wms.service.ExceptionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exception")public class ExceptionReportController {

    @Autowired
    private ExceptionReportService exceptionReportService;

    @GetMapping
    public Result<IPage<ExceptionReport>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long reporterId,
            @RequestParam(required = false) String status) {
        QueryWrapper<ExceptionReport> wrapper = new QueryWrapper<>();
        if (reporterId != null) {
            wrapper.eq("reporter_id", reporterId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        IPage<ExceptionReport> page = exceptionReportService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<ExceptionReport> getById(@PathVariable Long id) {
        ExceptionReport report = exceptionReportService.getById(id);
        return Result.success(report);
    }

    @PostMapping
    public Result<ExceptionReport> add(@RequestBody ExceptionReport report) {
        exceptionReportService.addExceptionReport(report);
        return Result.success("创建成功", report);    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ExceptionReport report) {
        report.setId(id);
        exceptionReportService.updateExceptionReport(report);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        exceptionReportService.deleteExceptionReport(id);
        return Result.success("删除成功");
    }

    @PostMapping("/report")    public Result<Map<String, Object>> reportException(@RequestBody Map<String, Object> request) {
        Long reporterId = Long.parseLong(request.get("reporterId").toString());
        String exceptionType = (String) request.get("exceptionType");
        Long productId = request.get("productId") != null ? Long.parseLong(request.get("productId").toString()) : null;
        Long locationId = request.get("locationId") != null ? Long.parseLong(request.get("locationId").toString()) : null;
        String description = (String) request.get("description");
        
        Map<String, Object> result = exceptionReportService.reportException(reporterId, exceptionType, productId, locationId, description);
        return Result.success(result);
    }

    @GetMapping("/reporter/{reporterId}")
    public Result<List<ExceptionReport>> getReporterReports(@PathVariable Long reporterId) {
        List<ExceptionReport> reports = exceptionReportService.getReporterReports(reporterId);
        return Result.success(reports);
    }

    @GetMapping("/operator/{operatorId}")
    public Result<IPage<ExceptionReport>> getOperatorReports(
            @PathVariable Long operatorId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<ExceptionReport> wrapper = new QueryWrapper<>();
        wrapper.eq("reporter_id", operatorId);
        IPage<ExceptionReport> page = exceptionReportService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @PutMapping("/{id}/handle")
    public Result<Void> handleException(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Long handlerId = Long.parseLong(request.get("handlerId").toString());
        String result = (String) request.get("result");
        
        exceptionReportService.handleException(id, handlerId, result);
        return Result.success("处理完成");
    }

}