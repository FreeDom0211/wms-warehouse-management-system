package com.jd.wms.web.controller;

import com.jd.wms.common.vo.Result;
import com.jd.wms.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/backup", "/api/backup"})
public class BackupApiController {

    @Autowired
    private BackupService backupService;

    @PostMapping
    public Result<Map<String, Object>> createBackup(@RequestBody Map<String, Object> request) {
        String backupType = (String) request.get("backupType");
        Long operatorId = request.get("operatorId") != null ? Long.parseLong(request.get("operatorId").toString()) : 1L;
        Map<String, Object> result = backupService.executeBackup(backupType, operatorId);
        return Result.success(result);
    }

    @GetMapping("/list")    public Result<List<Map<String, Object>>> getBackupList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> history = backupService.getBackupHistory(days);
        return Result.success(history);
    }

    @PostMapping("/manual")
    public Result<Map<String, Object>> manualBackup(@RequestBody Map<String, Object> request) {
        Long operatorId = request.get("operatorId") != null ? Long.parseLong(request.get("operatorId").toString()) : null;
        Map<String, Object> result = backupService.manualBackup(operatorId);
        return Result.success(result);
    }

    @GetMapping("/download/{fileName}")
    public Result<byte[]> downloadBackup(@PathVariable String fileName) {
        byte[] data = backupService.downloadBackup(fileName);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBackup(@PathVariable Long id) {
        backupService.deleteBackup(id);
        return Result.success("删除成功");
    }

}
