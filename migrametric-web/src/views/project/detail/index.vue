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
          <!-- DRAFT 状态：完整操作权限 -->
          <template v-if="projectInfo.status === 'DRAFT'">
            <el-button
              data-testid="btn-start-evaluation"
              type="primary"
              @click="handleStartEvaluation"
            >
              开始评估
            </el-button>
            <el-button @click="handleExport">导出报告</el-button>
            <el-button data-testid="btn-copy-project" @click="handleCopy">复制项目</el-button>
            <el-button type="danger" data-testid="btn-delete-project" @click="handleDelete">删除项目</el-button>
          </template>

          <!-- IN_PROGRESS 状态：继续评估 + 导出 + 复制 -->
          <template v-else-if="projectInfo.status === 'IN_PROGRESS'">
            <el-button
              data-testid="btn-continue-evaluation"
              type="primary"
              @click="handleContinueEvaluation"
            >
              继续评估
            </el-button>
            <el-button @click="handleExport">导出报告</el-button>
            <el-button data-testid="btn-copy-project" @click="handleCopy">复制项目</el-button>
          </template>

          <!-- COMPLETED 状态：查看报告 + 重新评估 + 导出 + 复制 + 归档 -->
          <template v-else-if="projectInfo.status === 'COMPLETED'">
            <el-button
              v-if="projectInfo.hasEvaluation"
              data-testid="btn-view-report"
              type="success"
              @click="handleViewReport"
            >
              查看报告
            </el-button>
            <el-button data-testid="btn-restart-evaluation" @click="handleContinueEvaluation">重新评估</el-button>
            <el-button @click="handleExport">导出报告</el-button>
            <el-button data-testid="btn-copy-project" @click="handleCopy">复制项目</el-button>
            <el-button type="warning" data-testid="btn-archive-project" @click="handleArchive">归档项目</el-button>
          </template>

          <!-- ARCHIVED 状态：只读，仅可查看报告/导出 -->
          <template v-else-if="projectInfo.status === 'ARCHIVED'">
            <el-button
              v-if="projectInfo.hasEvaluation"
              data-testid="btn-view-report"
              type="success"
              @click="handleViewReport"
            >
              查看报告
            </el-button>
            <el-button data-testid="btn-export-report-archived" @click="handleExport">导出报告</el-button>
            <el-tag type="info" effect="plain" data-testid="tag-archived-readonly">只读状态，不可修改</el-tag>
          </template>
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

      <!-- 状态历史卡片（REQ-§3.2.4） -->
      <el-card class="mt-20" data-testid="card-status-history">
        <template #header>
          <div class="card-header">
            <span>状态变更历史</span>
            <el-button
              v-if="isAdmin"
              type="primary"
              link
              data-testid="btn-manual-create-history"
              @click="openManualDialog"
            >
              补录历史
            </el-button>
          </div>
        </template>

        <!-- 过滤栏 -->
        <el-row :gutter="16" class="mb-16">
          <el-col :span="8">
            <el-input
              v-model="historyFilter.operator"
              placeholder="按操作人过滤"
              clearable
              data-testid="filter-operator"
              @change="loadStatusHistory(1)"
            />
          </el-col>
          <el-col :span="8">
            <el-select
              v-model="historyFilter.event"
              placeholder="按事件过滤"
              clearable
              data-testid="filter-event"
              style="width: 100%"
              @change="loadStatusHistory(1)"
            >
              <el-option label="CREATE" value="CREATE" />
              <el-option label="EVAL_START" value="EVAL_START" />
              <el-option label="EVAL_COMPLETE" value="EVAL_COMPLETE" />
              <el-option label="ARCHIVE" value="ARCHIVE" />
              <el-option label="MANUAL_EDIT" value="MANUAL_EDIT" />
            </el-select>
          </el-col>
        </el-row>

        <!-- 时间线 -->
        <el-empty
          v-if="!historyLoading && historyList.length === 0"
          description="暂无状态变更"
        />
        <el-timeline v-else v-loading="historyLoading" data-testid="timeline-status-history">
          <el-timeline-item
            v-for="h in historyList"
            :key="h.id"
            :timestamp="h.changeTime"
            :type="getStatusColor(h.toStatus)"
            :hollow="!h.manualEdit"
            placement="top"
          >
            <el-card shadow="never" :data-testid="'history-item-' + h.event">
              <div class="history-row">
                <span class="history-event">
                  <el-tag :type="getEventColor(h.event)" effect="plain">{{ h.event }}</el-tag>
                  <el-tag v-if="h.manualEdit" type="warning" effect="plain" class="ml-8">手动补录</el-tag>
                </span>
                <span class="history-transition">
                  <el-tag v-if="h.fromStatus" :type="getStatusColor(h.fromStatus)">{{ h.fromStatus }}</el-tag>
                  <span v-if="h.fromStatus"> → </span>
                  <el-tag :type="getStatusColor(h.toStatus)">{{ h.toStatus }}</el-tag>
                </span>
              </div>
              <div class="history-meta">
                <span>操作人：<strong>{{ h.operator }}</strong></span>
                <span v-if="h.reason" class="ml-16">备注：{{ h.reason }}</span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>

        <!-- 分页 -->
        <el-pagination
          v-if="historyTotal > historyPageSize"
          v-model:current-page="historyPageNum"
          v-model:page-size="historyPageSize"
          :total="historyTotal"
          layout="prev, pager, next"
          small
          @current-change="loadStatusHistory"
        />
      </el-card>
    </template>

    <!-- 补录历史对话框（仅 ADMIN 可见） -->
    <el-dialog
      v-model="manualDialogVisible"
      title="补录状态历史"
      width="500px"
      data-testid="dialog-manual-create"
    >
      <el-form ref="manualFormRef" :model="manualForm" :rules="manualRules" label-width="100px">
        <el-form-item label="变更前" prop="fromStatus">
          <el-select v-model="manualForm.fromStatus" placeholder="请选择" data-testid="manual-from-status">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更后" prop="toStatus">
          <el-select v-model="manualForm.toStatus" placeholder="请选择" data-testid="manual-to-status">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件">
          <el-input v-model="manualForm.event" disabled />
        </el-form-item>
        <el-form-item label="变更时间" prop="changeTime">
          <el-date-picker
            v-model="manualForm.changeTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="选择时间"
            style="width: 100%"
            data-testid="manual-change-time"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="manualForm.reason" type="textarea" data-testid="manual-reason" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="manualDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="manualSaving" data-testid="btn-submit-manual" @click="submitManual">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getProjectDetail,
  deleteProject,
  archiveProject,
  copyProject,
  type ProjectDetailVO,
  type ProjectStatus
} from '@/api/project/projects'
import {
  listStatusHistory,
  manualCreateStatusHistory,
  type StatusHistoryVO,
  type StatusHistoryCreateDTO
} from '@/api/project/status-history'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
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
const projectId = computed(() => Number(route.params.id) || 0)

async function loadProjectDetail() {
  if (!projectId.value) {
    ElMessage.error('项目ID无效')
    router.push('/project/list')
    return
  }

  loading.value = true
  try {
    const detail = await getProjectDetail(projectId.value)
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
  if (projectId.value) loadStatusHistory(1)
})

// ============== 状态历史（§3.2.4） ==============
const historyList = ref<StatusHistoryVO[]>([])
const historyTotal = ref(0)
const historyPageNum = ref(1)
const historyPageSize = ref(10)
const historyLoading = ref(false)
const historyFilter = reactive({
  operator: '',
  event: ''
})
const isAdmin = computed(() => userStore.isAdmin)

function getStatusColor(status: string | null): 'success' | 'primary' | 'warning' | 'info' | 'danger' {
  if (status === 'DRAFT') return 'info'
  if (status === 'IN_PROGRESS') return 'warning'
  if (status === 'COMPLETED') return 'success'
  if (status === 'ARCHIVED') return 'info'
  return 'info'
}

function getEventColor(event: string): 'success' | 'primary' | 'warning' | 'info' | 'danger' {
  if (event === 'CREATE') return 'info'
  if (event === 'EVAL_START') return 'warning'
  if (event === 'EVAL_COMPLETE') return 'success'
  if (event === 'ARCHIVE') return 'primary'
  if (event === 'MANUAL_EDIT') return 'danger'
  return 'info'
}

async function loadStatusHistory(pageNum?: number) {
  if (!projectId.value) return
  if (pageNum) historyPageNum.value = pageNum
  historyLoading.value = true
  try {
    const query: Record<string, unknown> = {
      pageNum: historyPageNum.value,
      pageSize: historyPageSize.value
    }
    if (historyFilter.operator) query.operator = historyFilter.operator
    if (historyFilter.event) query.event = historyFilter.event
    const result = await listStatusHistory(projectId.value, query)
    historyList.value = result.records
    historyTotal.value = result.total
  } catch (e) {
    ElMessage.error('加载状态历史失败：' + (e instanceof Error ? e.message : ''))
  } finally {
    historyLoading.value = false
  }
}

// ============== 补录对话框（仅 ADMIN） ==============
const manualDialogVisible = ref(false)
const manualSaving = ref(false)
const manualFormRef = ref<FormInstance>()
const manualForm = reactive<StatusHistoryCreateDTO>({
  fromStatus: '',
  toStatus: '',
  event: 'MANUAL_EDIT',
  reason: '',
  changeTime: '' as unknown as string
})
const manualRules: FormRules = {
  fromStatus: [{ required: true, message: '请选择变更前状态', trigger: 'change' }],
  toStatus: [{ required: true, message: '请选择变更后状态', trigger: 'change' }],
  changeTime: [{ required: true, message: '请选择变更时间', trigger: 'change' }]
}

function openManualDialog() {
  manualForm.fromStatus = ''
  manualForm.toStatus = ''
  manualForm.reason = ''
  manualForm.changeTime = new Date().toISOString().slice(0, 19) as unknown as string
  manualDialogVisible.value = true
}

async function submitManual() {
  if (!manualFormRef.value) return
  try {
    await manualFormRef.value.validate()
  } catch {
    return
  }
  if (manualForm.fromStatus === manualForm.toStatus) {
    ElMessage.error('fromStatus 与 toStatus 不能相同')
    return
  }
  manualSaving.value = true
  try {
    await manualCreateStatusHistory(projectId.value, {
      ...manualForm,
      changeTime: manualForm.changeTime as unknown as string
    })
    ElMessage.success('补录成功')
    manualDialogVisible.value = false
    loadStatusHistory(1)
  } catch (e) {
    ElMessage.error('补录失败：' + (e instanceof Error ? e.message : ''))
  } finally {
    manualSaving.value = false
  }
}
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
.ml-8 { margin-left: 8px; }
.ml-16 { margin-left: 16px; }
.mb-16 { margin-bottom: 16px; }
.history-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.history-event { display: flex; align-items: center; }
.history-transition { display: flex; align-items: center; gap: 4px; }
.history-meta { font-size: 13px; color: #606266; }
</style>
