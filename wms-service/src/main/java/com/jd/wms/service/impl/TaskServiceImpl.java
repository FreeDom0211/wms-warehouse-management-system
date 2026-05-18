package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.Task;
import com.jd.wms.dao.mapper.TaskMapper;
import com.jd.wms.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    @Override
    public Task getByRelatedNo(String relatedNo) {
        return baseMapper.selectByRelatedNo(relatedNo);
    }

    @Override
    @Transactional
    public boolean addTask(Task task) {
        task.setStatus("PENDING");
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        return save(task);
    }

    @Override
    @Transactional
    public boolean updateTask(Task task) {
        task.setUpdateTime(new Date());
        return updateById(task);
    }

    @Override
    @Transactional
    public boolean deleteTask(Long id) {
        Task task = getById(id);
        if (task == null) {
            throw new WmsException("任务不存在");
        }
        return removeById(id);
    }

    @Override
    public List<Task> getTasksByOperator(Long operatorId) {
        QueryWrapper<Task> wrapper = new QueryWrapper<>();
        wrapper.eq("operator_id", operatorId).orderByDesc("create_time");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Task> getTasksByStatus(Long operatorId, String status) {
        QueryWrapper<Task> wrapper = new QueryWrapper<>();
        wrapper.eq("operator_id", operatorId).eq("status", status).orderByDesc("create_time");
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean updateTaskStatus(Long id, String status) {
        Task task = getById(id);
        if (task == null) {
            throw new WmsException("任务不存在");
        }
        task.setStatus(status);
        task.setUpdateTime(new Date());
        return updateById(task);
    }

    @Override
    @Transactional
    public boolean assignTask(Long id, Long operatorId) {
        Task task = getById(id);
        if (task == null) {
            throw new WmsException("任务不存在");
        }
        task.setOperatorId(operatorId);
        task.setStatus("IN_PROGRESS");
        task.setUpdateTime(new Date());
        return updateById(task);
    }

    @Override
    public Map<String, Object> getTaskStatistics(Long operatorId) {
        Map<String, Object> stats = new HashMap<>();
        
        QueryWrapper<Task> wrapper = new QueryWrapper<>();
        wrapper.eq("operator_id", operatorId);
        
        long total = baseMapper.selectCount(wrapper);
        
        wrapper.eq("status", "PENDING");
        long pending = baseMapper.selectCount(wrapper);
        
        wrapper.clear();
        wrapper.eq("operator_id", operatorId).eq("status", "PROCESSING");
        long processing = baseMapper.selectCount(wrapper);
        
        wrapper.clear();
        wrapper.eq("operator_id", operatorId).eq("status", "COMPLETED");
        long completed = baseMapper.selectCount(wrapper);
        
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("processing", processing);
        stats.put("completed", completed);
        
        return stats;
    }

}