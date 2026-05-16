package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.Task;

import java.util.List;
import java.util.Map;

public interface TaskService extends IService<Task> {

    Task getByRelatedNo(String relatedNo);

    boolean addTask(Task task);

    boolean updateTask(Task task);

    boolean deleteTask(Long id);

    List<Task> getTasksByOperator(Long operatorId);

    List<Task> getTasksByStatus(Long operatorId, String status);

    boolean updateTaskStatus(Long id, String status);

    Map<String, Object> getTaskStatistics(Long operatorId);

}