package com.jd.wms.web.controller;

import com.jd.wms.common.vo.Result;
import com.jd.wms.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/backup")public class BackupController {

    @Autowired
    private BackupService backupService;

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
    public ResponseEntity<byte[]> downloadBackup(@PathVariable String fileName) {
        byte[] data = backupService.downloadBackup(fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("/generate-code")
    public Result<String> generateVerifyCode() {
        String code = backupService.generateVerifyCode();
        return Result.success(code);
    }

    @PostMapping("/restore")
    public Result<Void> restoreData(@RequestParam("file") MultipartFile file,
                                    @RequestParam("verifyCode") String verifyCode) {
        try {
            Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
            Files.write(tempPath, file.getBytes());
            backupService.restoreData(tempPath.toString(), verifyCode);
            Files.deleteIfExists(tempPath);
            return Result.success("恢复成功");
        } catch (IOException e) {
            return Result.error("文件处理失败: " + e.getMessage());
        }
    }

    @GetMapping("/history")    public Result<List<Map<String, Object>>> getBackupHistory(
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> history = backupService.getBackupHistory(days);
        return Result.success(history);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBackup(@PathVariable Long id) {
        backupService.deleteBackup(id);
        return Result.success("删除成功");
    }

}