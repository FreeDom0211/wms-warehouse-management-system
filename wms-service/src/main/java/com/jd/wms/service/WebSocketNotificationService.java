package com.jd.wms.service;

import com.jd.wms.dao.entity.Task;

import java.util.Map;

public interface WebSocketNotificationService {

    void notifyTaskUpdate(Task task);

    void notifyTaskAssigned(Long operatorId, Task task);

    void notifyAlert(Long managerId, Map<String, Object> alert);

    void notifyPerformanceUpdate(Long managerId, Map<String, Object> performance);

    void notifyAllManagers(String message, String type);

}
