package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.OperationLog;

import java.util.List;
import java.util.Map;

public interface OperationLogService extends IService<OperationLog> {

    List<Map<String, Object>> getLogList(Map<String, Object> params);

    Map<String, Object> getLogStatistics();

    boolean deleteLog(Long id);

    boolean clearOldLogs(int days);

}
