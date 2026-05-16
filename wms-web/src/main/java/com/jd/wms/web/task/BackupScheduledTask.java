package com.jd.wms.web.task;

import com.jd.wms.service.BackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BackupScheduledTask {

    @Autowired
    private BackupService backupService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void executeAutoBackup() {
        log.info("定时备份任务启动...");
        backupService.autoBackup();
        log.info("定时备份任务完成");
    }

}