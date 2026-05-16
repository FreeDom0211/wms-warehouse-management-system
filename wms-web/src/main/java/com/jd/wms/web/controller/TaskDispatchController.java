package com.jd.wms.web.controller;

import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.User;
import com.jd.wms.service.TaskDispatchService;
import com.jd.wms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/task")public class TaskDispatchController {

    @Autowired
    private TaskDispatchService taskDispatchService;

    @Autowired
    private UserService userService;

    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> getPendingTasks() {
        List<Map<String, Object>> tasks = taskDispatchService.getAllPendingTasks();
        return Result.success(tasks);
    }

    @GetMapping("/list")    public Result<List<Map<String, Object>>> getTaskList(
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Integer priority) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("operatorId", operatorId);
        params.put("status", status);
        params.put("taskType", taskType);
        params.put("priority", priority);
        List<Map<String, Object>> tasks = taskDispatchService.getAllTasks(params);
        return Result.success(tasks);
    }

    @GetMapping("/overview")    public Result<Map<String, Object>> getTaskOverview() {
        Map<String, Object> overview = taskDispatchService.getTaskOverview();
        return Result.success(overview);
    }

    @GetMapping("/operators")
    public Result<List<User>> getOperators() {
        List<User> operators = userService.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("role_code", "OPERATOR"));
        return Result.success(operators);
    }

    @PostMapping("/assign")
    public Result<Void> assignTask(@RequestBody Map<String, Object> request) {
        Long taskId = Long.parseLong(request.get("taskId").toString());
        Long operatorId = Long.parseLong(request.get("operatorId").toString());
        Integer priority = request.get("priority") != null ? Integer.parseInt(request.get("priority").toString()) : null;

        taskDispatchService.assignTask(taskId, operatorId, priority);
        return Result.success("任务分配成功");
    }

    @PostMapping("/reassign")
    public Result<Void> reassignTask(@RequestBody Map<String, Object> request) {
        Long taskId = Long.parseLong(request.get("taskId").toString());
        Long newOperatorId = Long.parseLong(request.get("newOperatorId").toString());

        taskDispatchService.reassignTask(taskId, newOperatorId);
        return Result.success("任务重新分配成功");
    }

    @PostMapping("/priority")
    public Result<Void> updatePriority(@RequestBody Map<String, Object> request) {
        Long taskId = Long.parseLong(request.get("taskId").toString());
        Integer priority = Integer.parseInt(request.get("priority").toString());

        taskDispatchService.updateTaskPriority(taskId, priority);
        return Result.success("优先级更新成");    }

    @PostMapping("/batch-assign")
    public Result<Void> batchAssignTasks(@RequestBody Map<String, Object> request) {
        List<Long> taskIds = (List<Long>) request.get("taskIds");
        Long operatorId = Long.parseLong(request.get("operatorId").toString());

        taskDispatchService.batchAssignTasks(taskIds, operatorId);
        return Result.success("批量分配成功");
    }

    @GetMapping("/stats/by-operator")
    public Result<List<Map<String, Object>>> getStatsByOperator(
            @RequestParam(required = false) Long operatorId) {
        List<Map<String, Object>> stats = taskDispatchService.getTaskStatisticsByOperator(operatorId);
        return Result.success(stats);
    }

    @GetMapping("/distribution")
    public Result<List<Map<String, Object>>> getDistribution() {
        List<Map<String, Object>> distribution = taskDispatchService.getTaskDistribution();
        return Result.success(distribution);
    }

}