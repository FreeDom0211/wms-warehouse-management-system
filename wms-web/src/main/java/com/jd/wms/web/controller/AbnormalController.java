package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.ExceptionReport;
import com.jd.wms.service.ExceptionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/abnormal")public class AbnormalController {

    @Autowired
    private ExceptionReportService exceptionReportService;

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

    @PostMapping("/report")    public Result<Map<String, Object>> reportException(@RequestBody Map<String, Object> request) {
        Long reporterId = Long.parseLong(request.get("reporterId").toString());
        String exceptionType = (String) request.get("exceptionType");
        Long productId = request.get("productId") != null ? Long.parseLong(request.get("productId").toString()) : null;
        Long locationId = request.get("locationId") != null ? Long.parseLong(request.get("locationId").toString()) : null;
        String description = (String) request.get("description");
        
        Map<String, Object> result = exceptionReportService.reportException(reporterId, exceptionType, productId, locationId, description);
        return Result.success(result);
    }

}
