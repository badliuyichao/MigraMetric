-- ============================================
-- MigraMetric 初始化数据脚本
-- 数据库版本: V1.0
-- 创建日期: 2026-03-19
-- ============================================

-- 使用数据库
USE migrametric_dev;

-- ============================================
-- 1. 用户数据 (密码使用 BCrypt 加密)
-- 默认密码: admin123
-- BCrypt加密后的密码: $2a$10$...
-- ============================================

INSERT INTO sys_user (username, password, user_name, email, phone, role, status, create_by) VALUES
-- 管理员用户 (密码: admin123)
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin@migrametric.com', '13800138000', 'ADMIN', 1, 'system'),
-- 普通用户 (密码: user123)
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', 'user@migrametric.com', '13900139000', 'USER', 1, 'system');

-- ============================================
-- 2. 角色数据
-- ============================================

INSERT INTO sys_role (role_code, role_name, role_sort, description, status, create_by) VALUES
('ADMIN', '系统管理员', 1, '系统管理员，拥有所有权限', 1, 'system'),
('USER', '普通用户', 2, '普通用户，拥有基础操作权限', 1, 'system');

-- ============================================
-- 3. 权限数据
-- ============================================

INSERT INTO sys_permission (perm_code, perm_name, perm_type, perm_sort, path, icon, parent_id, status, create_by) VALUES
-- 顶级菜单
('SYSTEM', '系统管理', 'menu', 100, '/system', 'Setting', 0, 1, 'system'),
('PROJECT', '项目管理', 'menu', 200, '/project', 'Folder', 0, 1, 'system'),
('DASHBOARD', '首页', 'menu', 1, '/dashboard', 'HomeFilled', 0, 1, 'system'),

-- 系统管理子菜单
('SYSTEM_TYPES', '系统管理', 'menu', 101, '/system/types', NULL, (SELECT id FROM sys_permission WHERE perm_code='SYSTEM'), 1, 'system'),

-- 项目管理子菜单
('PROJECT_LIST', '项目列表', 'menu', 201, '/project/list', NULL, (SELECT id FROM sys_permission WHERE perm_code='PROJECT'), 1, 'system'),
('PROJECT_CREATE', '创建项目', 'menu', 202, '/project/create', NULL, (SELECT id FROM sys_permission WHERE perm_code='PROJECT'), 1, 'system');

-- ============================================
-- 4. 系统类型数据
-- ============================================

INSERT INTO sys_system_type (system_name, system_category, description, status, create_by) VALUES
-- 源系统
('SAP', 1, 'SAP ERP系统，国际主流ERP系统', 1, 'system'),
('Oracle EBS', 1, 'Oracle电子商务套件', 1, 'system'),
('用友U8', 1, '用友U8 ERP系统', 1, 'system'),
('金蝶K3', 1, '金蝶K3 WISE系统', 1, 'system'),
('浪潮GS', 1, '浪潮GS企业管理软件', 1, 'system'),
('神舟数码', 1, '神舟数码DCMS系统', 1, 'system'),

-- 目标系统
('用友NC', 2, '用友NC Cloud大型企业数字化平台', 1, 'system'),
('用友U9', 2, '用友U9 Cloud中型企业云ERP', 1, 'system'),
('金蝶云星空', 2, '金蝶云·星空大型企业SaaS管理云', 1, 'system'),
('金蝶云星辰', 2, '金蝶云·星辰小型企业SaaS云服务', 1, 'system'),
('SAP S/4HANA', 2, 'SAP S/4HANA新一代ERP系统', 1, 'system'),
('Oracle Cloud', 2, 'Oracle Cloud ERP云服务', 1, 'system');

-- ============================================
-- 5. 模块数据 (基于用友NC)
-- ============================================

INSERT INTO sys_module (module_name, system_id, category, base_workload, default_weight, description, status, create_by)
SELECT '财务会计', id, '财务模块', 15.00, 1.00, '总账、应收应付、固定资产等', 1, 'system'
FROM sys_system_type WHERE system_name = '用友NC';

INSERT INTO sys_module (module_name, system_id, category, base_workload, default_weight, description, status, create_by)
SELECT '管理会计', id, '财务模块', 12.00, 1.00, '成本管理、预算管理、内部交易等', 1, 'system'
FROM sys_system_type WHERE system_name = '用友NC';

INSERT INTO sys_module (module_name, system_id, category, base_workload, default_weight, description, status, create_by)
SELECT '供应链管理', id, '供应链模块', 18.00, 1.20, '采购管理、销售管理、库存管理', 1, 'system'
FROM sys_system_type WHERE system_name = '用友NC';

INSERT INTO sys_module (module_name, system_id, category, base_workload, default_weight, description, status, create_by)
SELECT '生产制造', id, '生产模块', 20.00, 1.30, '生产计划、物料需求计划、设备管理', 1, 'system'
FROM sys_system_type WHERE system_name = '用友NC';

INSERT INTO sys_module (module_name, system_id, category, base_workload, default_weight, description, status, create_by)
SELECT '人力资源', id, 'HR模块', 10.00, 1.00, '人事管理、薪酬管理、考勤管理', 1, 'system'
FROM sys_system_type WHERE system_name = '用友NC';

INSERT INTO sys_module (module_name, system_id, category, base_workload, default_weight, description, status, create_by)
SELECT '客户关系管理', id, 'CRM模块', 8.00, 0.90, '客户管理、销售管理、服务管理', 1, 'system'
FROM sys_system_type WHERE system_name = '用友NC';

-- ============================================
-- 6. 数据量阶梯配置
-- ============================================

INSERT INTO sys_data_volume_ladder (ladder_name, min_volume, max_volume, weight, sort_order, create_by) VALUES
('微型', 0, 10, 0.60, 1, 'system'),
('小型', 10, 50, 0.80, 2, 'system'),
('中型', 50, 200, 1.00, 3, 'system'),
('大型', 200, 500, 1.20, 4, 'system'),
('超大型', 500, NULL, 1.50, 5, 'system');

-- ============================================
-- 7. 用户数阶梯配置
-- ============================================

INSERT INTO sys_user_count_ladder (ladder_name, min_count, max_count, weight, sort_order, create_by) VALUES
('微小型', 0, 50, 0.70, 1, 'system'),
('小型', 50, 200, 0.85, 2, 'system'),
('中型', 200, 500, 1.00, 3, 'system'),
('大型', 500, 1000, 1.15, 4, 'system'),
('超大型', 1000, NULL, 1.30, 5, 'system');

-- ============================================
-- 8. 报表配置
-- ============================================

INSERT INTO sys_report_config (config_key, config_value, config_name, description, status, create_by) VALUES
('report_workload_per_unit', '0.50', '报表迁移单位工作量', '每个报表迁移的工作量（人天/个）', 1, 'system'),
('core_migration_formula', 'base_workload * weight * data_vol_weight * user_count_weight', '核心迁移公式', '核心迁移工作量计算公式', 1, 'system'),
('report_migration_formula', 'report_count * report_workload_per_unit', '报表迁移公式', '报表迁移工作量计算公式', 1, 'system');

-- ============================================
-- 输出完成信息
-- ============================================

SELECT '========================================' AS '';
SELECT '初始化数据导入完成!' AS 'Message';
SELECT '========================================' AS '';

-- 统计各表数据量
SELECT 'sys_user' AS 'table_name', COUNT(*) AS 'count' FROM sys_user
UNION ALL
SELECT 'sys_role', COUNT(*) FROM sys_role
UNION ALL
SELECT 'sys_permission', COUNT(*) FROM sys_permission
UNION ALL
SELECT 'sys_system_type', COUNT(*) FROM sys_system_type
UNION ALL
SELECT 'sys_module', COUNT(*) FROM sys_module
UNION ALL
SELECT 'sys_data_volume_ladder', COUNT(*) FROM sys_data_volume_ladder
UNION ALL
SELECT 'sys_user_count_ladder', COUNT(*) FROM sys_user_count_ladder
UNION ALL
SELECT 'sys_report_config', COUNT(*) FROM sys_report_config
ORDER BY table_name;
