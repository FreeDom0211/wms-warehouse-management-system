-- 操作日志表
USE jd_wms;

DROP TABLE IF EXISTS operation_log;
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    module VARCHAR(50) COMMENT '模块名称',
    operation VARCHAR(50) COMMENT '操作类型',
    method_name VARCHAR(200) COMMENT '方法名',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人姓名',
    operator_ip VARCHAR(50) COMMENT '操作人IP',
    execution_time BIGINT COMMENT '执行时间(毫秒)',
    status VARCHAR(20) COMMENT '状态(SUCCESS/FAILED)',
    error_message VARCHAR(1000) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_module (module),
    INDEX idx_operator_id (operator_id),
    INDEX idx_create_time (create_time),
    INDEX idx_operation (operation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';