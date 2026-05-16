-- 主管功能模块数据库扩展

USE jd_wms;

-- 1. 扩展商品表，添加保质期和类别字段
ALTER TABLE product ADD COLUMN IF NOT EXISTS shelf_life_days INT COMMENT '保质期天数' AFTER unit;
ALTER TABLE product ADD COLUMN IF NOT EXISTS category VARCHAR(50) COMMENT '商品类别' AFTER shelf_life_days;
ALTER TABLE product ADD COLUMN IF NOT EXISTS min_stock INT DEFAULT 0 COMMENT '最低库存阈值' AFTER category;
ALTER TABLE product ADD COLUMN IF NOT EXISTS max_stock INT DEFAULT 0 COMMENT '最高库存阈值' AFTER min_stock;

-- 2. 创建库存预警表
DROP TABLE IF EXISTS inventory_alert;
CREATE TABLE IF NOT EXISTS inventory_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预警ID',
    alert_type VARCHAR(20) NOT NULL COMMENT '预警类型(LOW_STOCK:低库存/HIGH_STOCK:高库存/EXPIRED:过期/SHELF_LIFE:临期)',
    product_id BIGINT COMMENT '关联商品ID',
    warehouse_id BIGINT COMMENT '关联仓库ID',
    location_id BIGINT COMMENT '关联货位ID',
    current_quantity INT COMMENT '当前库存数量',
    threshold_value INT COMMENT '阈值',
    alert_level VARCHAR(10) DEFAULT 'WARNING' COMMENT '预警级别(WARNING:警告/ERROR:错误)',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态(PENDING:待处理/HANDLED:已处理/DISMISSED:已忽略)',
    description VARCHAR(500) COMMENT '预警描述',
    handler_id BIGINT COMMENT '处理人ID',
    handle_result VARCHAR(500) COMMENT '处理结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_alert_type (alert_type),
    INDEX idx_product_id (product_id),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存预警表';

-- 3. 创建质量检查表
DROP TABLE IF EXISTS quality_check;
CREATE TABLE IF NOT EXISTS quality_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '质量检查ID',
    check_no VARCHAR(50) NOT NULL UNIQUE COMMENT '检查单号',
    check_type VARCHAR(20) NOT NULL COMMENT '检查类型(INBOUND:入库抽检/PERIODIC:定期检查)',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    batch_no VARCHAR(50) COMMENT '批次号',
    location_id BIGINT COMMENT '货位ID',
    sample_quantity INT COMMENT '抽检数量',
    qualified_quantity INT COMMENT '合格数量',
    unqualified_quantity INT COMMENT '不合格数量',
    check_result VARCHAR(10) COMMENT '检查结果(PASS:合格/FAIL:不合格)',
    quality_issue VARCHAR(200) COMMENT '质量问题描述',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态(PENDING:待检查/COMPLETED:已完成/AUDITED:已审核)',
    auditor_id BIGINT COMMENT '审核人ID',
    auditor_result VARCHAR(10) COMMENT '审核结果(PASS:通过/FAIL:不通过)',
    auditor_remark VARCHAR(500) COMMENT '审核备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_check_no (check_no),
    INDEX idx_check_type (check_type),
    INDEX idx_product_id (product_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='质量检查表';

-- 4. 创建绩效统计表
DROP TABLE IF EXISTS performance_stats;
CREATE TABLE IF NOT EXISTS performance_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '绩效ID',
    operator_id BIGINT NOT NULL COMMENT '操作员ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stock_in_count INT DEFAULT 0 COMMENT '入库次数',
    stock_out_count INT DEFAULT 0 COMMENT '出库次数',
    check_count INT DEFAULT 0 COMMENT '盘点次数',
    total_handle_quantity INT DEFAULT 0 COMMENT '总处理数量',
    error_count INT DEFAULT 0 COMMENT '差异次数',
    accuracy_rate DECIMAL(5,2) COMMENT '准确率',
    avg_handle_time DECIMAL(10,2) COMMENT '平均处理时间(分钟)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_operator_date (operator_id, stat_date),
    INDEX idx_operator_id (operator_id),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='绩效统计表';

-- 5. 任务表添加更多字段
ALTER TABLE task ADD COLUMN IF NOT EXISTS assigned_by BIGINT COMMENT '分配人ID' AFTER operator_id;
ALTER TABLE task ADD COLUMN IF NOT EXISTS start_time DATETIME COMMENT '开始时间' AFTER remark;
ALTER TABLE task ADD COLUMN IF NOT EXISTS end_time DATETIME COMMENT '完成时间' AFTER start_time;

-- 6. 更新商品表测试数据
UPDATE product SET shelf_life_days = 365, category = '电子产品', min_stock = 10, max_stock = 1000 WHERE product_code = 'P001';
UPDATE product SET shelf_life_days = 730, category = '电子产品', min_stock = 5, max_stock = 500 WHERE product_code = 'P002';
UPDATE product SET shelf_life_days = 365, category = '电子产品', min_stock = 20, max_stock = 2000 WHERE product_code = 'P003';
UPDATE product SET shelf_life_days = 365, category = '电子产品', min_stock = 15, max_stock = 1500 WHERE product_code = 'P004';
UPDATE product SET shelf_life_days = 180, category = '日用品', min_stock = 50, max_stock = 5000 WHERE product_code = 'P005';
UPDATE product SET shelf_life_days = 180, category = '食品', min_stock = 100, max_stock = 10000 WHERE product_code = 'P006';
UPDATE product SET shelf_life_days = 365, category = '食品', min_stock = 200, max_stock = 20000 WHERE product_code = 'P007';
UPDATE product SET shelf_life_days = 90, category = '食品', min_stock = 50, max_stock = 5000 WHERE product_code = 'P008';