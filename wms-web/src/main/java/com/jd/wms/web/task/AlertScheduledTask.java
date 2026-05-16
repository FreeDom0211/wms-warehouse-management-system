package com.jd.wms.web.task;

import com.jd.wms.service.AlertService;
import com.jd.wms.service.PerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlertScheduledTask {

    @Autowired
    private AlertService alertService;

    @Autowired
    private PerformanceService performanceService;

    @Scheduled(cron = "0 0 8,12,18 * * ?")
    public void checkInventoryAlerts() {
        log.info("开始执行库存预警检查...");
        try {
            alertService.checkInventoryAlerts();
            log.info("库存预警检查完成");
        } catch (Exception e) {
            log.error("库存预警检查失败: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 30 9 * * ?")
    public void checkShelfLifeAlerts() {
        log.info("开始执行保质期预警检查...");
        try {
            alertService.checkShelfLifeAlerts();
            log.info("保质期预警检查完成");
        } catch (Exception e) {
            log.error("保质期预警检查失败: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void generateDailyPerformanceStats() {
        log.info("开始生成每日绩效统计...");
        try {
            performanceService.generateDailyStats();
            log.info("每日绩效统计生成完成");
        } catch (Exception e) {
            log.error("每日绩效统计生成失败: {}", e.getMessage());
        }
    }

}
