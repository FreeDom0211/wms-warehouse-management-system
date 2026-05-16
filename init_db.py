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
        
        hashed_pwd = '$2a$10$5ZfgbeZ70SivfVb0gIq8kOVTZnBKREnlxmPLZjQhrOkGPuzPUY/PS'
        
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
                name VARCHAR(100) NOT NULL,
                address VARCHAR(500),
                status INT DEFAULT 1,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        cursor.execute("INSERT INTO warehouse (name, address) VALUES ('北京仓库', '北京市朝阳区建国路88号')")
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS location (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                location_code VARCHAR(50) NOT NULL UNIQUE,
                warehouse_id BIGINT NOT NULL,
                status VARCHAR(20) DEFAULT 'EMPTY',
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        locations = [('A001', 1), ('A002', 1), ('B001', 1), ('B002', 1)]
        cursor.executemany("INSERT INTO location (location_code, warehouse_id) VALUES (%s, %s)", locations)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS product (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                product_code VARCHAR(50) NOT NULL UNIQUE,
                name VARCHAR(200) NOT NULL,
                unit VARCHAR(20) NOT NULL,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        products = [('P001', 'iPhone 15 Pro', '台'), ('P002', '洗衣液', '瓶'), ('P003', '方便面', '组')]
        cursor.executemany("INSERT INTO product (product_code, name, unit) VALUES (%s, %s, %s)", products)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS inventory (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                product_id BIGINT NOT NULL,
                location_id BIGINT NOT NULL,
                batch_no VARCHAR(50) NOT NULL,
                quantity INT NOT NULL DEFAULT 0,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        inventory = [(1, 1, 'B20240101', 100), (2, 2, 'B20240102', 500), (3, 3, 'B20240103', 200)]
        cursor.executemany("INSERT INTO inventory (product_id, location_id, batch_no, quantity) VALUES (%s, %s, %s, %s)", inventory)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS stock_in (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                stock_in_no VARCHAR(50) NOT NULL UNIQUE,
                operator_id BIGINT NOT NULL,
                product_id BIGINT NOT NULL,
                location_id BIGINT NOT NULL,
                quantity INT NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS stock_out (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                stock_out_no VARCHAR(50) NOT NULL UNIQUE,
                operator_id BIGINT NOT NULL,
                product_id BIGINT NOT NULL,
                location_id BIGINT NOT NULL,
                quantity INT NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS task (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                task_type VARCHAR(20) NOT NULL,
                related_no VARCHAR(50) NOT NULL,
                operator_id BIGINT NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                priority INT DEFAULT 2,
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS exception_report (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                reporter_id BIGINT NOT NULL,
                exception_type VARCHAR(20) NOT NULL,
                description VARCHAR(1000) NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS inventory_check (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                check_no VARCHAR(50) NOT NULL UNIQUE,
                operator_id BIGINT NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """)
        
        conn.commit()
        print("Database initialized successfully!")
        
finally:
    conn.close()
