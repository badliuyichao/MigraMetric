<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目详情</span>
          <el-button @click="router.push('/project/list')">返回列表</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目名称">{{ projectInfo.projectName }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ projectInfo.customerName }}</el-descriptions-item>
        <el-descriptions-item label="源系统">
          <el-tag type="success">{{ projectInfo.sourceSystem }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="目标系统">
          <el-tag type="warning">{{ projectInfo.targetSystem }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="项目负责人">{{ projectInfo.manager }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ projectInfo.contact }}</el-descriptions-item>
        <el-descriptions-item label="评估日期">{{ projectInfo.evaluationDate }}</el-descriptions-item>
        <el-descriptions-item label="项目状态">
          <el-tag :type="getStatusType(projectInfo.status)">
            {{ getStatusText(projectInfo.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="项目描述" :span="2">
          {{ projectInfo.description || '暂无描述' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <div class="action-buttons">
        <el-button type="primary" @click="handleEvaluate">开始评估</el-button>
        <el-button type="warning" @click="handleExport">导出报告</el-button>
        <el-button @click="handleEdit">编辑项目</el-button>
      </div>
    </el-card>

    <!-- 评估概况 -->
    <el-card v-if="projectInfo.status !== 'DRAFT'" class="mt-20">
      <template #header>
        <span>评估概况</span>
      </template>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="已选模块">5 个</el-descriptions-item>
        <el-descriptions-item label="总工作量">156.38 人天</el-descriptions-item>
        <el-descriptions-item label="数据量">500 万条</el-descriptions-item>
        <el-descriptions-item label="用户数量">600 人</el-descriptions-item>
        <el-descriptions-item label="报表数量">50 个</el-descriptions-item>
        <el-descriptions-item label="评估状态">
          <el-tag type="success">已完成</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const projectInfo = reactive({
  id: 1,
  projectName: 'XX公司ERP迁移项目',
  customerName: 'XX公司',
  sourceSystem: 'SAP',
  targetSystem: '用友',
  manager: '张三',
  contact: '13800138000',
  evaluationDate: '2026-03-19',
  status: 'IN_PROGRESS',
  description: '这是一个测试项目'
})

function getStatusType(status: string) {
  const map: Record<string, string> = {
    DRAFT: 'info',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    ARCHIVED: 'warning'
  }
  return map[status] || 'info'
}

function getStatusText(status: string) {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    ARCHIVED: '已归档'
  }
  return map[status] || status
}

function handleEvaluate() {
  // TODO: 跳转到评估页面
}

function handleExport() {
  // TODO: 导出报告
}

function handleEdit() {
  // TODO: 编辑项目
}
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.action-buttons {
  display: flex;
  gap: 10px;
}
</style>
