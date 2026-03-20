-- ============================================
-- MigraMetric 数据库初始化脚本
-- 数据库版本: V1.0
-- 创建日期: 2026-03-19
-- 数据库名称: migrametric_dev
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS migrametric_dev
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE migrametric_dev;

-- ============================================
-- 第一部分：用户域表 (sys_user, sys_role, sys_permission, ...)
-- ============================================

-- 1.1 sys_user - 用户表
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

-- 1.2 sys_role - 角色表
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

-- 1.3 sys_permission - 权限表
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
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 1.4 sys_user_role - 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id             BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 1.5 sys_role_permission - 角色权限关联表
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    role_id             BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    perm_id             BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (role_id, perm_id),
    KEY idx_perm_id (perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 1.6 sys_login_log - 登录日志表
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

-- ============================================
-- 第二部分：配置域表 (sys_system_type, sys_module, ...)
-- ============================================

-- 2.1 sys_system_type - 系统类型表
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
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_name (system_name),
    KEY idx_system_category (system_category),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统类型表';

-- 2.2 sys_module - 模块表
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

-- 2.3 sys_data_volume_ladder - 数据量阶梯表
DROP TABLE IF EXISTS sys_data_volume_ladder;
CREATE TABLE sys_data_volume_ladder (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '阶梯ID',
    ladder_name         VARCHAR(50)     NOT NULL COMMENT '阶梯名称',
    min_volume          DECIMAL(15,2)   NOT NULL DEFAULT 0 COMMENT '数据量下限（万条）',
    max_volume          DECIMAL(15,2)             DEFAULT NULL COMMENT '数据量上限（万条）',
    weight              DECIMAL(5,2)    NOT NULL DEFAULT 1.00 COMMENT '对应工作量系数',
    sort_order          INT             NOT NULL DEFAULT 0 COMMENT '排序顺序',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据量阶梯表';

-- 2.4 sys_user_count_ladder - 用户数阶梯表
DROP TABLE IF EXISTS sys_user_count_ladder;
CREATE TABLE sys_user_count_ladder (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '阶梯ID',
    ladder_name         VARCHAR(50)     NOT NULL COMMENT '阶梯名称',
    min_count           INT             NOT NULL DEFAULT 0 COMMENT '用户数下限',
    max_count           INT                      DEFAULT NULL COMMENT '用户数上限',
    weight              DECIMAL(5,2)    NOT NULL DEFAULT 1.00 COMMENT '对应工作量系数',
    sort_order          INT             NOT NULL DEFAULT 0 COMMENT '排序顺序',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    update_by           VARCHAR(64)              DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户数阶梯表';

-- 2.5 sys_report_config - 报表配置表
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
-- 第三部分：项目域表 (proj_project)
-- ============================================

-- 3.1 proj_project - 项目表
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
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '项目状态',
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
-- 第四部分：评估域表 (eval_evaluation, proj_module_config, eval_workload_detail)
-- ============================================

-- 4.1 eval_evaluation - 评估表
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
    evaluation_status    VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '评估状态',
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

-- 4.2 proj_module_config - 项目模块配置表
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

-- 4.3 eval_workload_detail - 工作量明细表
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
-- 第五部分：报告域表 (rpt_report, rpt_export_log)
-- ============================================

-- 5.1 rpt_report - 报告表
DROP TABLE IF EXISTS rpt_report;
CREATE TABLE rpt_report (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '报告ID',
    project_id          BIGINT UNSIGNED NOT NULL COMMENT '项目ID',
    report_title        VARCHAR(500)    NOT NULL COMMENT '报告标题',
    report_content      LONGTEXT                DEFAULT NULL COMMENT '报告内容（JSON格式）',
    total_workload      DECIMAL(10,2)            DEFAULT 0 COMMENT '总工作量',
    status              VARCHAR(20)     NOT NULL DEFAULT 'GENERATED' COMMENT '报告状态',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           VARCHAR(64)              DEFAULT NULL COMMENT '创建者',
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_id (project_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告表';

-- 5.2 rpt_export_log - 报告导出记录表
DROP TABLE IF EXISTS rpt_export_log;
CREATE TABLE rpt_export_log (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    project_id          BIGINT UNSIGNED NOT NULL COMMENT '项目ID',
    export_format       VARCHAR(20)     NOT NULL COMMENT '导出格式',
    export_path         VARCHAR(500)             DEFAULT NULL COMMENT '文件存储路径',
    file_name           VARCHAR(200)             DEFAULT NULL COMMENT '文件名称',
    file_size           BIGINT                  DEFAULT NULL COMMENT '文件大小（字节）',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '导出用户ID',
    status              VARCHAR(20)     NOT NULL DEFAULT 'SUCCESS' COMMENT '导出状态',
    error_msg           VARCHAR(500)             DEFAULT NULL COMMENT '错误信息',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '导出时间',
    PRIMARY KEY (id),
    KEY idx_project_id (project_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time),
    KEY idx_export_format (export_format)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告导出记录表';

-- ============================================
-- 第六部分：添加外键约束
-- ============================================

-- sys_module 表的外键约束
ALTER TABLE sys_module
    ADD CONSTRAINT fk_module_system
    FOREIGN KEY (system_id) REFERENCES sys_system_type(id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- proj_project 表的外键约束 - 源系统
ALTER TABLE proj_project
    ADD CONSTRAINT fk_project_source
    FOREIGN KEY (source_system_id) REFERENCES sys_system_type(id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- proj_project 表的外键约束 - 目标系统
ALTER TABLE proj_project
    ADD CONSTRAINT fk_project_target
    FOREIGN KEY (target_system_id) REFERENCES sys_system_type(id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- proj_project 表的外键约束 - 用户
ALTER TABLE proj_project
    ADD CONSTRAINT fk_project_user
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- eval_evaluation 表的外键约束 - 项目
ALTER TABLE eval_evaluation
    ADD CONSTRAINT fk_evaluation_project
    FOREIGN KEY (project_id) REFERENCES proj_project(id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- eval_evaluation 表的外键约束 - 数据量阶梯
ALTER TABLE eval_evaluation
    ADD CONSTRAINT fk_evaluation_data_volume_ladder
    FOREIGN KEY (data_volume_ladder_id) REFERENCES sys_data_volume_ladder(id)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- eval_evaluation 表的外键约束 - 用户数阶梯
ALTER TABLE eval_evaluation
    ADD CONSTRAINT fk_evaluation_user_count_ladder
    FOREIGN KEY (user_count_ladder_id) REFERENCES sys_user_count_ladder(id)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- proj_module_config 表的外键约束 - 项目
ALTER TABLE proj_module_config
    ADD CONSTRAINT fk_module_config_project
    FOREIGN KEY (project_id) REFERENCES proj_project(id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- proj_module_config 表的外键约束 - 模块
ALTER TABLE proj_module_config
    ADD CONSTRAINT fk_module_config_module
    FOREIGN KEY (module_id) REFERENCES sys_module(id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- eval_workload_detail 表的外键约束 - 评估
ALTER TABLE eval_workload_detail
    ADD CONSTRAINT fk_workload_evaluation
    FOREIGN KEY (evaluation_id) REFERENCES eval_evaluation(id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- eval_workload_detail 表的外键约束 - 模块
ALTER TABLE eval_workload_detail
    ADD CONSTRAINT fk_workload_module
    FOREIGN KEY (module_id) REFERENCES sys_module(id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- rpt_report 表的外键约束 - 项目
ALTER TABLE rpt_report
    ADD CONSTRAINT fk_report_project
    FOREIGN KEY (project_id) REFERENCES proj_project(id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- rpt_export_log 表的外键约束 - 项目
ALTER TABLE rpt_export_log
    ADD CONSTRAINT fk_export_project
    FOREIGN KEY (project_id) REFERENCES proj_project(id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- rpt_export_log 表的外键约束 - 用户
ALTER TABLE rpt_export_log
    ADD CONSTRAINT fk_export_user
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- sys_user_role 表的外键约束
ALTER TABLE sys_user_role
    ADD CONSTRAINT fk_user_role_user
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE sys_user_role
    ADD CONSTRAINT fk_user_role_role
    FOREIGN KEY (role_id) REFERENCES sys_role(id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- sys_role_permission 表的外键约束
ALTER TABLE sys_role_permission
    ADD CONSTRAINT fk_role_permission_role
    FOREIGN KEY (role_id) REFERENCES sys_role(id)
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE sys_role_permission
    ADD CONSTRAINT fk_role_permission_perm
    FOREIGN KEY (perm_id) REFERENCES sys_permission(id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 输出完成信息
SELECT '========================================' AS '';
SELECT '数据库 migrametric_dev 初始化完成!' AS 'Message';
SELECT '========================================' AS '';
SELECT COUNT(*) AS '表数量' FROM information_schema.tables WHERE table_schema = 'migrametric_dev';
