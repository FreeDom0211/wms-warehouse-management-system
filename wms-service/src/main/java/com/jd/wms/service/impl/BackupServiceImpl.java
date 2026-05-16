package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.BackupLog;
import com.jd.wms.dao.mapper.BackupLogMapper;
import com.jd.wms.service.BackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class BackupServiceImpl extends ServiceImpl<BackupLogMapper, BackupLog> implements BackupService {

    @Value("${wms.backup.path:/backup}")
    private String backupPath;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    private String currentVerifyCode;

    @Override
    @Transactional
    public boolean autoBackup() {
        log.info("开始执行定时备份...");
        return executeBackup("AUTO", null);
    }

    @Override
    @Transactional
    public Map<String, Object> manualBackup(Long operatorId) {
        log.info("开始执行手动备份...");
        boolean success = executeBackup("MANUAL", operatorId);

        Map<String, Object> result = new HashMap<>();
        if (success) {
            String fileName = generateFileName();
            result.put("success", true);
            result.put("fileName", fileName);
            result.put("message", "备份成功");
        } else {
            result.put("success", false);
            result.put("message", "备份失败");
        }
        return result;
    }

    @Override
    public byte[] downloadBackup(String fileName) {
        try {
            Path filePath = Paths.get(backupPath, fileName);
            if (!Files.exists(filePath)) {
                throw new WmsException("备份文件不存在");
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("下载备份文件失败: {}", e.getMessage());
            throw new WmsException("下载失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean restoreData(String filePath, String verifyCode) {
        if (!verifyCode.equals(currentVerifyCode)) {
            throw new WmsException("验证码错误");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new WmsException("备份文件不存在");
        }

        log.info("开始执行数据恢复...");

        try {
            String databaseName = extractDatabaseName();
            ProcessBuilder builder = new ProcessBuilder();

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                builder.command("mysql", "-u" + dbUsername, "-p" + dbPassword, databaseName, "-e", "source " + filePath);
            } else {
                builder.command("sh", "-c", "mysql -u" + dbUsername + " -p" + dbPassword + " " + databaseName + " < " + filePath);
            }

            Process process = builder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("数据恢复成功");
                currentVerifyCode = null;
                return true;
            } else {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    error.append(line).append("\n");
                }
                log.error("数据恢复失败: {}", error.toString());
                throw new WmsException("恢复失败: " + error.toString());
            }
        } catch (Exception e) {
            log.error("数据恢复异常: {}", e.getMessage());
            throw new WmsException("恢复失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getBackupHistory(int days) {
        Date endDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = calendar.getTime();

        QueryWrapper<BackupLog> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startDate)
               .orderByDesc("create_time");

        List<BackupLog> backupLogs = list(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (BackupLog backupLog : backupLogs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", backupLog.getId());
            item.put("backupType", backupLog.getBackupType());
            item.put("backupTypeLabel", "AUTO".equals(backupLog.getBackupType()) ? "定时备份" : "手动备份");
            item.put("fileName", backupLog.getFileName());
            item.put("fileSize", formatFileSize(backupLog.getFileSize()));
            item.put("status", backupLog.getStatus());
            item.put("statusLabel", "SUCCESS".equals(backupLog.getStatus()) ? "成功" : "失败");
            item.put("errorMessage", backupLog.getErrorMessage());
            item.put("operatorId", backupLog.getOperatorId());
            item.put("createTime", backupLog.getCreateTime());
            result.add(item);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean deleteBackup(Long logId) {
        BackupLog backupLog = getById(logId);
        if (backupLog == null) {
            throw new WmsException("备份记录不存在");
        }

        try {
            Path filePath = Paths.get(backupLog.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除备份文件失败: {}", e.getMessage());
        }

        return removeById(logId);
    }

    @Override
    public String generateVerifyCode() {
        String code = String.format("%06d", new Random().nextInt(999999));
        currentVerifyCode = code;
        log.info("生成恢复验证码: {}", code);
        return code;
    }

    private boolean executeBackup(String backupType, Long operatorId) {
        String fileName = generateFileName();
        Path filePath = Paths.get(backupPath, fileName);

        try {
            Files.createDirectories(filePath.getParent());

            String databaseName = extractDatabaseName();
            ProcessBuilder builder = new ProcessBuilder();

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                builder.command("cmd", "/c", "mysqldump", "-u" + dbUsername, "-p" + dbPassword, databaseName, ">", filePath.toString());
            } else {
                builder.command("sh", "-c", "mysqldump -u" + dbUsername + " -p" + dbPassword + " " + databaseName + " > " + filePath.toString());
            }

            Process process = builder.start();
            int exitCode = process.waitFor();

            BackupLog backupLog = new BackupLog();
            backupLog.setBackupType(backupType);
            backupLog.setFileName(fileName);
            backupLog.setFilePath(filePath.toString());
            backupLog.setOperatorId(operatorId);
            backupLog.setCreateTime(new Date());

            if (exitCode == 0) {
                File file = filePath.toFile();
                backupLog.setFileSize(file.length());
                backupLog.setStatus("SUCCESS");
                backupLog.setErrorMessage(null);
                log.info("备份成功: {}", fileName);
            } else {
                backupLog.setStatus("FAILED");
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    error.append(line).append("\n");
                }
                backupLog.setErrorMessage(error.toString());
                log.error("备份失败: {}", error.toString());
            }

            save(backupLog);
            return "SUCCESS".equals(backupLog.getStatus());

        } catch (Exception e) {
            log.error("备份异常: {}", e.getMessage());
            BackupLog backupLog = new BackupLog();
            backupLog.setBackupType(backupType);
            backupLog.setFileName(fileName);
            backupLog.setFilePath(filePath.toString());
            backupLog.setOperatorId(operatorId);
            backupLog.setStatus("FAILED");
            backupLog.setErrorMessage(e.getMessage());
            backupLog.setCreateTime(new Date());
            save(backupLog);
            return false;
        }
    }

    private String generateFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return "jd_wms_backup_" + sdf.format(new Date()) + ".sql";
    }

    private String extractDatabaseName() {
        String url = dbUrl;
        if (url.contains("database=")) {
            return url.substring(url.indexOf("database=") + 9).split("[;&]")[0];
        } else if (url.contains("/")) {
            return url.substring(url.lastIndexOf("/") + 1).split("[?;]")[0];
        }
        return "jd_wms";
    }

    private String formatFileSize(Long size) {
        if (size == null) return "0 B";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        return String.format("%.2f MB", size / (1024.0 * 1024));
    }

}