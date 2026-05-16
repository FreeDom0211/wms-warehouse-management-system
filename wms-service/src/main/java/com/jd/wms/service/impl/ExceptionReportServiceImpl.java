package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.ExceptionReport;
import com.jd.wms.dao.mapper.ExceptionReportMapper;
import com.jd.wms.service.ExceptionReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExceptionReportServiceImpl extends ServiceImpl<ExceptionReportMapper, ExceptionReport> implements ExceptionReportService {

    @Override
    @Transactional
    public boolean addExceptionReport(ExceptionReport report) {
        report.setStatus("PENDING");
        report.setCreateTime(new Date());
        report.setUpdateTime(new Date());
        return save(report);
    }

    @Override
    @Transactional
    public boolean updateExceptionReport(ExceptionReport report) {
        report.setUpdateTime(new Date());
        return updateById(report);
    }

    @Override
    @Transactional
    public boolean deleteExceptionReport(Long id) {
        ExceptionReport report = getById(id);
        if (report == null) {
            throw new WmsException("异常报告不存在");
        }
        return removeById(id);
    }

    @Override
    @Transactional
    public Map<String, Object> reportException(Long reporterId, String exceptionType, Long productId,
                                               Long locationId, String description) {
        ExceptionReport report = new ExceptionReport();
        report.setReporterId(reporterId);
        report.setExceptionType(exceptionType);
        report.setProductId(productId);
        report.setLocationId(locationId);
        report.setDescription(description);
        report.setStatus("PENDING");
        report.setCreateTime(new Date());
        report.setUpdateTime(new Date());
        
        save(report);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("reportId", report.getId());
        result.put("message", "异常上报成功");
        
        return result;
    }

    @Override
    public List<ExceptionReport> getReporterReports(Long reporterId) {
        QueryWrapper<ExceptionReport> wrapper = new QueryWrapper<>();
        wrapper.eq("reporter_id", reporterId).orderByDesc("create_time");
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean handleException(Long reportId, Long handlerId, String result) {
        ExceptionReport report = getById(reportId);
        if (report == null) {
            throw new WmsException("异常报告不存在");
        }
        
        report.setHandlerId(handlerId);
        report.setResult(result);
        report.setStatus("COMPLETED");
        report.setUpdateTime(new Date());
        
        return updateById(report);
    }

}