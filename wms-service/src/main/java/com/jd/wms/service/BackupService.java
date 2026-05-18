package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.BackupLog;

import java.util.List;
import java.util.Map;

public interface BackupService extends IService<BackupLog> {

    boolean autoBackup();

    Map<String, Object> manualBackup(Long operatorId);

    Map<String, Object> executeBackup(String backupType, Long operatorId);

    byte[] downloadBackup(String fileName);

    boolean restoreData(String filePath, String verifyCode);

    List<Map<String, Object>> getBackupHistory(int days);

    boolean deleteBackup(Long logId);

    String generateVerifyCode();

}