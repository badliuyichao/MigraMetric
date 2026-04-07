export const testProjects = {
  draft: {
    id: 1,
    projectName: '测试草稿项目',
    customerName: '测试客户A',
    sourceSystemName: 'SAP',
    targetSystemName: '用友NC',
    projectLeader: '张三',
    status: 'DRAFT',
    statusText: '草稿',
    createTime: '2026-04-01 10:00:00',
  },
  inProgress: {
    id: 2,
    projectName: '测试进行中项目',
    customerName: '测试客户B',
    sourceSystemName: 'Oracle',
    targetSystemName: '金蝶K3',
    projectLeader: '李四',
    status: 'IN_PROGRESS',
    statusText: '进行中',
    createTime: '2026-04-02 14:00:00',
  },
  completed: {
    id: 3,
    projectName: '测试已完成项目',
    customerName: '测试客户C',
    sourceSystemName: '金蝶K3',
    targetSystemName: '用友U8',
    projectLeader: '王五',
    status: 'COMPLETED',
    statusText: '已完成',
    createTime: '2026-03-15 09:00:00',
  },
}

export function createTestProject(overrides = {}) {
  return {
    projectName: `测试项目_${Date.now()}`,
    customerName: '测试客户',
    sourceSystemName: 'SAP',
    targetSystemName: '用友NC',
    projectLeader: '测试负责人',
    ...overrides,
  }
}