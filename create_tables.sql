USE jd_wms;

CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO role (role_code, role_name, description) VALUES
('OPERATOR', '仓储操作员', '负责日常出入库操作'),
('ADMINISTRATOR', '仓储主管', '负责仓库管理和人员调度'),
('MANAGER', '管理员', '系统管理员');

CREATE TABLE IF NOT EXISTS permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    perm_code VARCHAR(50) NOT NULL UNIQUE,
    perm_name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    menu_url VARCHAR(200),
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

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

CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
);

INSERT INTO role_permission (role_id, permission_id) VALUES
(1, 1), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15),
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11), (3, 12), (3, 13), (3, 14), (3, 15);

CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
);

INSERT INTO user_role (user_id, role_id) VALUES
(1, 3),
(2, 2),
(3, 1);

CREATE TABLE IF NOT EXISTS warehouse (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(500),
    zone_info VARCHAR(500),
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
);

INSERT INTO warehouse (name, address, zone_info) VALUES
('北京仓库', '北京市朝阳区建国路88号', 'A区:电子产品,B区:日用品,C区:食品');

CREATE TABLE IF NOT EXISTS location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    location_code VARCHAR(50) NOT NULL UNIQUE,
    warehouse_id BIGINT NOT NULL,
    zone VARCHAR(50),
    status VARCHAR(20) DEFAULT 'EMPTY',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_location_code (location_code),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_status (status)
);

INSERT INTO location (location_code, warehouse_id, zone, status) VALUES
('A001', 1, 'A区', 'EMPTY'),
('A002', 1, 'A区', 'EMPTY'),
('B001', 1, 'B区', 'EMPTY'),
('B002', 1, 'B区', 'EMPTY'),
('C001', 1, 'C区', 'EMPTY'),
('C002', 1, 'C区', 'EMPTY');

CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    spec VARCHAR(200),
    unit VARCHAR(20) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_code (product_code)
);

INSERT INTO product (product_code, name, spec, unit) VALUES
('P001', 'iPhone 15 Pro', '256GB 蓝色', '台'),
('P002', 'MacBook Pro 14', 'M3 Pro 18GB 512GB', '台'),
('P003', '小米14', '12GB+256GB 黑色', '台'),
('P004', '洗衣液', '5kg/瓶', '瓶'),
('P005', '方便面', '5连包', '组'),
('P006', '矿泉水', '550ml*24瓶', '箱');

CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    version INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id),
    INDEX idx_location_id (location_id),
    INDEX idx_batch_no (batch_no)
);

INSERT INTO inventory (product_id, location_id, batch_no, quantity) VALUES
(1, 1, 'B20240101', 100),
(2, 2, 'B20240102', 50),
(3, 3, 'B20240103', 200),
(4, 4, 'B20240104', 500),
(5, 5, 'B20240105', 300),
(6, 6, 'B20240106', 100);

CREATE TABLE IF NOT EXISTS stock_in (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stock_in_no VARCHAR(50) NOT NULL UNIQUE,
    operator_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    location_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_stock_in_no (stock_in_no),
    INDEX idx_operator_id (operator_id),
    INDEX idx_product_id (product_id),
    INDEX idx_status (status)
);

CREATE TABLE IF NOT EXISTS stock_out (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stock_out_no VARCHAR(50) NOT NULL UNIQUE,
    operator_id BIGINT NOT NULL,
    order_no VARCHAR(50),
    product_id BIGINT NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    location_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_stock_out_no (stock_out_no),
    INDEX idx_operator_id (operator_id),
    INDEX idx_order_no (order_no),
    INDEX idx_product_id (product_id),
    INDEX idx_status (status)
);

CREATE TABLE IF NOT EXISTS task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_type VARCHAR(20) NOT NULL,
    related_no VARCHAR(50) NOT NULL,
    operator_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    priority INT DEFAULT 2,
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task_type (task_type),
    INDEX idx_related_no (related_no),
    INDEX idx_operator_id (operator_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority)
);

CREATE TABLE IF NOT EXISTS exception_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    product_id BIGINT,
    location_id BIGINT,
    exception_type VARCHAR(20) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    handler_id BIGINT,
    result VARCHAR(1000),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_product_id (product_id),
    INDEX idx_location_id (location_id),
    INDEX idx_exception_type (exception_type),
    INDEX idx_status (status),
    INDEX idx_handler_id (handler_id)
);

CREATE TABLE IF NOT EXISTS inventory_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    check_no VARCHAR(50) NOT NULL UNIQUE,
    zone VARCHAR(100),
    operator_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    diff_description VARCHAR(1000),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_check_no (check_no),
    INDEX idx_zone (zone),
    INDEX idx_operator_id (operator_id),
    INDEX idx_status (status)
);

CREATE TABLE IF NOT EXISTS backup_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    backup_type VARCHAR(20) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    operator_id BIGINT,
    status VARCHAR(20) DEFAULT 'SUCCESS',
    error_message VARCHAR(1000),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS performance_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date DATE NOT NULL,
    stock_in_count INT DEFAULT 0,
    stock_out_count INT DEFAULT 0,
    task_completed_count INT DEFAULT 0,
    exception_count INT DEFAULT 0,
    avg_task_duration DECIMAL(10,2),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stat_date (stat_date)
);

CREATE TABLE IF NOT EXISTS inventory_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    location_id BIGINT,
    alert_type VARCHAR(20) NOT NULL,
    threshold INT NOT NULL,
    current_value INT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id),
    INDEX idx_location_id (location_id),
    INDEX idx_alert_type (alert_type),
    INDEX idx_status (status)
);

CREATE TABLE IF NOT EXISTS quality_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    check_no VARCHAR(50) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    operator_id BIGINT NOT NULL,
    check_result VARCHAR(20) DEFAULT 'PENDING',
    defective_count INT DEFAULT 0,
    description VARCHAR(1000),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_check_no (check_no),
    INDEX idx_product_id (product_id),
    INDEX idx_batch_no (batch_no),
    INDEX idx_operator_id (operator_id),
    INDEX idx_check_result (check_result)
);
