<template>
  <div class="page-container">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <template v-else>
      <!-- 基本信息卡片 -->
      <el-card>
        <template #header>
          <div class="card-header">
            <span>项目详情</span>
            <div class="header-actions">
              <el-button @click="router.push('/project/list')">返回列表</el-button>
              <el-button type="primary" @click="handleEdit">编辑项目</el-button>
            </div>
          </div>
        </template>

        <el-descriptions :column="2" border data-testid="descriptions-project-info">
          <el-descriptions-item label="项目名称">{{ projectInfo.projectName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ projectInfo.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="源系统">
            <el-tag type="success">{{ projectInfo.sourceSystemName || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="目标系统">
            <el-tag type="warning">{{ projectInfo.targetSystemName || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="项目负责人">{{ projectInfo.projectLeader || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ projectInfo.contact || '-' }}</el-descriptions-item>
          <el-descriptions-item label="评估日期">{{ projectInfo.evaluationDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="项目状态">
            <el-tag :type="getStatusType(projectInfo.status)" data-testid="project-status">
              {{ projectInfo.statusText || '-' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建人">{{ projectInfo.createByName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ projectInfo.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="项目描述" :span="2">
            {{ projectInfo.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="action-buttons">
          <el-button
            v-if="projectInfo.status === 'DRAFT'"
            data-testid="btn-start-evaluation"
            type="primary"
            @click="handleStartEvaluation"
          >
            开始评估
          </el-button>
          <el-button
            v-else-if="projectInfo.status === 'IN_PROGRESS'"
            data-testid="btn-continue-evaluation"
            type="primary"
            @click="handleContinueEvaluation"
          >
            继续评估
          </el-button>
          <el-button
            v-if="projectInfo.hasEvaluation"
            data-testid="btn-view-report"
            type="success"
            @click="handleViewReport"
          >
            查看报告
          </el-button>
          <el-button @click="handleExport">导出报告</el-button>
          <el-button v-if="projectInfo.status === 'DRAFT'" data-testid="btn-copy-project" @click="handleCopy">复制项目</el-button>
          <el-button v-if="projectInfo.status === 'COMPLETED'" type="warning" data-testid="btn-archive-project" @click="handleArchive">归档项目</el-button>
          <el-button v-if="projectInfo.status === 'DRAFT'" type="danger" data-testid="btn-delete-project" @click="handleDelete">删除项目</el-button>
        </div>
      </el-card>

      <!-- 评估概况卡片 -->
      <el-card v-if="projectInfo.status !== 'DRAFT'" class="mt-20">
        <template #header>
          <div class="card-header">
            <span>评估概况</span>
            <el-tag v-if="projectInfo.evaluationStatus" :type="getEvaluationStatusType(projectInfo.evaluationStatus)">
              {{ projectInfo.evaluationStatusText }}
            </el-tag>
          </div>
        </template>

        <el-descriptions :column="3" border>
          <el-descriptions-item label="已选模块">
            {{ projectInfo.selectedModuleCount || 0 }} 个
          </el-descriptions-item>
          <el-descriptions-item label="总工作量">
            <span class="workload-value">
              {{ projectInfo.totalWorkload != null ? projectInfo.totalWorkload.toFixed(2) : '-' }} 人天
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="数据库表数量">
            {{ projectInfo.tableCount != null ? projectInfo.tableCount : '-' }} 个
          </el-descriptions-item>
          <el-descriptions-item label="数据量">
            {{ projectInfo.dataVolume != null ? projectInfo.dataVolume : '-' }} 万条
          </el-descriptions-item>
          <el-descriptions-item label="用户数量">
            {{ projectInfo.userCount != null ? projectInfo.userCount : '-' }} 人
          </el-descriptions-item>
          <el-descriptions-item label="报表数量">
            {{ projectInfo.reportCount != null ? projectInfo.reportCount : '-' }} 个
          </el-descriptions-item>
          <el-descriptions-item label="核心迁移工作量">
            {{ projectInfo.coreWorkload != null ? projectInfo.coreWorkload.toFixed(2) : '-' }} 人天
          </el-descriptions-item>
          <el-descriptions-item label="报表工作量">
            {{ projectInfo.reportWorkload != null ? projectInfo.reportWorkload.toFixed(2) : '-' }} 人天
          </el-descriptions-item>
          <el-descriptions-item label="客开工作量">
            {{ projectInfo.customDevWorkload != null ? projectInfo.customDevWorkload.toFixed(2) : '-' }} 人天
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 暂无评估数据提示 -->
      <el-card v-else class="mt-20">
        <el-empty description="暂无评估数据，请先开始评估">
          <el-button type="primary" @click="handleStartEvaluation">开始评估</el-button>
        </el-empty>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getProjectDetail,
  deleteProject,
  archiveProject,
  copyProject,
  type ProjectDetailVO,
  type ProjectStatus
} from '@/api/project/projects'

const router = useRouter()
const route = useRoute()
const loading = ref(false)

// 项目详情数据
const projectInfo = reactive<ProjectDetailVO>({
  // 基本信息
  id: 0,
  projectName: '',
  customerName: '',
  sourceSystemId: 0,
  sourceSystemName: '',
  targetSystemId: 0,
  targetSystemName: '',
  projectLeader: '',
  contact: '',
  description: '',
  evaluationDate: '',
  status: 'DRAFT' as ProjectStatus,
  statusText: '',
  userId: 0,
  createByName: '',
  createTime: '',
  updateTime: '',
  // 评估概况
  hasEvaluation: false,
  selectedModuleCount: 0,
  totalWorkload: null,
  coreWorkload: null,
  reportWorkload: null,
  customDevWorkload: null,
  dataVolume: null,
  userCount: null,
  reportCount: null,
  tableCount: null,
  evaluationStatus: null,
  evaluationStatusText: null,
  evaluationTime: null
})

/**
 * 获取项目状态标签类型
 */
function getStatusType(status: string | undefined): 'success' | 'primary' | 'warning' | 'info' | 'danger' {
  const map: Record<string, 'success' | 'primary' | 'warning' | 'info' | 'danger'> = {
    DRAFT: 'info',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    ARCHIVED: 'warning'
  }
  return map[status || ''] || 'info'
}

/**
 * 获取评估状态标签类型
 */
function getEvaluationStatusType(status: string | null): 'success' | 'primary' | 'warning' | 'info' | 'danger' {
  const map: Record<string, 'success' | 'primary' | 'warning' | 'info' | 'danger'> = {
    DRAFT: 'info',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success'
  }
  return map[status || ''] || 'info'
}

/**
 * 加载项目详情
 */
async function loadProjectDetail() {
  const projectId = Number(route.params.id)
  if (!projectId) {
    ElMessage.error('项目ID无效')
    router.push('/project/list')
    return
  }

  loading.value = true
  try {
    const detail = await getProjectDetail(projectId)
    Object.assign(projectInfo, detail)
  } catch {
    ElMessage.error('加载项目详情失败')
    router.push('/project/list')
  } finally {
    loading.value = false
  }
}

/**
 * 编辑项目
 */
function handleEdit() {
  router.push(`/project/edit/${route.params.id}`)
}

/**
 * 开始评估
 */
function handleStartEvaluation() {
  router.push(`/project/evaluate/${route.params.id}`)
}

/**
 * 继续评估
 */
function handleContinueEvaluation() {
  router.push(`/project/evaluate/${route.params.id}`)
}

/**
 * 查看报告（统计图表）
 */
function handleViewReport() {
  router.push(`/project/statistics/${route.params.id}`)
}

/**
 * 导出报告
 */
function handleExport() {
  ElMessage.info('导出功能开发中')
}

/**
 * 复制项目
 */
async function handleCopy() {
  try {
    await ElMessageBox.confirm('确定要复制该项目吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const newId = await copyProject(Number(route.params.id))
    ElMessage.success('复制成功')
    router.push(`/project/detail/${newId}`)
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('复制失败')
    }
  }
}

/**
 * 归档项目
 */
async function handleArchive() {
  try {
    await ElMessageBox.confirm('确定要归档该项目吗？归档后项目将变为只读状态', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await archiveProject(Number(route.params.id))
    ElMessage.success('归档成功')
    loadProjectDetail()
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('归档失败')
    }
  }
}

/**
 * 删除项目
 */
async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除该项目吗？删除后不可恢复', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteProject(Number(route.params.id))
    ElMessage.success('删除成功')
    router.push('/project/list')
  } catch (error: any) {
    // 区分用户取消和API错误
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadProjectDetail()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 20px;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 100px;
  color: #909399;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.workload-value {
  color: #67c23a;
  font-weight: bold;
}

.mt-20 {
  margin-top: 20px;
}
</style>
