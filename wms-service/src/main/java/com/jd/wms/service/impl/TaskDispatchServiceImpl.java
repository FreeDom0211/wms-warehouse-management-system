package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.*;
import com.jd.wms.dao.mapper.*;
import com.jd.wms.service.TaskDispatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskDispatchServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskDispatchService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Override
    public List<Map<String, Object>> getAllPendingTasks() {
        QueryWrapper<Task> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "UNASSIGNED");
        wrapper.orderByDesc("priority");
        List<Task> tasks = taskMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Task task : tasks) {
            result.add(convertTaskToMap(task));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllTasks(Map<String, Object> params) {
        QueryWrapper<Task> wrapper = new QueryWrapper<>();

        if (params != null) {
            if (params.get("status") != null) {
                wrapper.eq("status", params.get("status"));
            }
            if (params.get("operatorId") != null) {
                wrapper.eq("operator_id", params.get("operatorId"));
            }
        }

        List<Task> tasks = taskMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Task task : tasks) {
            result.add(convertTaskToMap(task));
        }
        return result;
    }

    @Override
    public Map<String, Object> getTaskOverview() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Task> allWrapper = new QueryWrapper<>();
        long totalTasks = taskMapper.selectCount(allWrapper);

        QueryWrapper<Task> unassignedWrapper = new QueryWrapper<>();
        unassignedWrapper.eq("status", "UNASSIGNED");
        long unassignedTasks = taskMapper.selectCount(unassignedWrapper);

        QueryWrapper<Task> assignedWrapper = new QueryWrapper<>();
        assignedWrapper.eq("status", "ASSIGNED");
        long assignedTasks = taskMapper.selectCount(assignedWrapper);

        QueryWrapper<Task> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("status", "COMPLETED");
        long completedTasks = taskMapper.selectCount(completedWrapper);

        result.put("totalTasks", totalTasks);
        result.put("unassignedTasks", unassignedTasks);
        result.put("assignedTasks", assignedTasks);
        result.put("completedTasks", completedTasks);

        return result;
    }

    @Override
    public boolean assignTask(Long taskId, Long operatorId, Integer priority) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new WmsException("任务不存在");
        }

        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new WmsException("操作员不存在");
        }
        if (!"OPERATOR".equals(operator.getRoleCode())) {
            throw new WmsException("指定用户不是操作员");
        }

        task.setOperatorId(operatorId);
        task.setStatus("ASSIGNED");
        task.setPriority(priority);
        task.setUpdateTime(new Date());

        return taskMapper.updateById(task) > 0;
    }

    @Override
    public boolean reassignTask(Long taskId, Long newOperatorId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new WmsException("任务不存在");
        }

        User operator = userMapper.selectById(newOperatorId);
        if (operator == null) {
            throw new WmsException("操作员不存在");
        }

        task.setOperatorId(newOperatorId);
        task.setUpdateTime(new Date());

        return taskMapper.updateById(task) > 0;
    }

    @Override
    public boolean updateTaskPriority(Long taskId, Integer priority) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new WmsException("任务不存在");
        }

        task.setPriority(priority);
        task.setUpdateTime(new Date());

        return taskMapper.updateById(task) > 0;
    }

    @Override
    public boolean batchAssignTasks(List<Long> taskIds, Long operatorId) {
        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new WmsException("操作员不存在");
        }

        for (Long taskId : taskIds) {
            Task task = taskMapper.selectById(taskId);
            if (task != null && "UNASSIGNED".equals(task.getStatus())) {
                task.setOperatorId(operatorId);
                task.setStatus("ASSIGNED");
                task.setUpdateTime(new Date());
                taskMapper.updateById(task);
            }
        }
        return true;
    }

    @Override
    public List<Map<String, Object>> getTaskStatisticsByOperator(Long operatorId) {
        List<Map<String, Object>> result = new ArrayList<>();

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role_code", "OPERATOR");
        if (operatorId != null) {
            wrapper.eq("id", operatorId);
        }
        List<User> operators = userMapper.selectList(wrapper);

        for (User operator : operators) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("operatorId", operator.getId());
            stats.put("operatorName", operator.getName());

            QueryWrapper<Task> taskWrapper = new QueryWrapper<>();
            taskWrapper.eq("operator_id", operator.getId());
            taskWrapper.eq("status", "IN_PROGRESS");
            stats.put("inProgressCount", taskMapper.selectCount(taskWrapper));

            taskWrapper = new QueryWrapper<>();
            taskWrapper.eq("operator_id", operator.getId());
            taskWrapper.eq("status", "COMPLETED");
            stats.put("completedCount", taskMapper.selectCount(taskWrapper));

            taskWrapper = new QueryWrapper<>();
            taskWrapper.eq("operator_id", operator.getId());
            stats.put("totalCount", taskMapper.selectCount(taskWrapper));

            result.add(stats);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getTaskDistribution() {
        QueryWrapper<Location> locWrapper = new QueryWrapper<>();
        locWrapper.select("warehouse_id", "zone");
        List<Location> locations = locationMapper.selectList(locWrapper);

        Map<String, Map<String, Object>> distribution = new LinkedHashMap<>();
        for (Location loc : locations) {
            String key = loc.getWarehouseId() + "_" + loc.getZone();
            if (!distribution.containsKey(key)) {
                Map<String, Object> item = new HashMap<>();
                item.put("warehouseId", loc.getWarehouseId());
                item.put("zone", loc.getZone());
                item.put("taskCount", 0);
                distribution.put(key, item);
            }
        }

        QueryWrapper<Task> taskWrapper = new QueryWrapper<>();
        List<Task> tasks = taskMapper.selectList(taskWrapper);

        for (Task task : tasks) {
            if (task.getOperatorId() != null) {
                Location loc = locationMapper.selectById(task.getOperatorId());
                if (loc != null) {
                    String key = loc.getWarehouseId() + "_" + loc.getZone();
                    if (distribution.containsKey(key)) {
                        Map<String, Object> item = distribution.get(key);
                        item.put("taskCount", (int) item.get("taskCount") + 1);
                    }
                }
            }
        }

        return new ArrayList<>(distribution.values());
    }

    private Map<String, Object> convertTaskToMap(Task task) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", task.getId());
        map.put("taskType", task.getTaskType());
        map.put("relatedNo", task.getRelatedNo());
        map.put("operatorId", task.getOperatorId());
        map.put("status", task.getStatus());
        map.put("priority", task.getPriority());
        map.put("remark", task.getRemark());
        map.put("createTime", task.getCreateTime());
        map.put("updateTime", task.getUpdateTime());

        if (task.getOperatorId() != null) {
            User operator = userMapper.selectById(task.getOperatorId());
            if (operator != null) {
                map.put("operatorName", operator.getName());
            }
        }

        return map;
    }
}