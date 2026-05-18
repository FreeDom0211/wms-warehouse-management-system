CREATE DATABASE IF NOT EXISTS jd_wms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE jd_wms;

DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS role_permission;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS inventory_check;
DROP TABLE IF EXISTS exception_report;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS stock_out;
DROP TABLE IF EXISTS stock_in;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS warehouse;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS backup_log;

CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    work_no VARCHAR(50) NOT NULL UNIQUE COMMENT '工号',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    role_code VARCHAR(20) NOT NULL COMMENT '角色编码(OPERATOR/ADMINISTRATOR/MANAGER)',
    status INT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_work_no (work_no),
    INDEX idx_phone (phone),
    INDEX idx_role_code (role_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    product_code VARCHAR(50) NOT NULL UNIQUE COMMENT '商品编号',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    spec VARCHAR(200) COMMENT '规格',
    unit VARCHAR(20) NOT NULL COMMENT '单位',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_product_code (product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

CREATE TABLE IF NOT EXISTS warehouse (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仓库ID',
    warehouse_code VARCHAR(50) COMMENT '仓库编码',
    warehouse_name VARCHAR(100) COMMENT '仓库名称',
    name VARCHAR(100) NOT NULL COMMENT '仓库名称',
    address VARCHAR(500) COMMENT '地址',
    zone_info VARCHAR(500) COMMENT '库区信息',
    capacity INT COMMENT '容量',
    used_capacity INT COMMENT '已用容量',
    status INT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

CREATE TABLE IF NOT EXISTS location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '货位ID',
    location_code VARCHAR(50) NOT NULL UNIQUE COMMENT '货位编码',
    warehouse_id BIGINT NOT NULL COMMENT '所属仓库ID',
    zone VARCHAR(50) COMMENT '库区',
    status VARCHAR(20) DEFAULT 'EMPTY' COMMENT '状态(EMPTY:空闲/OCCUPIED:占用/EXCEPTION:异常)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_location_code (location_code),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_status (status),
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='货位表';

CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    location_id BIGINT NOT NULL COMMENT '货位ID',
    batch_no VARCHAR(50) NOT NULL COMMENT '批次号',
    quantity INT NOT NULL DEFAULT 0 COMMENT '数量',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_product_id (product_id),
    INDEX idx_location_id (location_id),
    INDEX idx_batch_no (batch_no),
    UNIQUE KEY uk_product_location_batch (product_id, location_id, batch_no),
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

CREATE TABLE IF NOT EXISTS stock_in (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '入库单ID',
    stock_in_no VARCHAR(50) NOT NULL UNIQUE COMMENT '入库单号',
    operator_id BIGINT NOT NULL COMMENT '操作员ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    batch_no VARCHAR(50) NOT NULL COMMENT '批次号',
    location_id BIGINT NOT NULL COMMENT '货位ID',
    quantity INT NOT NULL COMMENT '数量',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态(PENDING:待入库/COMPLETED:已完成/CANCELLED:已取消)',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_stock_in_no (stock_in_no),
    INDEX idx_operator_id (operator_id),
    INDEX idx_product_id (product_id),
    INDEX idx_status (status),
    FOREIGN KEY (operator_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库单表';

CREATE TABLE IF NOT EXISTS stock_out (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '出库单ID',
    stock_out_no VARCHAR(50) NOT NULL UNIQUE COMMENT '出库单号',
    operator_id BIGINT NOT NULL COMMENT '操作员ID',
    order_no VARCHAR(50) COMMENT '订单号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    batch_no VARCHAR(50) NOT NULL COMMENT '批次号',
    location_id BIGINT NOT NULL COMMENT '货位ID',
    quantity INT NOT NULL COMMENT '数量',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态(PENDING:待出库/COMPLETED:已完成/CANCELLED:已取消)',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_stock_out_no (stock_out_no),
    INDEX idx_operator_id (operator_id),
    INDEX idx_order_no (order_no),
    INDEX idx_product_id (product_id),
    INDEX idx_status (status),
    FOREIGN KEY (operator_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库单表';

CREATE TABLE IF NOT EXISTS task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_type VARCHAR(20) NOT NULL COMMENT '任务类型(STOCK_IN:入库/STOCK_OUT:出库/INVENTORY_CHECK:盘点)',
    related_no VARCHAR(50) NOT NULL COMMENT '关联单号',
    operator_id BIGINT NOT NULL COMMENT '分配的操作员ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态(PENDING:待处理/PROCESSING:进行中/COMPLETED:已完成)',
    priority INT DEFAULT 2 COMMENT '优先级(1:高/2:中/3:低)',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_task_type (task_type),
    INDEX idx_related_no (related_no),
    INDEX idx_operator_id (operator_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    FOREIGN KEY (operator_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

CREATE TABLE IF NOT EXISTS exception_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '异常报告ID',
    reporter_id BIGINT NOT NULL COMMENT '上报人ID',
    product_id BIGINT COMMENT '关联商品ID',
    location_id BIGINT COMMENT '关联货位ID',
    exception_type VARCHAR(20) NOT NULL COMMENT '异常类型(DIFF:差异/DAMAGED:破损/INVENTORY_ABNORMAL:库存异常)',
    description VARCHAR(1000) NOT NULL COMMENT '描述',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态(PENDING:待处理/PROCESSING:处理中/COMPLETED:已完成)',
    handler_id BIGINT COMMENT '处理人ID',
    result VARCHAR(1000) COMMENT '处理结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_product_id (product_id),
    INDEX idx_location_id (location_id),
    INDEX idx_exception_type (exception_type),
    INDEX idx_status (status),
    INDEX idx_handler_id (handler_id),
    FOREIGN KEY (reporter_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE SET NULL,
    FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE SET NULL,
    FOREIGN KEY (handler_id) REFERENCES user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常报告表';

CREATE TABLE IF NOT EXISTS inventory_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '盘点单ID',
    check_no VARCHAR(50) NOT NULL UNIQUE COMMENT '盘点单号',
    zone VARCHAR(100) COMMENT '区域',
    operator_id BIGINT NOT NULL COMMENT '操作员ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态(PENDING:待盘点/PROCESSING:进行中/COMPLETED:已完成)',
    diff_description VARCHAR(1000) COMMENT '差异描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_check_no (check_no),
    INDEX idx_zone (zone),
    INDEX idx_operator_id (operator_id),
    INDEX idx_status (status),
    FOREIGN KEY (operator_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='盘点表';

CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    perm_code VARCHAR(50) NOT NULL UNIQUE COMMENT '权限编码',
    perm_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    menu_url VARCHAR(200) COMMENT '菜单URL',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_perm_code (perm_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '操作日志ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    operation_type VARCHAR(50) COMMENT '操作类型',
    operation_module VARCHAR(50) COMMENT '操作模块',
    method_name VARCHAR(100) COMMENT '方法名称',
    operation_desc VARCHAR(500) COMMENT '操作描述',
    request_param VARCHAR(2000) COMMENT '请求参数',
    response_result VARCHAR(2000) COMMENT '响应结果',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_operator_id (operator_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operation_module (operation_module),
    INDEX idx_operation_time (operation_time),
    FOREIGN KEY (operator_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS backup_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '备份日志ID',
    backup_type VARCHAR(50) COMMENT '备份类型',
    backup_path VARCHAR(500) COMMENT '备份路径',
    file_name VARCHAR(200) COMMENT '文件名',
    file_size BIGINT COMMENT '文件大小',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '状态(SUCCESS:成功/FAILED:失败)',
    error_msg VARCHAR(1000) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_backup_type (backup_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备份日志表';

INSERT INTO role (role_code, role_name, description) VALUES
('OPERATOR', '仓储操作员', '负责日常出入库操作'),
('ADMINISTRATOR', '仓储主管', '负责仓库管理和人员调度'),
('MANAGER', '管理员', '系统管理员');

INSERT INTO permission (perm_code, perm_name, parent_id, menu_url, sort_order) VALUES
('system', '系统管理', 0, '/system', 1),
('user', '用户管理', 1, '/system/user', 10),
('role', '角色管理', 1, '/system/role', 20),
('permission', '权限管理', 1, '/system/permission', 30),
('warehouse', '仓库管理', 0, '/warehouse', 2),
('warehouse_list', '仓库列表', 5, '/warehouse/list', 10),
('location', '货位管理', 5, '/warehouse/location', 20),
('inventory', '库存管理', 0, '/inventory', 3),
('product', '商品管理', 8, '/inventory/product', 10),
('stock', '库存查询', 8, '/inventory/stock', 20),
('stock_in', '入库管理', 0, '/stock/in', 4),
('stock_out', '出库管理', 0, '/stock/out', 5),
('task', '任务管理', 0, '/task', 6),
('exception', '异常管理', 0, '/exception', 7),
('check', '盘点管理', 0, '/check', 8);

INSERT INTO role_permission (role_id, permission_id) VALUES
(1, 1), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15),
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11), (3, 12), (3, 13), (3, 14), (3, 15);

INSERT INTO user (work_no, phone, name, password, role_code, status) VALUES
('admin001', '13800138001', '管理员', '$2b$10$4/GF9v9B33DJE.wng/dsHOwFwf6ayJm1Zqh4KA4f.zvU5rGG2QqQG', 'MANAGER', 1),
('admin002', '13800138002', '仓储主管', '$2b$10$4/GF9v9B33DJE.wng/dsHOwFwf6ayJm1Zqh4KA4f.zvU5rGG2QqQG', 'ADMINISTRATOR', 1),
('op001', '13800138003', '操作员张三', '$2b$10$4/GF9v9B33DJE.wng/dsHOwFwf6ayJm1Zqh4KA4f.zvU5rGG2QqQG', 'OPERATOR', 1);

INSERT INTO warehouse (warehouse_code, warehouse_name, name, address, zone_info, capacity, used_capacity) VALUES
('WH001', '北京仓库', '北京仓库', '北京市朝阳区建国路88号', 'A区:电子产品,B区:日用品,C区:食品', 10000, 2500),
('WH002', '上海仓库', '上海仓库', '上海市浦东新区陆家嘴环路1000号', 'A区:服装,B区:家电,C区:化妆品', 8000, 3000),
('WH003', '广州仓库', '广州仓库', '广州市天河区珠江新城花城大道88号', 'A区:生鲜,B区:母婴用品,C区:家居', 12000, 4000);

INSERT INTO location (location_code, warehouse_id, zone, status) VALUES
('BJA001', 1, 'A区', 'EMPTY'),
('BJA002', 1, 'A区', 'EMPTY'),
('BJB001', 1, 'B区', 'EMPTY'),
('BJB002', 1, 'B区', 'EMPTY'),
('BJC001', 1, 'C区', 'EMPTY'),
('BJC002', 1, 'C区', 'EMPTY'),
('SHA001', 2, 'A区', 'EMPTY'),
('SHA002', 2, 'A区', 'EMPTY'),
('SHB001', 2, 'B区', 'EMPTY'),
('SHB002', 2, 'B区', 'EMPTY'),
('GZA001', 3, 'A区', 'EMPTY'),
('GZA002', 3, 'A区', 'EMPTY'),
('GZB001', 3, 'B区', 'EMPTY'),
('GZB002', 3, 'B区', 'EMPTY');

INSERT INTO product (product_code, name, spec, unit) VALUES
('P001', 'iPhone 15 Pro', '256GB 蓝色', '台'),
('P002', 'MacBook Pro 14', 'M3 Pro 18GB 512GB', '台'),
('P003', '小米14', '12GB+256GB 黑色', '台'),
('P004', '华为Mate60 Pro', '12GB+512GB 昆仑玻璃', '台'),
('P005', '洗衣液', '5kg/瓶', '瓶'),
('P006', '方便面', '5连包', '组'),
('P007', '矿泉水', '550ml*24瓶', '箱'),
('P008', '牛奶', '250ml*12盒', '箱'),
('P009', '笔记本电脑', '14寸 i7 16GB 512GB', '台'),
('P010', '打印机', '激光彩色', '台');