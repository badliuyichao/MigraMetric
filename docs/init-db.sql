-- ============================================
-- MigraMetric 数据库初始化脚本
-- 异构系统升迁工作量评估系统
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS migrametric
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE migrametric;

-- ============================================
-- 第一部分：用户域表
-- ============================================

-- -------------------------------------------
-- 1. sys_user - 用户表
-- -------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username            VARCHAR(50)     NOT NULL COMMENT '用户名（登录账号）',
    password            VARCHAR(200)    NOT NULL COMMENT '密码（BCrypt加密）',
    user_name           VARCHAR(50)     NOT NULL COMMENT '用户姓名',
    email               VARCHAR(100)             DEFAULT NULL COMMENT '邮箱',
    phone               VARCHAR(20)              DEFAULT NULL COMMENT '手机号',
    avatar              VARCHAR(500)             DEFAULT NULL COMMENT '头像URL',
    role                VARCHAR(20)     NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN-系统管理员，USER-普通用户',
    status              TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_time     DATETIME                 DEFAULT NULL COMMENT '最后登录时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    remark              VARCHAR(500)             DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_status (status),
    KEY idx_role (role),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -------------------------------------------
-- 2. sys_role - 角色表
-- -------------------------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code           VARCHAR(20)     NOT NULL COMMENT '角色代码',
    role_name           VARCHAR(50)     NOT NULL COMMENT '角色名称',
    role_sort           INT             NOT NULL DEFAULT 0 COMMENT '显示顺序',
    description         VARCHAR(500)             DEFAULT NULL COMMENT '角色描述',
    status              TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code),
    KEY idx_status (status),
    KEY idx_role_sort (role_sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- -------------------------------------------
-- 3. sys_permission - 权限表
-- -------------------------------------------
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    perm_code           VARCHAR(100)    NOT NULL COMMENT '权限代码',
    perm_name           VARCHAR(50)     NOT NULL COMMENT '权限名称',
    perm_type           VARCHAR(20)     NOT NULL COMMENT '权限类型：menu-菜单，button-按钮',
    perm_sort           INT             NOT NULL DEFAULT 0 COMMENT '显示顺序',
    path                VARCHAR(200)             DEFAULT NULL COMMENT '路由路径',
    icon                VARCHAR(100)             DEFAULT NULL COMMENT '图标',
    parent_id           BIGINT UNSIGNED          DEFAULT 0 COMMENT '父权限ID',
    status              TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    remark              VARCHAR(500)             DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- -------------------------------------------
-- 4. sys_user_role - 用户角色关联表
-- -------------------------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id             BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- -------------------------------------------
-- 5. sys_role_permission - 角色权限关联表
-- -------------------------------------------
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    role_id             BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    perm_id             BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (role_id, perm_id),
    KEY idx_perm_id (perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- -------------------------------------------
-- 6. sys_login_log - 登录日志表
-- -------------------------------------------
DROP TABLE IF EXISTS sys_login_log;
CREATE TABLE sys_login_log (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    username            VARCHAR(50)              DEFAULT NULL COMMENT '登录用户名',
    ip_address          VARCHAR(128)             DEFAULT NULL COMMENT '登录IP地址',
    login_location      VARCHAR(255)             DEFAULT NULL COMMENT '登录地点',
    browser             VARCHAR(100)             DEFAULT NULL COMMENT '浏览器类型',
    os                  VARCHAR(100)             DEFAULT NULL COMMENT '操作系统',
    status              VARCHAR(10)              DEFAULT NULL COMMENT '登录状态：success-成功，fail-失败',
    msg                 VARCHAR(500)             DEFAULT NULL COMMENT '提示消息',
    login_time          DATETIME                 DEFAULT NULL COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_username (username),
    KEY idx_login_time (login_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- -------------------------------------------
-- 6.1 sys_operation_log - 操作日志表
-- -------------------------------------------
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    module              VARCHAR(100)              DEFAULT NULL COMMENT '操作模块',
    operation_type      VARCHAR(50)               DEFAULT NULL COMMENT '操作类型',
    description         VARCHAR(500)              DEFAULT NULL COMMENT '操作描述',
    request_method      VARCHAR(100)              DEFAULT NULL COMMENT '请求方法',
    request_url         VARCHAR(500)              DEFAULT NULL COMMENT '请求URL',
    request_params      TEXT                      DEFAULT NULL COMMENT '请求参数',
    http_method         VARCHAR(20)               DEFAULT NULL COMMENT 'HTTP方法',
    response_data       TEXT                      DEFAULT NULL COMMENT '响应数据',
    user_id             BIGINT UNSIGNED           DEFAULT NULL COMMENT '操作用户ID',
    username            VARCHAR(50)               DEFAULT NULL COMMENT '操作用户名',
    ip_address          VARCHAR(128)              DEFAULT NULL COMMENT '操作IP地址',
    location            VARCHAR(255)              DEFAULT NULL COMMENT '操作地点',
    status              TINYINT                   DEFAULT 1 COMMENT '操作状态：0-失败，1-成功',
    error_msg           VARCHAR(1000)             DEFAULT NULL COMMENT '错误信息',
    execution_time      BIGINT                    DEFAULT NULL COMMENT '执行时长（毫秒）',
    create_time         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_module (module),
    KEY idx_operation_type (operation_type),
    KEY idx_username (username),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';


-- ============================================
-- 第二部分：配置域表
-- ============================================

-- -------------------------------------------
-- 7. sys_system_type - 系统类型表
-- -------------------------------------------
DROP TABLE IF EXISTS sys_system_type;
CREATE TABLE sys_system_type (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '系统类型ID',
    system_name         VARCHAR(100)    NOT NULL COMMENT '系统名称',
    system_category     TINYINT        NOT NULL DEFAULT 1 COMMENT '系统类别：1-源系统，2-目标系统',
    description         VARCHAR(500)             DEFAULT NULL COMMENT '描述信息',
    status              TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    remark              VARCHAR(500)             DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_name (system_name),
    KEY idx_system_category (system_category),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统类型表';

-- -------------------------------------------
-- 8. sys_module - 模块表
-- -------------------------------------------
DROP TABLE IF EXISTS sys_module;
CREATE TABLE sys_module (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '模块ID',
    module_name         VARCHAR(100)    NOT NULL COMMENT '模块名称',
    system_id           BIGINT UNSIGNED NOT NULL COMMENT '所属系统ID',
    category            VARCHAR(50)              DEFAULT NULL COMMENT '模块分类',
    base_workload       DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '基础工作量（人天）',
    default_weight      DECIMAL(5,2)    NOT NULL DEFAULT 1.00 COMMENT '默认加权系数',
    description         VARCHAR(500)             DEFAULT NULL COMMENT '模块描述',
    status              TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id),
    KEY idx_system_id (system_id),
    KEY idx_status (status),
    KEY idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模块表';

-- -------------------------------------------
-- 9. sys_data_volume_ladder - 数据量阶梯表
-- -------------------------------------------
DROP TABLE IF EXISTS sys_data_volume_ladder;
CREATE TABLE sys_data_volume_ladder (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '阶梯ID',
    ladder_name         VARCHAR(50)     NOT NULL COMMENT '阶梯名称',
    min_volume          DECIMAL(15,2)   NOT NULL DEFAULT 0 COMMENT '数据量下限（万条）',
    max_volume          DECIMAL(15,2)             DEFAULT NULL COMMENT '数据量上限（万条），NULL表示无上限',
    weight              DECIMAL(5,2)    NOT NULL DEFAULT 1.00 COMMENT '对应工作量系数',
    sort_order          INT             NOT NULL DEFAULT 0 COMMENT '排序顺序',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据量阶梯表';

-- -------------------------------------------
-- 10. sys_user_count_ladder - 用户数阶梯表
-- -------------------------------------------
DROP TABLE IF EXISTS sys_user_count_ladder;
CREATE TABLE sys_user_count_ladder (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '阶梯ID',
    ladder_name         VARCHAR(50)     NOT NULL COMMENT '阶梯名称',
    min_count           INT             NOT NULL DEFAULT 0 COMMENT '用户数下限',
    max_count           INT                      DEFAULT NULL COMMENT '用户数上限，NULL表示无上限',
    weight              DECIMAL(5,2)    NOT NULL DEFAULT 1.00 COMMENT '对应工作量系数',
    sort_order          INT             NOT NULL DEFAULT 0 COMMENT '排序顺序',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户数阶梯表';

-- -------------------------------------------
-- 11. sys_report_config - 报表配置表
-- -------------------------------------------
DROP TABLE IF EXISTS sys_report_config;
CREATE TABLE sys_report_config (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    config_key          VARCHAR(100)    NOT NULL COMMENT '配置键',
    config_value        VARCHAR(500)    NOT NULL COMMENT '配置值',
    config_name         VARCHAR(100)             DEFAULT NULL COMMENT '配置名称',
    description         VARCHAR(500)             DEFAULT NULL COMMENT '配置描述',
    status              TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表配置表';


-- ============================================
-- 第三部分：项目域表
-- ============================================

-- -------------------------------------------
-- 12. proj_project - 项目表
-- -------------------------------------------
DROP TABLE IF EXISTS proj_project;
CREATE TABLE proj_project (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    project_name        VARCHAR(200)    NOT NULL COMMENT '项目名称',
    customer_name       VARCHAR(200)    NOT NULL COMMENT '客户名称',
    source_system_id    BIGINT UNSIGNED NOT NULL COMMENT '源系统ID',
    target_system_id    BIGINT UNSIGNED NOT NULL COMMENT '目标系统ID',
    project_leader      VARCHAR(100)             DEFAULT NULL COMMENT '项目负责人',
    contact             VARCHAR(100)             DEFAULT NULL COMMENT '联系方式',
    description         VARCHAR(1000)            DEFAULT NULL COMMENT '项目描述',
    evaluation_date     DATE            NOT NULL COMMENT '评估日期',
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '项目状态：DRAFT-草稿，IN_PROGRESS-进行中，COMPLETED-已完成，ARCHIVED-已归档',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '创建用户ID',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_name (project_name),
    KEY idx_source_system (source_system_id),
    KEY idx_target_system (target_system_id),
    KEY idx_status (status),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';


-- ============================================
-- 第四部分：评估域表
-- ============================================

-- -------------------------------------------
-- 13. eval_evaluation - 评估表
-- -------------------------------------------
DROP TABLE IF EXISTS eval_evaluation;
CREATE TABLE eval_evaluation (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评估ID',
    project_id          BIGINT UNSIGNED NOT NULL COMMENT '项目ID',
    table_count         INT                      DEFAULT 0 COMMENT '数据库表数量（个）',
    data_volume         DECIMAL(15,2)            DEFAULT 0 COMMENT '数据量（万条）',
    data_volume_ladder_id BIGINT UNSIGNED         DEFAULT NULL COMMENT '数据量阶梯ID',
    user_count           INT                      DEFAULT 0 COMMENT '用户数量（人）',
    user_count_ladder_id BIGINT UNSIGNED          DEFAULT NULL COMMENT '用户数阶梯ID',
    report_count        INT                      DEFAULT 0 COMMENT '报表数量（个）',
    has_custom_dev      TINYINT          DEFAULT 0 COMMENT '是否有客开：0-否，1-是',
    custom_dev_count     INT                      DEFAULT 0 COMMENT '客开模块数量（个）',
    custom_dev_workload  DECIMAL(10,2)            DEFAULT 0 COMMENT '客开评估人天',
    data_clean_desc      VARCHAR(1000)            DEFAULT NULL COMMENT '数据清洗需求描述',
    data_clean_complexity TINYINT                 DEFAULT NULL COMMENT '数据清洗复杂度：1-简单，2-中等，3-复杂',
    core_workload        DECIMAL(10,2)            DEFAULT 0 COMMENT '核心迁移工作量（人天）',
    report_workload      DECIMAL(10,2)            DEFAULT 0 COMMENT '报表迁移工作量（人天）',
    total_workload       DECIMAL(10,2)            DEFAULT 0 COMMENT '总工作量（人天）',
    evaluation_status    VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '评估状态：DRAFT-草稿，IN_PROGRESS-进行中，COMPLETED-已完成',
    evaluation_time      DATETIME                 DEFAULT NULL COMMENT '评估完成时间',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_id (project_id),
    KEY idx_data_volume_ladder (data_volume_ladder_id),
    KEY idx_user_count_ladder (user_count_ladder_id),
    KEY idx_evaluation_status (evaluation_status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估表';

-- -------------------------------------------
-- 14. proj_module_config - 项目模块配置表
-- -------------------------------------------
DROP TABLE IF EXISTS proj_module_config;
CREATE TABLE proj_module_config (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    project_id          BIGINT UNSIGNED NOT NULL COMMENT '项目ID',
    module_id           BIGINT UNSIGNED NOT NULL COMMENT '模块ID',
    weight              DECIMAL(5,2)    NOT NULL COMMENT '调整后的加权系数',
    module_workload     DECIMAL(10,2)            DEFAULT 0 COMMENT '该模块的工作量（人天）',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id),
    KEY idx_project_id (project_id),
    KEY idx_module_id (module_id),
    KEY idx_project_module (project_id, module_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目模块配置表';

-- -------------------------------------------
-- 15. eval_workload_detail - 工作量明细表
-- -------------------------------------------
DROP TABLE IF EXISTS eval_workload_detail;
CREATE TABLE eval_workload_detail (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    evaluation_id       BIGINT UNSIGNED NOT NULL COMMENT '评估ID',
    module_id           BIGINT UNSIGNED NOT NULL COMMENT '模块ID',
    base_workload       DECIMAL(10,2)            DEFAULT 0 COMMENT '模块基础人天',
    weight              DECIMAL(5,2)             DEFAULT 1.00 COMMENT '加权系数',
    data_volume_weight  DECIMAL(5,2)             DEFAULT 1.00 COMMENT '数据量阶梯系数',
    user_count_weight   DECIMAL(5,2)             DEFAULT 1.00 COMMENT '用户数阶梯系数',
    module_workload     DECIMAL(10,2)            DEFAULT 0 COMMENT '模块工作量（人天）',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_evaluation_id (evaluation_id),
    KEY idx_module_id (module_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作量明细表';


-- ============================================
-- 第五部分：报告域表
-- ============================================

-- -------------------------------------------
-- 16. rpt_report - 报告表
-- -------------------------------------------
DROP TABLE IF EXISTS rpt_report;
CREATE TABLE rpt_report (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '报告ID',
    project_id          BIGINT UNSIGNED NOT NULL COMMENT '项目ID',
    report_title        VARCHAR(500)    NOT NULL COMMENT '报告标题',
    report_content      LONGTEXT                DEFAULT NULL COMMENT '报告内容（JSON格式）',
    total_workload      DECIMAL(10,2)            DEFAULT 0 COMMENT '总工作量',
    status              VARCHAR(20)     NOT NULL DEFAULT 'GENERATED' COMMENT '报告状态：GENERATED-已生成',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_id (project_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告表';

-- -------------------------------------------
-- 17. rpt_export_log - 报告导出记录表
-- -------------------------------------------
DROP TABLE IF EXISTS rpt_export_log;
CREATE TABLE rpt_export_log (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    project_id          BIGINT UNSIGNED NOT NULL COMMENT '项目ID',
    export_format       VARCHAR(20)     NOT NULL COMMENT '导出格式：EXCEL、PDF、WORD',
    export_path         VARCHAR(500)             DEFAULT NULL COMMENT '文件存储路径',
    file_name           VARCHAR(200)             DEFAULT NULL COMMENT '文件名称',
    file_size           BIGINT                  DEFAULT NULL COMMENT '文件大小（字节）',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '导出用户ID',
    status              VARCHAR(20)     NOT NULL DEFAULT 'SUCCESS' COMMENT '导出状态：SUCCESS-成功，FAIL-失败',
    error_msg           VARCHAR(500)             DEFAULT NULL COMMENT '错误信息',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '导出时间',
    PRIMARY KEY (id),
    KEY idx_project_id (project_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time),
    KEY idx_export_format (export_format)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告导出记录表';


-- ============================================
-- 第六部分：外键约束
-- ============================================

-- sys_module 表的外键约束
ALTER TABLE sys_module
ADD CONSTRAINT fk_module_system
FOREIGN KEY (system_id) REFERENCES sys_system_type(id)
ON DELETE RESTRICT ON UPDATE CASCADE;

-- proj_project 表的外键约束
ALTER TABLE proj_project
ADD CONSTRAINT fk_project_source
FOREIGN KEY (source_system_id) REFERENCES sys_system_type(id)
ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE proj_project
ADD CONSTRAINT fk_project_target
FOREIGN KEY (target_system_id) REFERENCES sys_system_type(id)
ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE proj_project
ADD CONSTRAINT fk_project_user
FOREIGN KEY (user_id) REFERENCES sys_user(id)
ON DELETE RESTRICT ON UPDATE CASCADE;

-- eval_evaluation 表的外键约束
ALTER TABLE eval_evaluation
ADD CONSTRAINT fk_evaluation_project
FOREIGN KEY (project_id) REFERENCES proj_project(id)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE eval_evaluation
ADD CONSTRAINT fk_evaluation_data_ladder
FOREIGN KEY (data_volume_ladder_id) REFERENCES sys_data_volume_ladder(id)
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE eval_evaluation
ADD CONSTRAINT fk_evaluation_user_ladder
FOREIGN KEY (user_count_ladder_id) REFERENCES sys_user_count_ladder(id)
ON DELETE SET NULL ON UPDATE CASCADE;

-- proj_module_config 表的外键约束
ALTER TABLE proj_module_config
ADD CONSTRAINT fk_module_config_project
FOREIGN KEY (project_id) REFERENCES proj_project(id)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE proj_module_config
ADD CONSTRAINT fk_module_config_module
FOREIGN KEY (module_id) REFERENCES sys_module(id)
ON DELETE RESTRICT ON UPDATE CASCADE;

-- eval_workload_detail 表的外键约束
ALTER TABLE eval_workload_detail
ADD CONSTRAINT fk_workload_detail_evaluation
FOREIGN KEY (evaluation_id) REFERENCES eval_evaluation(id)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE eval_workload_detail
ADD CONSTRAINT fk_workload_detail_module
FOREIGN KEY (module_id) REFERENCES sys_module(id)
ON DELETE RESTRICT ON UPDATE CASCADE;

-- rpt_report 表的外键约束
ALTER TABLE rpt_report
ADD CONSTRAINT fk_report_project
FOREIGN KEY (project_id) REFERENCES proj_project(id)
ON DELETE CASCADE ON UPDATE CASCADE;

-- rpt_export_log 表的外键约束
ALTER TABLE rpt_export_log
ADD CONSTRAINT fk_export_project
FOREIGN KEY (project_id) REFERENCES proj_project(id)
ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE rpt_export_log
ADD CONSTRAINT fk_export_user
FOREIGN KEY (user_id) REFERENCES sys_user(id)
ON DELETE RESTRICT ON UPDATE CASCADE;


-- ============================================
-- 第七部分：初始化数据
-- ============================================

-- -------------------------------------------
-- 1. 用户表初始数据
-- 密码说明：admin123 的 BCrypt 加密结果
-- -------------------------------------------
INSERT INTO sys_user (username, password, user_name, role, status, create_by, remark) VALUES
('admin', '$2a$10$tH4jO9AkRgCPVzJZKEYxuu8wx6U2bzygzAQT/DeqUec/4x113sw/q', '系统管理员', 'ADMIN', 1, 'system', '系统管理员账号'),
('user01', '$2a$10$tH4jO9AkRgCPVzJZKEYxuu8wx6U2bzygzAQT/DeqUec/4x113sw/q', '张三', 'USER', 1, 'admin', '评估用户'),
('user02', '$2a$10$tH4jO9AkRgCPVzJZKEYxuu8wx6U2bzygzAQT/DeqUec/4x113sw/q', '李四', 'USER', 1, 'admin', '评估用户');

-- -------------------------------------------
-- 2. 角色表初始数据
-- -------------------------------------------
INSERT INTO sys_role (role_code, role_name, role_sort, description, status, create_by) VALUES
('SUPER_ADMIN', '超级管理员', 1, '拥有系统所有权限', 1, 'system'),
('ADMIN', '系统管理员', 2, '系统管理员，拥有系统配置和用户管理权限', 1, 'system'),
('USER', '普通用户', 3, '普通用户，拥有评估相关权限', 1, 'system');

-- -------------------------------------------
-- 3. 权限表初始数据
-- -------------------------------------------
INSERT INTO sys_permission (perm_code, perm_name, perm_type, perm_sort, path, parent_id, status, create_by) VALUES
-- 一级菜单
('system', '系统管理', 'menu', 1, '/system', 0, 1, 'system'),
('project', '项目管理', 'menu', 2, '/project', 0, 1, 'system'),
('evaluation', '工作量评估', 'menu', 3, '/evaluation', 0, 1, 'system'),
('statistics', '统计分析', 'menu', 4, '/statistics', 0, 1, 'system'),
('report', '报告输出', 'menu', 5, '/report', 0, 1, 'system'),

-- 系统管理子菜单
('system:types', '系统管理', 'menu', 1, '/system/types', 1, 1, 'system'),
('system:module', '模块库管理', 'menu', 2, '/system/module', 1, 1, 'system'),
('system:ladder', '阶梯配置', 'menu', 3, '/system/ladder', 1, 1, 'system'),
('system:config', '报表系数配置', 'menu', 4, '/system/config', 1, 1, 'system'),
('system:user', '用户管理', 'menu', 5, '/system/user', 1, 1, 'system'),

-- 系统管理按钮
('system:type:list', '查询', 'button', 1, NULL, 2, 1, 'system'),
('system:type:add', '新增', 'button', 2, NULL, 2, 1, 'system'),
('system:type:edit', '编辑', 'button', 3, NULL, 2, 1, 'system'),
('system:type:delete', '删除', 'button', 4, NULL, 2, 1, 'system'),

-- 模块库管理按钮
('system:module:list', '查询', 'button', 1, NULL, 3, 1, 'system'),
('system:module:add', '新增', 'button', 2, NULL, 3, 1, 'system'),
('system:module:edit', '编辑', 'button', 3, NULL, 3, 1, 'system'),
('system:module:delete', '删除', 'button', 4, NULL, 3, 1, 'system'),

-- 阶梯配置按钮
('system:ladder:list', '查询', 'button', 1, NULL, 4, 1, 'system'),
('system:ladder:add', '新增', 'button', 2, NULL, 4, 1, 'system'),
('system:ladder:edit', '编辑', 'button', 3, NULL, 4, 1, 'system'),
('system:ladder:delete', '删除', 'button', 4, NULL, 4, 1, 'system'),

-- 报表系数配置按钮
('system:config:list', '查询', 'button', 1, NULL, 5, 1, 'system'),
('system:config:edit', '编辑', 'button', 2, NULL, 5, 1, 'system'),

-- 用户管理按钮
('system:user:list', '查询', 'button', 1, NULL, 6, 1, 'system'),
('system:user:add', '新增', 'button', 2, NULL, 6, 1, 'system'),
('system:user:edit', '编辑', 'button', 3, NULL, 6, 1, 'system'),
('system:user:delete', '删除', 'button', 4, NULL, 6, 1, 'system'),
('system:user:resetPwd', '重置密码', 'button', 5, NULL, 6, 1, 'system'),

-- 项目管理按钮
('project:list', '查询', 'button', 1, NULL, 7, 1, 'system'),
('project:add', '新增', 'button', 2, NULL, 7, 1, 'system'),
('project:edit', '编辑', 'button', 3, NULL, 7, 1, 'system'),
('project:delete', '删除', 'button', 4, NULL, 7, 1, 'system'),
('project:copy', '复制', 'button', 5, NULL, 7, 1, 'system'),
('project:archive', '归档', 'button', 6, NULL, 7, 1, 'system'),

-- 评估按钮
('evaluation:view', '查看', 'button', 1, NULL, 8, 1, 'system'),
('evaluation:edit', '编辑', 'button', 2, NULL, 8, 1, 'system'),
('evaluation:calculate', '计算', 'button', 3, NULL, 8, 1, 'system'),
('evaluation:save', '保存', 'button', 4, NULL, 8, 1, 'system'),

-- 报告按钮
('report:export', '导出', 'button', 1, NULL, 9, 1, 'system');

-- -------------------------------------------
-- 4. 用户角色关联表初始数据
-- -------------------------------------------
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin -> 超级管理员
(2, 3),  -- user01 -> 普通用户
(3, 3);  -- user02 -> 普通用户

-- -------------------------------------------
-- 5. 角色权限关联表初始数据
-- -------------------------------------------
-- 超级管理员拥有所有权限
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 1, id FROM sys_permission;

-- 系统管理员拥有系统管理、用户管理权限
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 2, id FROM sys_permission WHERE perm_code LIKE 'system%' OR perm_code LIKE 'project%' OR perm_code LIKE 'evaluation%' OR perm_code LIKE 'report%';

-- 普通用户拥有项目管理、评估、报告权限
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 3, id FROM sys_permission WHERE perm_code NOT LIKE 'system:user%';

-- -------------------------------------------
-- 6. 系统类型表初始数据
-- -------------------------------------------
INSERT INTO sys_system_type (system_name, system_category, description, status, create_by) VALUES
-- 源系统
('SAP ECC', 1, 'SAP ERP ECC系统', 1, 'system'),
('Oracle EBS', 1, 'Oracle电子商务套件', 1, 'system'),
('金蝶K3 WISE', 1, '金蝶K3 WISE系统', 1, 'system'),
('用友U8', 1, '用友U8系统', 1, 'system'),
('用友NC', 1, '用友NC系统', 1, 'system'),
('鼎捷T100', 1, '鼎捷T100系统', 1, 'system'),
('浪潮GS', 1, '浪潮GS系统', 1, 'system'),
-- 目标系统
('SAP S/4HANA', 2, 'SAP S/4HANA系统', 1, 'system'),
('SAP Business One', 2, 'SAP Business One系统', 1, 'system'),
('Oracle Cloud ERP', 2, 'Oracle云ERP系统', 1, 'system'),
('金蝶云星空', 2, '金蝶云星空系统', 1, 'system'),
('用友U9 Cloud', 2, '用友U9 Cloud系统', 1, 'system'),
('用友BIP', 2, '用友BIP系统', 1, 'system'),
('鼎捷T100 Cloud', 2, '鼎捷T100 Cloud系统', 1, 'system');

-- -------------------------------------------
-- 7. 模块表初始数据
-- -------------------------------------------
INSERT INTO sys_module (module_name, system_id, category, base_workload, default_weight, description, status, create_by) VALUES
-- SAP ECC 系统模块
-- 财务模块
('总账管理', 1, '财务模块', 15.00, 1.20, '总账核算与管理，包括凭证管理、账簿查询等', 1, 'system'),
('应收管理', 1, '财务模块', 12.00, 1.10, '应收账款管理，包括客户信用、收款处理等', 1, 'system'),
('应付管理', 1, '财务模块', 12.00, 1.10, '应付账款管理，包括供应商信用、付款处理等', 1, 'system'),
('固定资产', 1, '财务模块', 10.00, 1.00, '固定资产管理，包括资产卡片、折旧计提等', 1, 'system'),
('成本管理', 1, '财务模块', 15.00, 1.30, '成本核算与管理，包括成本对象、费用分配等', 1, 'system'),
('资金管理', 1, '财务模块', 12.00, 1.15, '资金管理，包括银行对账、票据管理等', 1, 'system'),
-- 供应链模块
('采购管理', 1, '供应链模块', 20.00, 1.40, '采购业务管理，包括请购、订单、收货等', 1, 'system'),
('库存管理', 1, '供应链模块', 18.00, 1.30, '库存仓储管理，包括入库、出库、盘点等', 1, 'system'),
('销售管理', 1, '供应链模块', 20.00, 1.40, '销售业务管理，包括报价、订单、发货等', 1, 'system'),
('物料需求计划', 1, '供应链模块', 25.00, 1.60, 'MRP运算与计划管理', 1, 'system'),
-- 生产管理模块
('生产计划', 1, '生产模块', 25.00, 1.80, '生产计划管理，包括主生产计划、粗能力计划等', 1, 'system'),
('生产执行', 1, '生产模块', 22.00, 1.60, '生产订单执行管理', 1, 'system'),
('车间管理', 1, '生产模块', 20.00, 1.50, '车间作业管理，包括工序派工、报工等', 1, 'system'),
('质量管理', 1, '生产模块', 15.00, 1.20, '质量检验与管理', 1, 'system'),
-- HR模块
('人事管理', 1, 'HR模块', 15.00, 1.10, '人事档案管理', 1, 'system'),
('薪酬管理', 1, 'HR模块', 18.00, 1.30, '薪资考勤管理', 1, 'system'),
('招聘管理', 1, 'HR模块', 12.00, 1.00, '招聘流程管理', 1, 'system'),
('培训管理', 1, 'HR模块', 10.00, 0.90, '培训计划与执行管理', 1, 'system');

-- Oracle EBS 系统模块
INSERT INTO sys_module (module_name, system_id, category, base_workload, default_weight, description, status, create_by) VALUES
-- 财务模块
('总账', 2, '财务模块', 18.00, 1.30, 'Oracle总账管理', 1, 'system'),
('应收', 2, '财务模块', 14.00, 1.15, 'Oracle应收管理', 1, 'system'),
('应付', 2, '财务模块', 14.00, 1.15, 'Oracle应付管理', 1, 'system'),
('资产', 2, '财务模块', 12.00, 1.05, 'Oracle资产模块', 1, 'system'),
-- 供应链模块
('采购', 2, '供应链模块', 22.00, 1.45, 'Oracle采购管理', 1, 'system'),
('库存', 2, '供应链模块', 20.00, 1.35, 'Oracle库存管理', 1, 'system'),
('销售', 2, '供应链模块', 22.00, 1.45, 'Oracle销售管理', 1, 'system'),
-- 生产模块
('离散制造', 2, '生产模块', 28.00, 1.90, 'Oracle离散制造', 1, 'system'),
('流程制造', 2, '生产模块', 30.00, 2.00, 'Oracle流程制造', 1, 'system');

-- 用友U8 系统模块
INSERT INTO sys_module (module_name, system_id, category, base_workload, default_weight, description, status, create_by) VALUES
-- 财务模块
('总账', 4, '财务模块', 10.00, 1.00, '用友U8总账', 1, 'system'),
('UFO报表', 4, '财务模块', 8.00, 0.85, '用友U8报表', 1, 'system'),
('应收应付', 4, '财务模块', 12.00, 1.10, '用友U8应收应付', 1, 'system'),
('固定资产', 4, '财务模块', 8.00, 0.85, '用友U8固定资产', 1, 'system'),
('工资管理', 4, '财务模块', 10.00, 1.00, '用友U8工资管理', 1, 'system'),
-- 供应链模块
('采购管理', 4, '供应链模块', 15.00, 1.20, '用友U8采购管理', 1, 'system'),
('销售管理', 4, '供应链模块', 15.00, 1.20, '用友U8销售管理', 1, 'system'),
('库存管理', 4, '供应链模块', 12.00, 1.05, '用友U8库存管理', 1, 'system'),
('存货核算', 4, '供应链模块', 10.00, 0.95, '用友U8存货核算', 1, 'system'),
-- 生产模块
('物料清单', 4, '生产模块', 18.00, 1.35, '用友U8物料清单', 1, 'system'),
('主生产计划', 4, '生产模块', 20.00, 1.50, '用友U8主生产计划', 1, 'system'),
('需求规划', 4, '生产模块', 18.00, 1.35, '用友U8需求规划', 1, 'system'),
('车间管理', 4, '生产模块', 15.00, 1.20, '用友U8车间管理', 1, 'system'),
('工序管理', 4, '生产模块', 12.00, 1.05, '用友U8工序管理', 1, 'system');

-- -------------------------------------------
-- 8. 数据量阶梯表初始数据
-- -------------------------------------------
INSERT INTO sys_data_volume_ladder (ladder_name, min_volume, max_volume, weight, sort_order, create_by) VALUES
('记录级', 0, 1, 0.60, 1, 'system'),
('小量级', 1, 10, 0.80, 2, 'system'),
('中等量级', 10, 100, 1.00, 3, 'system'),
('大量级', 100, 1000, 1.50, 4, 'system'),
('超大量级', 1000, NULL, 2.00, 5, 'system');

-- -------------------------------------------
-- 9. 用户数阶梯表初始数据
-- -------------------------------------------
INSERT INTO sys_user_count_ladder (ladder_name, min_count, max_count, weight, sort_order, create_by) VALUES
('小规模', 0, 50, 0.90, 1, 'system'),
('中规模', 50, 200, 1.00, 2, 'system'),
('大规模', 200, 500, 1.30, 3, 'system'),
('大规模企业', 500, 1000, 1.50, 4, 'system'),
('超大规模', 1000, NULL, 2.00, 5, 'system');

-- -------------------------------------------
-- 10. 报表配置表初始数据
-- -------------------------------------------
INSERT INTO sys_report_config (config_key, config_value, config_name, description, status, create_by) VALUES
('report_workload_per_unit', '0.50', '报表迁移单位工作量', '每个报表迁移所需的工作量（人天/个）', 1, 'system'),
('custom_dev_min_workload', '5.00', '客开最小评估人天', '客开工作量评估的最小值（人天）', 1, 'system'),
('workload_precision', '2', '工作量计算精度', '工作量计算结果保留的小数位数', 1, 'system');

-- -------------------------------------------
-- 11. 全局统计种子项目（REQ-3.4.2）— 3 个 COMPLETED 项目
-- -------------------------------------------
-- 注意：dev 库可能已有项目，必须用自增 ID（不能硬编码 1,2,3）
-- 这里用 SET @next_id 保证幂等：先查 max(id)，从 max+1 开始
SET @next_pid := (SELECT COALESCE(MAX(id), 0) FROM proj_project) + 1;
SET @next_eid := (SELECT COALESCE(MAX(id), 0) FROM eval_evaluation) + 1;

INSERT INTO proj_project (id, project_name, customer_name, source_system_id, target_system_id, project_leader, contact, description, evaluation_date, status, user_id, create_by) VALUES
(@next_pid + 0, CONCAT('XX集团ERP迁移_全局统计种子01_', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s')), 'XX集团', 1, 11, '张三', '13800138001', '种子数据：用于全局统计仪表盘（大型项目）', '2026-06-01', 'COMPLETED', 1, 'system'),
(@next_pid + 1, CONCAT('江西国泰ERP升级_全局统计种子02_', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s')), '江西国泰', 1, 11, '李四', '13800138002', '种子数据：用于全局统计仪表盘（中型项目）', '2026-05-15', 'COMPLETED', 1, 'system'),
(@next_pid + 2, CONCAT('华东制造ERP替换_全局统计种子03_', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s')), '华东制造', 4, 11, '王五', '13800138003', '种子数据：用于全局统计仪表盘（小型项目）', '2026-04-20', 'COMPLETED', 1, 'system');

-- 评估记录（core_workload / report_workload / total_workload 三个值必填，供聚合 SQL 用）
INSERT INTO eval_evaluation (id, project_id, table_count, data_volume, data_volume_ladder_id, user_count, user_count_ladder_id, report_count, has_custom_dev, custom_dev_count, custom_dev_workload, core_workload, report_workload, total_workload, evaluation_status, evaluation_time) VALUES
(@next_eid + 0, @next_pid + 0, 200, 500.00, 4, 800, 4, 60, 1, 5, 30.00, 220.50, 30.00, 280.50, 'COMPLETED', '2026-06-01 10:00:00'),
(@next_eid + 1, @next_pid + 1, 120, 200.00, 3, 300, 3, 35, 0, 0, 0.00, 145.20, 17.50, 162.70, 'COMPLETED', '2026-05-15 10:00:00'),
(@next_eid + 2, @next_pid + 2, 60, 30.00, 3, 100, 2, 20, 0, 0, 0.00, 78.00, 10.00, 88.00, 'COMPLETED', '2026-04-20 10:00:00');

-- 项目模块配置（每个项目挑 3-4 个模块，覆盖不同 category，确保按模块聚合有数据）
-- dev 库 sys_module 仅有 id=1 财务会计（system_id=13），用兜底映射：
-- 若指定模块名不存在，则用 id=1（财务会计）；weight 字段也兜底
INSERT INTO proj_module_config (project_id, module_id, weight) VALUES
-- 项目 @next_pid+0：大项目，6 个 config（全用 id=1，模拟"同一模块被选多次"）
(@next_pid + 0, COALESCE((SELECT id FROM sys_module WHERE module_name='总账管理' AND system_id=1), 1), 1.20),
(@next_pid + 0, COALESCE((SELECT id FROM sys_module WHERE module_name='采购管理' AND system_id=1), 1), 1.40),
(@next_pid + 0, COALESCE((SELECT id FROM sys_module WHERE module_name='销售管理' AND system_id=1), 1), 1.40),
(@next_pid + 0, COALESCE((SELECT id FROM sys_module WHERE module_name='生产计划' AND system_id=1), 1), 1.80),
(@next_pid + 0, COALESCE((SELECT id FROM sys_module WHERE module_name='人事管理' AND system_id=1), 1), 1.10),
(@next_pid + 0, COALESCE((SELECT id FROM sys_module WHERE module_name='应收管理' AND system_id=1), 1), 1.10),
-- 项目 @next_pid+1：中项目，4 个 config
(@next_pid + 1, COALESCE((SELECT id FROM sys_module WHERE module_name='总账管理' AND system_id=1), 1), 1.20),
(@next_pid + 1, COALESCE((SELECT id FROM sys_module WHERE module_name='采购管理' AND system_id=1), 1), 1.40),
(@next_pid + 1, COALESCE((SELECT id FROM sys_module WHERE module_name='销售管理' AND system_id=1), 1), 1.40),
(@next_pid + 1, COALESCE((SELECT id FROM sys_module WHERE module_name='库存管理' AND system_id=1), 1), 1.30),
-- 项目 @next_pid+2：小项目，3 个 config
(@next_pid + 2, COALESCE((SELECT id FROM sys_module WHERE module_name='总账管理' AND system_id=1), 1), 1.20),
(@next_pid + 2, COALESCE((SELECT id FROM sys_module WHERE module_name='应收管理' AND system_id=1), 1), 1.10),
(@next_pid + 2, COALESCE((SELECT id FROM sys_module WHERE module_name='应付管理' AND system_id=1), 1), 1.10);


-- ============================================
-- 第八部分：视图创建（可选，便于查询）
-- ============================================

-- 项目完整信息视图
CREATE OR REPLACE VIEW v_project_full_info AS
SELECT
    p.id,
    p.project_name,
    p.customer_name,
    p.evaluation_date,
    p.status AS project_status,
    p.project_leader,
    p.contact,
    p.description,
    p.create_time,
    ss.system_name AS source_system_name,
    ts.system_name AS target_system_name,
    u.user_name AS creator_name,
    e.total_workload,
    e.evaluation_status,
    COUNT(DISTINCT mc.id) AS module_count
FROM proj_project p
LEFT JOIN sys_system_type ss ON p.source_system_id = ss.id
LEFT JOIN sys_system_type ts ON p.target_system_id = ts.id
LEFT JOIN sys_user u ON p.user_id = u.id
LEFT JOIN eval_evaluation e ON p.id = e.project_id
LEFT JOIN proj_module_config mc ON p.id = mc.project_id
GROUP BY p.id;

-- 评估结果汇总视图
CREATE OR REPLACE VIEW v_evaluation_summary AS
SELECT
    e.id,
    e.project_id,
    p.project_name,
    p.customer_name,
    e.total_workload,
    e.core_workload,
    e.report_workload,
    e.custom_dev_workload,
    e.table_count,
    e.data_volume,
    e.user_count,
    e.report_count,
    e.has_custom_dev,
    e.evaluation_status,
    e.evaluation_time,
    dvl.ladder_name AS data_volume_ladder,
    ucl.ladder_name AS user_count_ladder
FROM eval_evaluation e
LEFT JOIN proj_project p ON e.project_id = p.id
LEFT JOIN sys_data_volume_ladder dvl ON e.data_volume_ladder_id = dvl.id
LEFT JOIN sys_user_count_ladder ucl ON e.user_count_ladder_id = ucl.id;


-- ============================================
-- 第九部分：存储过程
-- ============================================

-- 存储过程：根据数据量获取匹配的阶梯
DELIMITER //
CREATE PROCEDURE sp_match_data_volume_ladder(
    IN p_data_volume DECIMAL(15,2),
    OUT p_ladder_id BIGINT,
    OUT p_ladder_name VARCHAR(50),
    OUT p_weight DECIMAL(5,2)
)
BEGIN
    SELECT id, ladder_name, weight INTO p_ladder_id, p_ladder_name, p_weight
    FROM sys_data_volume_ladder
    WHERE min_volume <= p_data_volume
      AND (max_volume IS NULL OR max_volume > p_data_volume)
    ORDER BY sort_order DESC
    LIMIT 1;

    -- 如果没有匹配，返回默认值
    IF p_ladder_id IS NULL THEN
        SELECT id, ladder_name, weight INTO p_ladder_id, p_ladder_name, p_weight
        FROM sys_data_volume_ladder
        WHERE sort_order = (SELECT MAX(sort_order) FROM sys_data_volume_ladder)
        LIMIT 1;
    END IF;
END //
DELIMITER ;

-- 存储过程：根据用户数获取匹配的阶梯
DELIMITER //
CREATE PROCEDURE sp_match_user_count_ladder(
    IN p_user_count INT,
    OUT p_ladder_id BIGINT,
    OUT p_ladder_name VARCHAR(50),
    OUT p_weight DECIMAL(5,2)
)
BEGIN
    SELECT id, ladder_name, weight INTO p_ladder_id, p_ladder_name, p_weight
    FROM sys_user_count_ladder
    WHERE min_count <= p_user_count
      AND (max_count IS NULL OR max_count > p_user_count)
    ORDER BY sort_order DESC
    LIMIT 1;

    -- 如果没有匹配，返回默认值
    IF p_ladder_id IS NULL THEN
        SELECT id, ladder_name, weight INTO p_ladder_id, p_ladder_name, p_weight
        FROM sys_user_count_ladder
        WHERE sort_order = (SELECT MAX(sort_order) FROM sys_user_count_ladder)
        LIMIT 1;
    END IF;
END //
DELIMITER ;


-- ============================================
-- 第十部分：完成提示
-- ============================================

SELECT '========================================' AS '';
SELECT '  MigraMetric 数据库初始化完成!' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT CONCAT('  表数量: ', COUNT(*), ' 张') AS ''
FROM information_schema.tables
WHERE table_schema = 'migrametric';
SELECT '' AS '';
SELECT '  默认账号信息:' AS '';
SELECT '  --------------------------------' AS '';
SELECT '  用户名: admin' AS '';
SELECT '  密码:   admin123' AS '';
SELECT '  --------------------------------' AS '';
SELECT '' AS '';
SELECT '  用户名: user01' AS '';
SELECT '  密码:   admin123' AS '';
SELECT '  --------------------------------' AS '';
SELECT '' AS '';
