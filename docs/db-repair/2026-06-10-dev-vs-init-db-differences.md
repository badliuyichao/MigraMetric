# dev 数据库修复建议 · init-db.sql vs dev 库差异

- **文档版本**：V1.0
- **核查日期**：2026-06-10
- **核查人**：Claude
- **业务决策**：**以 dev 库为准**（阶梯命名更友好）— dev 库是"现场"状态，init-db.sql 是"出厂"状态，本次修复方向是**让 init-db.sql 对齐 dev 库**
- **修复范围**：仅 dev 库（migrametric_dev），不动生产

---

## 〇、问题总览

| ID | 严重性 | 类别 | 简述 | 影响范围 |
|----|--------|------|------|----------|
| ISSUE-01 | 🔴 P0 | RBAC | sys_role_permission 0 条，菜单/按钮权限校验全失效 | 整个系统 |
| ISSUE-02 | 🔴 P0 | RBAC | sys_user_role 0 条，3 个用户都没有角色关联 | 整个系统 |
| ISSUE-03 | 🟡 P1 | 主数据 | sys_module 缺 40 条（init 41 vs dev 1）| 新建项目/评估 |
| ISSUE-04 | 🟡 P1 | 主数据 | sys_system_type 缺 6 条（init 14 vs dev 8）| 系统管理 |
| ISSUE-05 | 🟠 P2 | 报表配置 | sys_report_config 缺 2 条 | 报表配置页 |
| ISSUE-06 | 🟠 P2 | 文档同步 | init-db.sql 阶梯命名与 dev 库不一致，需以 dev 为准反推 init | 文档/代码/产品 |
| ISSUE-07 | 🟢 P3 | 主数据 | sys_user 缺 user02 | 无影响 |
| ISSUE-08 | 🟢 P3 | 角色 | sys_role 缺 SUPER_ADMIN (id=1) | 当前无影响，潜在风险 |

---

## ISSUE-01 🔴 P0 · sys_role_permission 完全缺失

### 问题
- init-db.sql：3 个 INSERT（超级管理员全部权限 + 系统管理员部分权限 + 普通用户部分权限）
- dev 库：**0 条**
- 后果：菜单/按钮的 `meta.roles` 校验依赖 sys_role_permission 表的查询，**导致系统管理、阶梯配置、用户管理菜单的 RBAC 拦截失效**

### 修复建议
**修复方案 A（推荐 · 重跑 init-db.sql 第 612-621 行）**：
```sql
DELETE FROM sys_role_permission;
INSERT INTO sys_role_permission (role_id, perm_id) SELECT 1, id FROM sys_permission;
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 2, id FROM sys_permission
WHERE perm_code LIKE 'system%' OR perm_code LIKE 'project%'
   OR perm_code LIKE 'evaluation%' OR perm_code LIKE 'report%';
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT 3, id FROM sys_permission
WHERE perm_code NOT LIKE 'system:user%';
```

**修复方案 B（增量追加 · 不清空）**：
仅当 dev 库 sys_role_permission 后续被人工添加过业务数据时才使用，本场景**不适用**。

### 验收
执行后 `SELECT COUNT(*) FROM sys_role_permission;` 应 ≥ 3 个角色 × 各自权限数（按 perm 表行数 × 角色分配）。

---

## ISSUE-02 🔴 P0 · sys_user_role 完全缺失

### 问题
- init-db.sql：3 条 (1,1)/(2,3)/(3,3)
- dev 库：**0 条**
- 后果：当前不影响登录（后端 JWT 直接读 `sys_user.role` 字段，不查 user_role 表），但**未来若加"用户多角色/角色切换"功能会失灵**；且 `sys_role.id=1 (SUPER_ADMIN)` 在 dev 库不存在，关联 (1,1) 会 FK 失败

### 修复建议
**修复步骤（先补 SUPER_ADMIN 角色，再插关联）**：

```sql
-- 步骤 1: 补 SUPER_ADMIN 角色（dev 库缺，id 顺序顺延为 3，避免与现有 ADMIN(1)/USER(2) 冲突）
INSERT INTO sys_role (id, role_code, role_name, role_sort, description, status, create_by) VALUES
(3, 'SUPER_ADMIN', '超级管理员', 1, '拥有系统所有权限', 1, 'system');

-- 步骤 2: 重新映射 user_role 关联（admin 关联 SUPER_ADMIN，user01/user02 关联 USER）
-- 现有 id: admin=1, user=2
DELETE FROM sys_user_role;
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 3),  -- admin -> SUPER_ADMIN
(2, 2);  -- user -> USER
```

> ⚠️ 注：原 init-db.sql 的 (1,1) 假设 sys_role.id=1 是 SUPER_ADMIN，但 dev 库 id=1 是 ADMIN。这里需要**把 admin 关联到新插入的 SUPER_ADMIN**（id=3），或者修改 init-db.sql 让 id 重排（见 ISSUE-06）。

### 验收
执行后 `SELECT * FROM sys_user_role;` 应有 2 条记录。

---

## ISSUE-03 🟡 P1 · sys_module 严重缺失（98%）

### 问题
- init-db.sql：41 条（system_id=1 18 条 + system_id=2 9 条 + system_id=4 14 条）
- dev 库：1 条（system_id=13 财务会计）
- 后果：
  - 新建项目选 source_system=1(SAP ECC)/2(Oracle EBS)/4(金蝶K3) 时，评估步骤二的模块列表为空
  - 评估计算公式需要 module.baseWorkload 和 defaultWeight，缺数据会走不到模块聚合逻辑

### 修复建议
**修复方案（直接重跑 init-db.sql 第 645-705 行种子）**：
```sql
-- sys_module 三个 INSERT 块（system_id=1/2/4 共 41 条）
INSERT INTO sys_module (module_name, system_id, category, base_workload, default_weight, description, status, create_by) VALUES
('总账管理', 1, '财务模块', 15.00, 1.20, '总账核算与管理', 1, 'system'),
('应收管理', 1, '财务模块', 12.00, 1.10, '应收账款管理', 1, 'system'),
-- ... (完整 18 条 SAP ECC)
;
INSERT INTO sys_module (module_name, system_id, category, base_workload, default_weight, description, status, create_by) VALUES
('总账', 2, '财务模块', 18.00, 1.30, 'Oracle总账管理', 1, 'system'),
-- ... (完整 9 条 Oracle EBS)
;
INSERT INTO sys_module (module_name, system_id, category, base_workload, default_weight, description, status, create_by) VALUES
('采购管理', 4, '供应链模块', 20.00, 1.40, '金蝶K3采购管理', 1, 'system'),
-- ... (完整 14 条 金蝶K3)
;
```

### 验收
执行后 `SELECT system_id, COUNT(*) FROM sys_module GROUP BY system_id;` 应返回 4 行（id 1/2/4/13 各有若干条）。

---

## ISSUE-04 🟡 P1 · sys_system_type 缺 6 条

### 问题
- init-db.sql：14 条
- dev 库：8 条（id 不连续：1/2/4/5/9/10/11/13）
- 缺 6 条：用友NC、鼎捷T100、金蝶K3 WISE、用友U8、鼎捷T100 Cloud、用友U9 Cloud
- 后果：项目创建时下拉"源系统/目标系统"少 6 个选项

### 修复建议
**修复方案 A（推荐 · 调整 id 后增量补）**：
init-db.sql 用了硬编码 id=3/6/7/8/12/14，需保持 dev 库 id 连续，调整插入 SQL：

```sql
-- 删除现有 8 条，重新插入 14 条（按 init 顺序，id 1-14）
DELETE FROM sys_system_type;
INSERT INTO sys_system_type (id, system_name, system_category, description, status, create_by) VALUES
(1, 'SAP ECC', 1, 'SAP ERP ECC系统', 1, 'system'),
(2, 'Oracle EBS', 1, 'Oracle电子商务套件', 1, 'system'),
(3, '金蝶K3 WISE', 1, '金蝶K3 WISE系统', 1, 'system'),  -- 旧 id=3
(4, '用友U8', 1, '用友U8系统', 1, 'system'),            -- 旧 id=4
(5, '用友NC', 1, '用友NC系统', 1, 'system'),            -- 旧 id=5
(6, '鼎捷T100', 1, '鼎捷T100系统', 1, 'system'),         -- 旧 id=6
(7, '浪潮GS', 1, '浪潮GS系统', 1, 'system'),             -- 旧 id=7
(8, 'SAP S/4HANA', 2, 'SAP S/4HANA系统', 1, 'system'),   -- 旧 id=8
(9, 'SAP Business One', 2, 'SAP Business One系统', 1, 'system'),
(10, 'Oracle Cloud ERP', 2, 'Oracle云ERP系统', 1, 'system'),
(11, '金蝶云星空', 2, '金蝶云星空系统', 1, 'system'),
(12, '用友U9 Cloud', 2, '用友U9 Cloud系统', 1, 'system'), -- 旧 id=12
(13, '用友BIP', 2, '用友BIP系统', 1, 'system'),
(14, '鼎捷T100 Cloud', 2, '鼎捷T100 Cloud系统', 1, 'system');
```

**注意**：`sys_module` / `proj_project` / `eval_evaluation` 的 FK 引用 sys_system_type.id。如果已经跑了模块种子（ISSUE-03），**需要先确认模块种子的 system_id 与新插入的系统 id 对应**。

### 验收
执行后 `SELECT COUNT(*) FROM sys_system_type;` 应 = 14。

---

## ISSUE-05 🟠 P2 · sys_report_config 缺 2 条

### 问题
- init-db.sql：3 条（`report_workload_per_unit`、`custom_dev_min_workload`、`workload_precision`）
- dev 库：1 条（仅 `report_workload_per_unit=5`，注意 init 是 0.5）
- 后果：前端"报表系数配置"页只能改一个值
- **额外问题**：dev 库 `report_workload_per_unit=5` 与 init-db.sql 的 `0.5` 不一致，导致评估公式输出**差 10 倍**！这是历史遗留 bug

### 修复建议
**修复方案**：
```sql
-- 先核对值，决定保留 0.5 还是 5
SELECT id, config_key, config_value FROM sys_report_config;
-- 补缺 2 条 + 修正 report_workload_per_unit 为 0.5（init 的设计值）
INSERT INTO sys_report_config (config_key, config_value, config_name, description, status, create_by) VALUES
('custom_dev_min_workload', '5.00', '客开最小评估人天', '客开工作量评估的最小值（人天）', 1, 'system'),
('workload_precision', '2', '工作量计算精度', '工作量计算结果保留的小数位数', 1, 'system')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value), create_by='system';

-- 修正 report_workload_per_unit（如果当前值是 5 而 init 设计是 0.5）
UPDATE sys_report_config SET config_value = '0.5'
WHERE config_key = 'report_workload_per_unit';
```

**决策点**：5 vs 0.5 — dev 库当前是 5（admin 调过），但 init 设计 0.5。需要确认业务侧期望哪个值。**建议确认后修正**。

### 验收
执行后 `SELECT * FROM sys_report_config;` 应有 3 条记录。

---

## ISSUE-06 🟠 P2 · 阶梯命名规范统一（dev 库为准）

### 问题
dev 库阶梯命名和数字范围与 init-db.sql 不一致。dev 库命名更友好，决定以 dev 为准，需反推修改 init-db.sql。

| 表 | init（应改） | dev（保留） | 差异类型 |
|---|---|---|---|
| sys_data_volume_ladder | 记录级 0-1 / 小量级 1-10 / 中等量级 10-100 / 大量级 100-1000 / 超大量级 1000+ | 微型 0-10 / 小型 10-50 / 中型 50-200 / 大型 200-500 / 超大型 500+ | **name + 范围都变** |
| sys_user_count_ladder | 小规模 0-50 / 中规模 50-200 / 大规模 200-500 / 大规模企业 500-1000 / 超大规模 1000+ | 微小型 0-50 / 小型 50-200 / 中型 200-500 / 大型 500-1000 / 超大型 1000+ | **仅 name 变** |

### 修复建议
**Step 1：修改 init-db.sql（文档同步）**

把 init-db.sql 第 710-715 行（sys_data_volume_ladder）改为：
```sql
INSERT INTO sys_data_volume_ladder (ladder_name, min_volume, max_volume, weight, sort_order, create_by) VALUES
('微型', 0, 10, 0.60, 1, 'system'),
('小型', 10, 50, 0.80, 2, 'system'),
('中型', 50, 200, 1.00, 3, 'system'),
('大型', 200, 500, 1.20, 4, 'system'),
('超大型', 500, NULL, 1.50, 5, 'system');
```

把 init-db.sql 第 720-725 行（sys_user_count_ladder）改为：
```sql
INSERT INTO sys_user_count_ladder (ladder_name, min_count, max_count, weight, sort_order, create_by) VALUES
('微小型', 0, 50, 1.00, 1, 'system'),
('小型', 50, 200, 1.20, 2, 'system'),
('中型', 200, 500, 1.40, 3, 'system'),
('大型', 500, 1000, 1.60, 4, 'system'),
('超大型', 1000, NULL, 1.80, 5, 'system');
```

**Step 2：检查产品设计文档是否需要同步更新**

产品设计文档 §3.1 阶梯配置章节可能引用旧名字（记录级/小量级等），需要 grep 后更新：
```bash
grep -n "记录级\|小量级\|中等量级\|小规模\|中规模\|大规模企业" docs/architecture/产品设计文档.md
```

**Step 3：全量回归评估计算**

数据量阶梯范围变更后，**同样 100 万条数据**：
- 旧阶梯（init）：weight=1.50
- 新阶梯（dev）：weight=1.20
- 评估结果差异：20% 偏差

需要全量回归 EV-001~005 E2E 用例（已通过，期望值在 dev 库条件下成立）。

### 验收
- init-db.sql 第 710-725 行内容与 dev 库一致
- 产品设计文档 §3.1 阶梯命名已同步
- EV-001~005 E2E 全过

---

## ISSUE-07 🟢 P3 · sys_user 缺 user02

### 问题
- init-db.sql：admin / user01 / user02 共 3 个
- dev 库：admin / user 共 2 个（user 是 id=2，原 init 的 user01 已被 ISSUE-02 调整映射）

### 修复建议
**可选**，user02 当前未在 E2E fixture 引用，不补不影响功能。

若要补齐：
```sql
INSERT INTO sys_user (username, password, user_name, role, status, create_by, remark) VALUES
('user02', '$2a$10$tH4jO9AkRgCPVzJZKEYxuu8wx6U2bzygzAQT/DeqUec/4x113sw/q', '李四', 'USER', 1, 'admin', '评估用户');
INSERT INTO sys_user_role (user_id, role_id) VALUES (LAST_INSERT_ID(), 2);
```

### 验收
无

---

## ISSUE-08 🟢 P3 · sys_role 缺 SUPER_ADMIN

### 问题
- init-db.sql：SUPER_ADMIN(id=1) / ADMIN(id=2) / USER(id=3) 共 3 个
- dev 库：ADMIN(id=1) / USER(id=2) 共 2 个

### 修复建议
**已在 ISSUE-02 修复方案中包含**（步骤 1 插入 SUPER_ADMIN id=3）。

注意：原 init 的 id 编号 (1,2,3) 与 dev 现存 (1,2) 冲突。**统一以 dev 库 id 为准**：
- ADMIN 保持 id=1
- USER 保持 id=2
- SUPER_ADMIN 新增 id=3

修改 init-db.sql 第 531-534 行：
```sql
INSERT INTO sys_role (role_code, role_name, role_sort, description, status, create_by) VALUES
('ADMIN', '系统管理员', 1, '系统管理员，拥有系统配置和用户管理权限', 1, 'system'),
('USER', '普通用户', 2, '普通用户，拥有评估相关权限', 1, 'system'),
('SUPER_ADMIN', '超级管理员', 3, '拥有系统所有权限', 1, 'system');
```

同时第 604-606 行（user_role 关联）改为：
```sql
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 3),  -- admin -> SUPER_ADMIN
(2, 2),  -- user -> USER
(3, 2);  -- user02 -> USER（如果补 user02）
```

### 验收
- init-db.sql 第 531-534 行内容与 dev 库一致
- 重新跑 init-db.sql 第 612-621 行（role_permission 关联）可正常执行

---

## 三、修复执行顺序（建议）

```
┌─ 第 1 步：P0 RBAC 修复
│   ├─ ISSUE-08: 补 SUPER_ADMIN 角色
│   ├─ ISSUE-02: 补 sys_user_role 关联
│   └─ ISSUE-01: 补 sys_role_permission 关联
│
├─ 第 2 步：P1 主数据修复
│   ├─ ISSUE-04: 补 sys_system_type 6 条
│   └─ ISSUE-03: 补 sys_module 40 条
│
├─ 第 3 步：P2 报表/文档
│   ├─ ISSUE-05: 补 sys_report_config 2 条 + 修正 report_workload_per_unit
│   └─ ISSUE-06: 修改 init-db.sql + 产品设计文档
│
└─ 第 4 步：P3 可选
    └─ ISSUE-07: 补 user02
```

每步完成后建议 `mvn test` + 全量 E2E 回归。

---

## 四、待用户确认的决策点

| 决策 | 现状 | 选项 |
|---|---|---|
| **D1. report_workload_per_unit 值** | dev 库 = 5，init 设计 = 0.5 | A. 改回 0.5（与 init 对齐）<br>B. 保留 5（dev 库为准，但需更新产品文档）<br>C. 找 admin 确认 5 是有意还是手误 |
| **D2. sys_role id 编号** | init 用 (1,2,3)，dev 用 (1,2)，新 SUPER_ADMIN 该是 3 还是 1？ | A. dev 库保持 id=1=ADMIN, id=2=USER，新 SUPER_ADMIN=id=3（推荐）<br>B. 重排让 SUPER_ADMIN=id=1 |
| **D3. sys_module 修复顺序** | ISSUE-04 先于 ISSUE-03（因为 module 引用 system_type） | 严格按 4→3 顺序执行 |

---

## 五、修复完成后的 dev 库目标态

| 表 | 当前 | 目标 | 变化 |
|---|---|---|---|
| sys_user | 2 | 2 或 3 | 取决于 D7 |
| sys_role | 2 | 3 | +1 SUPER_ADMIN |
| sys_permission | 6 | 40 | +34 |
| sys_user_role | 0 | 2 或 3 | +2/+3 |
| sys_role_permission | 0 | 40+ | +40 |
| sys_system_type | 8 | 14 | +6 |
| sys_module | 1 | 42 | +41 |
| sys_data_volume_ladder | 5 | 5 | 0（保持 dev） |
| sys_user_count_ladder | 5 | 5 | 0（保持 dev） |
| sys_report_config | 1 | 3 | +2（需决策 D1） |
| proj_project | 0 | 0 | 0（按需手动创建） |
| eval_evaluation | 0 | 0 | 0 |
| proj_module_config | 0 | 0 | 0 |

---

## 六、相关文档链接

- 需求 §3.1：阶梯配置 → 见 `docs/architecture/需求说明文档.md`
- 产品 §3.1：阶梯配置 → 见 `docs/architecture/产品设计文档.md`
- 技术 §1.2：数据模型 → 见 `docs/architecture/技术架构文档.md`
- 测试 §十四：全局统计 E2E → 见 `docs/develop/测试方案及测试计划（合集）.md`
- SQL 种子位置：`docs/init-db.sql` 第 523-768 行

---

## 七、版本历史

| 版本 | 日期 | 变更 | 作者 |
|---|---|---|---|
| V1.0 | 2026-06-10 | 初始核查（8 个 ISSUE + 修复建议）| Claude |
