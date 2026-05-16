package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.ExceptionReport;

import java.util.List;
import java.util.Map;

public interface ExceptionReportService extends IService<ExceptionReport> {

    boolean addExceptionReport(ExceptionReport report);

    boolean updateExceptionReport(ExceptionReport report);

    boolean deleteExceptionReport(Long id);

    Map<String, Object> reportException(Long reporterId, String exceptionType, Long productId, 
                                        Long locationId, String description);

    List<ExceptionReport> getReporterReports(Long reporterId);

    boolean handleException(Long reportId, Long handlerId, String result);

}