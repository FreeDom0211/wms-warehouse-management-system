USE wms;
DELETE FROM user;
INSERT INTO user (work_no, phone, name, password, role_code, status) VALUES
('admin001', '13800138001', '管理员', '$2b$12$OjOy1w.bYrrpVXcilLo./eSs8qwo52uAzQ1Bnoj59Lx2DdVWysZ/y', 'ADMINISTRATOR', 1),
('admin002', '13800138002', '仓储主管', '$2b$12$OjOy1w.bYrrpVXcilLo./eSs8qwo52uAzQ1Bnoj59Lx2DdVWysZ/y', 'MANAGER', 1),
('op001', '13800138003', '操作员', '$2b$12$OjOy1w.bYrrpVXcilLo./eSs8qwo52uAzQ1Bnoj59Lx2DdVWysZ/y', 'OPERATOR', 1);
