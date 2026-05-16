package com.jd.wms.common.event;

import org.springframework.context.ApplicationEvent;

import java.util.HashMap;
import java.util.Map;

public class TaskEvent extends ApplicationEvent {

    private Long operatorId;
    private Long managerId;
    private String eventType;
    private Map<String, Object> data;

    public TaskEvent(Object source) {
        super(source);
        this.data = new HashMap<>();
    }

    public static TaskEvent of(String eventType) {
        TaskEvent event = new TaskEvent(new Object());
        event.setEventType(eventType);
        return event;
    }

    public TaskEvent operatorId(Long operatorId) {
        this.operatorId = operatorId;
        return this;
    }

    public TaskEvent managerId(Long managerId) {
        this.managerId = managerId;
        return this;
    }

    public TaskEvent data(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public TaskEvent addData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}