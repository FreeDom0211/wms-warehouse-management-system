package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.Task;
import com.jd.wms.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public Result<IPage<Task>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status) {
        QueryWrapper<Task> wrapper = new QueryWrapper<>();
        if (operatorId != null) {
            wrapper.eq("operator_id", operatorId);
        }
        if (taskType != null && !taskType.isEmpty()) {
            wrapper.eq("task_type", taskType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        IPage<Task> page = taskService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        Task task = taskService.getById(id);
        return Result.success(task);
    }

    @PostMapping
    public Result<Task> add(@RequestBody Task task) {
        taskService.addTask(task);
        return Result.success("任务创建成功", task);    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        taskService.updateTask(task);
        return Result.success("任务更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return Result.success("任务删除成功");
    }

    @GetMapping("/operator/{operatorId}")
    public Result<IPage<Task>> getTasksByOperator(
            @PathVariable Long operatorId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<Task> wrapper = new QueryWrapper<>();
        wrapper.eq("operator_id", operatorId);
        IPage<Task> page = taskService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @GetMapping("/operator/{operatorId}/status/{status}")
    public Result<List<Task>> getTasksByStatus(@PathVariable Long operatorId, @PathVariable String status) {
        List<Task> tasks = taskService.getTasksByStatus(operatorId, status);
        return Result.success(tasks);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        taskService.updateTaskStatus(id, status);
        return Result.success("任务状态更新成");    }

    @GetMapping("/operator/{operatorId}/statistics")
    public Result<Map<String, Object>> getStatistics(@PathVariable Long operatorId) {
        Map<String, Object> stats = taskService.getTaskStatistics(operatorId);
        return Result.success(stats);
    }

    @GetMapping("/operator/statistics")
    public Result<Map<String, Object>> getOperatorStatistics(@RequestParam Long operatorId) {
        Map<String, Object> stats = taskService.getTaskStatistics(operatorId);
        return Result.success(stats);
    }

}