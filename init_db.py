import pymysql

conn = pymysql.connect(
    host='localhost',
    user='root',
    password='123456',
    charset='utf8mb4'
)

try:
    with conn.cursor() as cursor:
        cursor.execute("DROP DATABASE IF EXISTS jd_wms")
        cursor.execute("CREATE DATABASE jd_wms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
        cursor.execute("USE jd_wms")
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS `user` (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                work_no VARCHAR(50) NOT NULL UNIQUE,
                phone VARCHAR(20) NOT NULL UNIQUE,
                name VARCHAR(50) NOT NULL,
                password VARCHAR(255) NOT NULL,
                role_code VARCHAR(20) NOT NULL,
                status INT DEFAULT 1,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        # 使用MD5+盐加密，密码是123456
        hashed_pwd = '327c68ed73a8382ddedc2cd122a59fec'
        
        users = [
            ('admin001', '13800138001', '管理员', hashed_pwd, 'MANAGER', 1),
            ('admin002', '13800138002', '仓储主管', hashed_pwd, 'ADMINISTRATOR', 1),
            ('op001', '13800138003', '操作员张三', hashed_pwd, 'OPERATOR', 1)
        ]
        
        cursor.executemany("""
            INSERT INTO user (work_no, phone, name, password, role_code, status) 
            VALUES (%s, %s, %s, %s, %s, %s)
        """, users)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS role (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                role_code VARCHAR(50) NOT NULL UNIQUE,
                role_name VARCHAR(100) NOT NULL,
                description VARCHAR(500),
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        roles = [
            ('OPERATOR', '仓储操作员', '仓库操作员'),
            ('ADMINISTRATOR', '仓储主管', '仓库主管'),
            ('MANAGER', '管理员', '系统管理员')
        ]
        
        cursor.executemany("INSERT INTO role (role_code, role_name, description) VALUES (%s, %s, %s)", roles)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS permission (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                perm_code VARCHAR(50) NOT NULL UNIQUE,
                perm_name VARCHAR(50) NOT NULL,
                parent_id BIGINT DEFAULT 0,
                menu_url VARCHAR(200),
                sort_order INT DEFAULT 0,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        permissions = [
            ('system', '系统管理', 0, '/system', 1),
            ('user', '用户管理', 1, '/system/user', 10),
            ('role', '角色管理', 1, '/system/role', 20),
            ('permission', '权限管理', 1, '/system/permission', 30),
            ('warehouse', '仓库管理', 0, '/warehouse', 2),
            ('location', '货位管理', 5, '/warehouse/location', 20),
            ('inventory', '库存管理', 0, '/inventory', 3),
            ('product', '商品管理', 7, '/inventory/product', 10),
            ('stock_in', '入库管理', 0, '/stock/in', 4),
            ('stock_out', '出库管理', 0, '/stock/out', 5),
            ('task', '任务管理', 0, '/task', 6),
            ('exception', '异常管理', 0, '/exception', 7),
            ('check', '盘点管理', 0, '/check', 8)
        ]
        
        cursor.executemany("INSERT INTO permission (perm_code, perm_name, parent_id, menu_url, sort_order) VALUES (%s, %s, %s, %s, %s)", permissions)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS role_permission (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                role_id BIGINT NOT NULL,
                permission_id BIGINT NOT NULL,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """)
        
        role_permissions = [
            (1, 1), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13),
            (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10), (2, 11), (2, 12), (2, 13),
            (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11), (3, 12), (3, 13)
        ]
        
        cursor.executemany("INSERT INTO role_permission (role_id, permission_id) VALUES (%s, %s)", role_permissions)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS user_role (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                user_id BIGINT NOT NULL,
                role_id BIGINT NOT NULL,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """)
        
        user_roles = [(1, 3), (2, 2), (3, 1)]
        cursor.executemany("INSERT INTO user_role (user_id, role_id) VALUES (%s, %s)", user_roles)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS warehouse (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                warehouse_code VARCHAR(50) NOT NULL UNIQUE,
                name VARCHAR(100) NOT NULL,
                warehouse_name VARCHAR(100),
                address VARCHAR(500),
                zone_info VARCHAR(500),
                status INT DEFAULT 1,
                capacity INT DEFAULT 10000,
                used_capacity INT DEFAULT 0,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        cursor.execute("INSERT INTO warehouse (warehouse_code, name, warehouse_name, address, zone_info, capacity, used_capacity) VALUES ('WH001', '北京仓库', '北京仓库', '北京市朝阳区建国路88号', 'A区:1-10,B区:11-20,C区:21-30', 10000, 2500)")
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS location (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                location_code VARCHAR(50) NOT NULL UNIQUE,
                warehouse_id BIGINT NOT NULL,
                zone VARCHAR(20),
                status VARCHAR(20) DEFAULT 'EMPTY',
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        locations = [
            ('A001', 1, 'A区'), ('A002', 1, 'A区'), ('A003', 1, 'A区'), ('A004', 1, 'A区'),
            ('B001', 1, 'B区'), ('B002', 1, 'B区'), ('B003', 1, 'B区'), ('B004', 1, 'B区'),
            ('C001', 1, 'C区'), ('C002', 1, 'C区')
        ]
        cursor.executemany("INSERT INTO location (location_code, warehouse_id, zone) VALUES (%s, %s, %s)", locations)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS product (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                product_code VARCHAR(50) NOT NULL UNIQUE,
                name VARCHAR(200) NOT NULL,
                spec VARCHAR(200),
                unit VARCHAR(20) NOT NULL,
                shelf_life_days INT DEFAULT 365,
                category VARCHAR(50),
                min_stock INT DEFAULT 10,
                max_stock INT DEFAULT 1000,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        products = [
            ('P001', 'iPhone 15 Pro', '256GB', '台', 365, '电子产品', 10, 100), 
            ('P002', '洗衣液', '500ml', '瓶', 730, '日用品', 50, 500), 
            ('P003', '方便面', '5连包', '组', 180, '食品', 100, 500),
            ('P004', '笔记本电脑', '16GB+512GB', '台', 365, '电子产品', 5, 50),
            ('P005', '耳机', '无线蓝牙', '副', 365, '电子产品', 20, 200),
            ('P006', '充电宝', '10000mAh', '个', 365, '电子产品', 10, 100),
            ('P007', '数据线', 'Type-C 1m', '条', 365, '电子产品', 50, 300),
            ('P008', '鼠标', '无线办公', '个', 365, '电子产品', 20, 100)
        ]
        cursor.executemany("INSERT INTO product (product_code, name, spec, unit, shelf_life_days, category, min_stock, max_stock) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)", products)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS inventory (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                product_id BIGINT NOT NULL,
                location_id BIGINT NOT NULL,
                batch_no VARCHAR(50) NOT NULL,
                quantity INT NOT NULL DEFAULT 0,
                version INT DEFAULT 0,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        inventory = [
            (1, 1, 'B20240101', 100, 0),
            (2, 2, 'B20240102', 500, 0),
            (3, 3, 'B20240103', 200, 0),
            (4, 4, 'B20240104', 50, 0),
            (5, 5, 'B20240105', 200, 0),
            (6, 6, 'B20240106', 150, 0),
            (7, 7, 'B20240107', 300, 0),
            (8, 8, 'B20240108', 100, 0)
        ]
        cursor.executemany("INSERT INTO inventory (product_id, location_id, batch_no, quantity, version) VALUES (%s, %s, %s, %s, %s)", inventory)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS stock_in (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                stock_in_no VARCHAR(50) NOT NULL UNIQUE,
                operator_id BIGINT NOT NULL,
                product_id BIGINT NOT NULL,
                batch_no VARCHAR(50),
                location_id BIGINT NOT NULL,
                quantity INT NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                remark VARCHAR(500),
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        stock_ins = [
            ('RK20240101', 3, 1, 'B20240101', 1, 50, 'COMPLETED', '正常入库'),
            ('RK20240102', 3, 2, 'B20240102', 2, 200, 'COMPLETED', '正常入库'),
            ('RK20240103', 3, 3, 'B20240103', 3, 100, 'PENDING', '待处理'),
            ('RK20240104', 3, 1, 'B20240104', 4, 30, 'PROCESSING', '处理中')
        ]
        cursor.executemany("INSERT INTO stock_in (stock_in_no, operator_id, product_id, batch_no, location_id, quantity, status, remark) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)", stock_ins)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS stock_out (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                stock_out_no VARCHAR(50) NOT NULL UNIQUE,
                operator_id BIGINT NOT NULL,
                order_no VARCHAR(50),
                product_id BIGINT NOT NULL,
                batch_no VARCHAR(50),
                location_id BIGINT NOT NULL,
                quantity INT NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                remark VARCHAR(500),
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        stock_outs = [
            ('CK20240101', 3, 'DD20240101', 1, 'B20240101', 1, 20, 'COMPLETED', '正常出库'),
            ('CK20240102', 3, 'DD20240102', 2, 'B20240102', 2, 100, 'COMPLETED', '正常出库'),
            ('CK20240103', 3, 'DD20240103', 3, 'B20240103', 3, 50, 'PENDING', '待处理'),
            ('CK20240104', 3, 'DD20240104', 1, 'B20240101', 1, 10, 'PROCESSING', '处理中')
        ]
        cursor.executemany("INSERT INTO stock_out (stock_out_no, operator_id, order_no, product_id, batch_no, location_id, quantity, status, remark) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)", stock_outs)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS task (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                task_no VARCHAR(50) NOT NULL UNIQUE,
                task_type VARCHAR(20) NOT NULL,
                related_no VARCHAR(50) NOT NULL,
                operator_id BIGINT NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                priority INT DEFAULT 2,
                remark VARCHAR(500),
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        tasks = [
            ('TSK20240101', 'STOCK_IN', 'RK20240103', 3, 'PENDING', 2),
            ('TSK20240102', 'STOCK_IN', 'RK20240104', 3, 'IN_PROGRESS', 1),
            ('TSK20240103', 'STOCK_OUT', 'CK20240103', 3, 'PENDING', 2),
            ('TSK20240104', 'STOCK_OUT', 'CK20240104', 3, 'IN_PROGRESS', 1),
            ('TSK20240105', 'CHECK', 'PD20240101', 3, 'PENDING', 3)
        ]
        cursor.executemany("INSERT INTO task (task_no, task_type, related_no, operator_id, status, priority) VALUES (%s, %s, %s, %s, %s, %s)", tasks)
        
        cursor.execute("""
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
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        exceptions = [
            (3, 2, 2, 'DAMAGED', '发现5瓶洗衣液包装破损，已隔离存放', 'PENDING', None, None),
            (3, 3, 3, 'QUANTITY_MISMATCH', '盘点发现方便面库存短缺10组', 'HANDLED', 3, '已补货完成'),
            (3, 1, 1, 'OTHER', '发现一批iPhone即将过期，需要处理', 'PENDING', None, None)
        ]
        cursor.executemany("INSERT INTO exception_report (reporter_id, product_id, location_id, exception_type, description, status, handler_id, result) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)", exceptions)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS inventory_check (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                check_no VARCHAR(50) NOT NULL UNIQUE,
                zone VARCHAR(20),
                operator_id BIGINT NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                diff_description VARCHAR(1000),
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        checks = [
            ('PD20240101', 'A区', 3, 'PENDING', None),
            ('PD20240102', 'B区', 3, 'COMPLETED', '盘点完成，无差异')
        ]
        cursor.executemany("INSERT INTO inventory_check (check_no, zone, operator_id, status, diff_description) VALUES (%s, %s, %s, %s, %s)", checks)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS backup_log (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                backup_type VARCHAR(20) NOT NULL,
                file_name VARCHAR(200) NOT NULL,
                file_path VARCHAR(500) NOT NULL,
                file_size BIGINT DEFAULT 0,
                status VARCHAR(20) DEFAULT 'SUCCESS',
                error_message VARCHAR(1000),
                operator_id BIGINT,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """)
        
        conn.commit()
        print("Database initialized successfully!")
        
finally:
    conn.close()
