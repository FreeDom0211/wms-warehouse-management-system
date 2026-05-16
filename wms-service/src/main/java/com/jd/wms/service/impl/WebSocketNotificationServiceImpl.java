package com.jd.wms.service.impl;

import com.jd.wms.common.event.TaskEvent;
import com.jd.wms.dao.entity.Task;
import com.jd.wms.service.WebSocketNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    private final ApplicationEventPublisher eventPublisher;

    public WebSocketNotificationServiceImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void notifyTaskUpdate(Task task) {
        Map<String, Object> taskInfo = convertTaskToMap(task);

        if (task.getOperatorId() != null) {
            TaskEvent event = TaskEvent.of("TASK_UPDATE")
                    .operatorId(task.getOperatorId())
                    .data(taskInfo);
            eventPublisher.publishEvent(event);
        }

        TaskEvent event = TaskEvent.of("TASK_UPDATE_ALL_MANAGERS")
                .data(taskInfo);
        eventPublisher.publishEvent(event);
    }

    @Override
    public void notifyTaskAssigned(Long operatorId, Task task) {
        Map<String, Object> taskInfo = convertTaskToMap(task);
        taskInfo.put("message", "您有新的任务: " + task.getRelatedNo());

        TaskEvent event = TaskEvent.of("TASK_ASSIGNED")
                .operatorId(operatorId)
                .data(taskInfo);
        eventPublisher.publishEvent(event);
    }

    @Override
    public void notifyAlert(Long managerId, Map<String, Object> alert) {
        TaskEvent event = TaskEvent.of("ALERT")
                .managerId(managerId)
                .data(alert);
        eventPublisher.publishEvent(event);
    }

    @Override
    public void notifyPerformanceUpdate(Long managerId, Map<String, Object> performance) {
        TaskEvent event = TaskEvent.of("PERFORMANCE_UPDATE")
                .managerId(managerId)
                .data(performance);
        eventPublisher.publishEvent(event);
    }

    @Override
    public void notifyAllManagers(String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("type", type);

        TaskEvent event = TaskEvent.of("NOTIFY_ALL_MANAGERS")
                .data(notification);
        eventPublisher.publishEvent(event);
    }

    private Map<String, Object> convertTaskToMap(Task task) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", task.getId());
        map.put("taskType", task.getTaskType());
        map.put("relatedNo", task.getRelatedNo());
        map.put("operatorId", task.getOperatorId());
        map.put("status", task.getStatus());
        map.put("priority", task.getPriority());
        map.put("updateTime", task.getUpdateTime());
        return map;
    }

}