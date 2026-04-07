# 界面测试目录

## 目录说明

- `e2e/` - E2E自动化测试，测试完整用户流程
- `visual/` - 视觉回归测试，检测UI视觉变化
- `fixtures/` - 测试数据，如测试用户、测试项目等
- `utils/` - 测试工具函数，如认证辅助、API辅助等
- `setup/` - 测试初始化脚本

## 运行测试

```bash
# 运行单元测试
pnpm test:unit

# 运行组件交互测试
pnpm test:interaction

# 运行E2E测试
pnpm test:e2e

# 运行视觉回归测试
pnpm test:visual
```