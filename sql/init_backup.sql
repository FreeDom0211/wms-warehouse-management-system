-- 数据备份日志表
USE jd_wms;

DROP TABLE IF EXISTS backup_log;
CREATE TABLE IF NOT EXISTS backup_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    backup_type VARCHAR(20) NOT NULL COMMENT '备份类型(AUTO:定时备份/MANUAL:手动备份)',
    file_name VARCHAR(100) NOT NULL COMMENT '备份文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '备份文件路径',
    file_size BIGINT COMMENT '文件大小(字节)',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '备份状态(SUCCESS:成功/FAILED:失败)',
    error_message VARCHAR(1000) COMMENT '错误信息',
    operator_id BIGINT COMMENT '操作用户ID(手动备份时记录)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_backup_type (backup_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备份日志表';