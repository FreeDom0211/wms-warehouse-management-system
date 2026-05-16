package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.Task;

import java.util.List;
import java.util.Map;

public interface TaskDispatchService extends IService<Task> {

    List<Map<String, Object>> getAllPendingTasks();

    List<Map<String, Object>> getAllTasks(Map<String, Object> params);

    Map<String, Object> getTaskOverview();

    boolean assignTask(Long taskId, Long operatorId, Integer priority);

    boolean reassignTask(Long taskId, Long newOperatorId);

    boolean updateTaskPriority(Long taskId, Integer priority);

    boolean batchAssignTasks(List<Long> taskIds, Long operatorId);

    List<Map<String, Object>> getTaskStatisticsByOperator(Long operatorId);

    List<Map<String, Object>> getTaskDistribution();

}