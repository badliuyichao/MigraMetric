<template>
  <div class="evaluation-wizard">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>工作量评估 - {{ projectName }}</span>
          <el-button @click="handleBack">返回项目</el-button>
        </div>
      </template>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <!-- 评估向导 -->
      <div v-else class="wizard-content">
        <!-- 步骤指示器 -->
        <el-steps :active="currentStep - 1" finish-status="success" align-center class="evaluation-steps">
          <el-step title="系统确认" description="确认升迁系统" />
          <el-step title="选择模块" description="选择迁移模块" />
          <el-step title="填写指标" description="录入评估指标" />
          <el-step title="查看结果" description="工作量汇总" />
        </el-steps>

        <!-- 步骤内容区域 -->
        <div class="step-content">
          <!-- 步骤一：系统确认 -->
          <div v-show="currentStep === 1" class="step-panel">
            <el-card shadow="never">
              <template #header>
                <span>系统确认</span>
              </template>

              <el-alert
                title="请确认升迁系统信息"
                type="info"
                :closable="false"
                show-icon
                class="mb-20"
              />

              <el-descriptions :column="2" border>
                <el-descriptions-item label="项目名称">
                  {{ projectName }}
                </el-descriptions-item>
                <el-descriptions-item label="客户名称">
                  {{ customerName }}
                </el-descriptions-item>
                <el-descriptions-item label="源系统">
                  <el-tag type="primary">{{ sourceSystemName }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="目标系统">
                  <el-tag type="success">{{ targetSystemName }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="项目负责人">
                  {{ projectLeader || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="评估日期">
                  {{ evaluationDate || '-' }}
                </el-descriptions-item>
              </el-descriptions>

              <div class="system-arrow">
                <el-icon><ArrowRight /></el-icon>
                <span>升迁</span>
                <el-icon><ArrowRight /></el-icon>
              </div>

              <el-alert
                title="如需修改系统信息，请返回项目基本信息页面进行编辑"
                type="warning"
                :closable="false"
                show-icon
                class="mt-20"
              />
            </el-card>
          </div>

          <!-- 步骤二：选择模块 -->
          <div v-show="currentStep === 2" class="step-panel">
            <el-card shadow="never">
              <template #header>
                <span>选择迁移模块</span>
              </template>

              <el-alert
                title="请选择需要迁移的模块，至少选择一个模块"
                type="info"
                :closable="false"
                show-icon
                class="mb-20"
              />

              <div class="module-tip">
                <span>已选模块：</span>
                <el-tag type="primary">{{ selectedModules.length }} 个</el-tag>
                <span class="module-hint">（可调整每个模块的加权系数）</span>
              </div>

              <el-table
                ref="moduleTableRef"
                :data="availableModules"
                border
                style="width: 100%"
                data-testid="module-table"
                @selection-change="handleSelectionChange"
              >
                <el-table-column type="selection" width="55" />
                <el-table-column prop="moduleName" label="模块名称" width="150" />
                <el-table-column prop="category" label="分类" width="120" />
                <el-table-column prop="baseWorkload" label="基础人天" width="100">
                  <template #default="{ row }">
                    {{ row.baseWorkload }} 人天
                  </template>
                </el-table-column>
                <el-table-column prop="defaultWeight" label="默认系数" width="100">
                  <template #default="{ row }">
                    {{ row.defaultWeight.toFixed(2) }}
                  </template>
                </el-table-column>
                <el-table-column label="调整系数" width="180">
                  <template #default="{ row }">
                    <el-input-number
                      v-model="row.weight"
                      :min="0.1"
                      :max="10"
                      :step="0.1"
                      :precision="2"
                      size="small"
                      controls-position="right"
                      :disabled="!row.checked"
                      @change="handleWeightChange(row)"
                    />
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>

          <!-- 步骤三：填写指标 -->
          <div v-show="currentStep === 3" class="step-panel">
            <el-card shadow="never">
              <template #header>
                <span>填写评估指标</span>
              </template>

              <el-form
                ref="metricsFormRef"
                :model="metricsForm"
                label-width="140px"
                class="metrics-form"
              >
                <!-- 已选模块信息 -->
                <el-divider content-position="left">已选模块信息</el-divider>
                <div class="selected-modules-summary">
                  <el-tag v-for="mod in selectedModules" :key="mod.moduleId" class="module-tag">
                    {{ mod.moduleName }}
                  </el-tag>
                  <span v-if="selectedModules.length === 0" class="no-modules">
                    暂未选择模块，请返回上一步选择
                  </span>
                </div>

                <!-- 数据库指标 -->
                <el-divider content-position="left">数据库指标</el-divider>
                <el-form-item label="数据库表数量" required>
                  <el-input-number
                    v-model="metricsForm.tableCount"
                    :min="0"
                    :max="999999"
                    controls-position="right"
                    style="width: 100%"
                    placeholder="请输入需要迁移的数据库表数量"
                  />
                  <span class="form-tip">单位：个</span>
                </el-form-item>

                <!-- 数据量阶梯 -->
                <el-form-item label="数据量" required>
                  <el-input-number
                    v-model="metricsForm.dataVolume"
                    :min="0"
                    :precision="2"
                    controls-position="right"
                    style="width: calc(100% - 120px)"
                    placeholder="请输入数据总量"
                    @change="handleDataVolumeChange"
                  />
                  <span class="form-unit">万条</span>
                  <el-tag v-if="dataVolumeMatch" type="success" class="ml-10">
                    {{ dataVolumeMatch.ladderName }} (系数: {{ dataVolumeMatch.weight }})
                  </el-tag>
                </el-form-item>

                <!-- 用户数阶梯 -->
                <el-form-item label="用户数量" required>
                  <el-input-number
                    v-model="metricsForm.userCount"
                    :min="0"
                    :max="999999"
                    controls-position="right"
                    style="width: calc(100% - 120px)"
                    placeholder="请输入系统用户总数"
                    @change="handleUserCountChange"
                  />
                  <span class="form-unit">人</span>
                  <el-tag v-if="userCountMatch" type="success" class="ml-10">
                    {{ userCountMatch.ladderName }} (系数: {{ userCountMatch.weight }})
                  </el-tag>
                </el-form-item>

                <!-- 报表指标 -->
                <el-divider content-position="left">报表指标</el-divider>
                <el-form-item label="报表数量" required>
                  <el-input-number
                    v-model="metricsForm.reportCount"
                    :min="0"
                    :max="99999"
                    controls-position="right"
                    style="width: 100%"
                    placeholder="请输入需要迁移的报表数量"
                  />
                  <span class="form-tip">单位：个</span>
                </el-form-item>

                <!-- 客开情况 -->
                <el-divider content-position="left">客开情况</el-divider>
                <el-form-item label="是否有客开" required>
                  <el-radio-group v-model="metricsForm.hasCustomDev">
                    <el-radio :value="false">无</el-radio>
                    <el-radio :value="true">有</el-radio>
                  </el-radio-group>
                </el-form-item>

                <template v-if="metricsForm.hasCustomDev">
                  <el-form-item label="客开模块数量">
                    <el-input-number
                      v-model="metricsForm.customDevCount"
                      :min="0"
                      :max="999"
                      controls-position="right"
                      style="width: 100%"
                      placeholder="请输入客开模块数量"
                    />
                  </el-form-item>
                  <el-form-item label="客开人天">
                    <el-input-number
                      v-model="metricsForm.customDevWorkload"
                      :min="0"
                      :precision="2"
                      controls-position="right"
                      style="width: 100%"
                      placeholder="请输入客开评估人天数"
                    />
                    <span class="form-tip">单位：人天</span>
                  </el-form-item>
                </template>

                <!-- 数据清洗需求 -->
                <el-divider content-position="left">数据清洗需求（仅供参考）</el-divider>
                <el-form-item label="数据清洗描述">
                  <el-input
                    v-model="metricsForm.dataCleanDesc"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入数据清洗需求描述"
                  />
                </el-form-item>
                <el-form-item label="数据清洗复杂度">
                  <el-radio-group v-model="metricsForm.dataCleanComplexity">
                    <el-radio :value="1">简单</el-radio>
                    <el-radio :value="2">中等</el-radio>
                    <el-radio :value="3">复杂</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-form>
            </el-card>
          </div>

          <!-- 步骤四：查看结果 -->
          <div v-show="currentStep === 4" class="step-panel">
            <el-card shadow="never">
              <template #header>
                <span>工作量评估结果</span>
              </template>

              <div v-if="calculationLoading" class="loading-container">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>计算中...</span>
              </div>

              <div v-else-if="workloadResult" class="result-content" data-testid="eval-result">
                <!-- 工作量汇总卡片 -->
                <el-row :gutter="20" class="workload-summary" data-testid="workload-summary">
                  <el-col :span="6">
                    <el-statistic title="核心迁移工作量" :value="workloadResult.coreWorkload" suffix="人天" />
                  </el-col>
                  <el-col :span="6">
                    <el-statistic title="报表工作量" :value="workloadResult.reportWorkload" suffix="人天" />
                  </el-col>
                  <el-col :span="6">
                    <el-statistic title="客开工作量" :value="workloadResult.customDevWorkload" suffix="人天" />
                  </el-col>
                  <el-col :span="6">
                    <el-statistic title="总工作量" :value="workloadResult.totalWorkload" suffix="人天">
                      <template #prefix>
                        <span class="total-prefix">合计</span>
                      </template>
                    </el-statistic>
                  </el-col>
                </el-row>

                <!-- 阶梯系数说明 -->
                <el-alert
                  :title="`数据量系数: ${workloadResult.dataVolumeWeight} | 用户数系数: ${workloadResult.userCountWeight} | 报表系数: ${workloadResult.reportCoefficient}`"
                  type="info"
                  :closable="false"
                  show-icon
                  class="mb-20"
                />

                <!-- 模块工作量明细 -->
                <el-divider content-position="left">模块工作量明细</el-divider>
                <el-table :data="workloadResult.moduleWorkloads" border style="width: 100%">
                  <el-table-column prop="moduleName" label="模块名称" width="150" />
                  <el-table-column prop="baseWorkload" label="基础人天" width="100">
                    <template #default="{ row }">
                      {{ row.baseWorkload.toFixed(2) }} 人天
                    </template>
                  </el-table-column>
                  <el-table-column prop="weight" label="加权系数" width="100">
                    <template #default="{ row }">
                      {{ row.weight.toFixed(2) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="dataVolumeWeight" label="数据量系数" width="100">
                    <template #default="{ row }">
                      {{ row.dataVolumeWeight.toFixed(2) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="userCountWeight" label="用户数系数" width="100">
                    <template #default="{ row }">
                      {{ row.userCountWeight.toFixed(2) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="moduleWorkload" label="模块工作量" width="120">
                    <template #default="{ row }">
                      <span class="workload-value">{{ row.moduleWorkload.toFixed(2) }} 人天</span>
                    </template>
                  </el-table-column>
                </el-table>

                <!-- 评估指标汇总 -->
                <el-divider content-position="left">评估指标汇总</el-divider>
                <el-descriptions :column="3" border>
                  <el-descriptions-item label="数据库表数量">
                    {{ metricsForm.tableCount || '-' }} 个
                  </el-descriptions-item>
                  <el-descriptions-item label="数据量">
                    {{ metricsForm.dataVolume || '-' }} 万条 ({{ dataVolumeMatch?.ladderName || '-' }})
                  </el-descriptions-item>
                  <el-descriptions-item label="用户数量">
                    {{ metricsForm.userCount || '-' }} 人 ({{ userCountMatch?.ladderName || '-' }})
                  </el-descriptions-item>
                  <el-descriptions-item label="报表数量">
                    {{ metricsForm.reportCount || '-' }} 个
                  </el-descriptions-item>
                  <el-descriptions-item label="客开情况">
                    {{ metricsForm.hasCustomDev ? '有' : '无' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="客开人天">
                    {{ metricsForm.hasCustomDev ? (metricsForm.customDevWorkload || 0) + ' 人天' : '-' }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>

              <el-empty v-else description="暂无计算结果，请先完成指标填写" />
            </el-card>
          </div>
        </div>

        <!-- 步骤操作按钮 -->
        <div class="step-actions">
          <el-button v-if="currentStep > 1" data-testid="btn-prev-step" @click="handlePrevStep">上一步</el-button>
          <el-button v-if="currentStep < 3" data-testid="btn-next-step" type="primary" :disabled="!canNextStep" @click="handleNextStep">
            下一步
          </el-button>
          <el-button v-if="currentStep === 3" data-testid="btn-calculate" type="primary" :disabled="!canCalculate" :loading="calculating" @click="handleCalculate">
            计算工作量
          </el-button>
          <el-button v-if="currentStep === 4" data-testid="btn-complete-evaluation" type="success" :disabled="!canComplete" :loading="completing" @click="handleComplete">
            完成评估
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, ArrowRight } from '@element-plus/icons-vue'
import type { FormInstance, ElTable } from 'element-plus'
import { getProjectDetail } from '@/api/project/projects'
import type { ProjectDetailVO } from '@/api/project/projects'
import {
  createEvaluation,
  getEvaluation,
  saveIndicators,
  getProjectModules,
  saveModuleConfig,
  matchDataVolume,
  matchUserCount,
  calculateWorkload,
  completeEvaluation,
  getConfiguredModules
} from '@/api/evaluation/evaluations'
import type {
  EvaluationMetrics,
  EvaluationDetailVO,
  LadderMatchVO,
  ModuleConfigItem,
  WorkloadResultVO
} from '@/api/evaluation/evaluations'

const router = useRouter()
const route = useRoute()

// 状态
const loading = ref(true)
const calculating = ref(false)
const completing = ref(false)
const calculationLoading = ref(false)
const currentStep = ref(1)
const prevStep = ref(1)
const metricsFormRef = ref<FormInstance>()
const moduleTableRef = ref<InstanceType<typeof ElTable>>()
const modulesLoaded = ref(false)

// 项目信息
const projectId = ref(0)
const projectName = ref('')
const customerName = ref('')
const sourceSystemName = ref('')
const targetSystemName = ref('')
const projectLeader = ref('')
const evaluationDate = ref('')

// 可用模块列表
const availableModules = ref<ModuleConfigItem[]>([])
const selectedModules = ref<ModuleConfigItem[]>([])
const initialModules = ref<ModuleConfigItem[]>([])

// 阶梯匹配结果
const dataVolumeMatch = ref<LadderMatchVO | null>(null)
const userCountMatch = ref<LadderMatchVO | null>(null)

// 指标表单
const metricsForm = reactive<EvaluationMetrics>({
  tableCount: null,
  dataVolume: null,
  dataVolumeLadderId: null,
  dataVolumeLadderName: null,
  dataVolumeWeight: null,
  userCount: null,
  userCountLadderId: null,
  userCountLadderName: null,
  userCountWeight: null,
  reportCount: null,
  hasCustomDev: false,
  customDevCount: null,
  customDevWorkload: null,
  dataCleanDesc: '',
  dataCleanComplexity: undefined
})

// 计算结果
const workloadResult = ref<WorkloadResultVO | null>(null)

// 能否进入下一步
const canNextStep = computed(() => {
  if (currentStep.value === 1) {
    return true // 步骤一始终可以下一步
  }
  if (currentStep.value === 2) {
    return selectedModules.value.length > 0 // 至少选择一个模块
  }
  return true
})

// 能否计算
const canCalculate = computed(() => {
  return metricsForm.tableCount != null && metricsForm.tableCount > 0 &&
         metricsForm.dataVolume != null && metricsForm.dataVolume > 0 &&
         metricsForm.userCount != null && metricsForm.userCount > 0 &&
         metricsForm.reportCount != null && metricsForm.reportCount >= 0 &&
         selectedModules.value.length > 0 &&
         dataVolumeMatch.value != null &&
         userCountMatch.value != null
})

// 能否完成评估
const canComplete = computed(() => {
  return workloadResult.value != null
})

/**
 * 加载项目信息
 */
async function loadProjectInfo() {
  try {
    const id = Number(route.params.id)
    if (!id) {
      ElMessage.error('项目ID无效')
      router.push('/project/list')
      return
    }

    projectId.value = id

    // 获取项目详情
    console.log('开始加载项目详情, id:', id)
    const detail = await getProjectDetail(id) as ProjectDetailVO
    console.log('项目详情加载成功:', detail)

    projectName.value = detail.projectName || ''
    customerName.value = detail.customerName || ''
    sourceSystemName.value = detail.sourceSystemName || ''
    targetSystemName.value = detail.targetSystemName || ''
    projectLeader.value = detail.projectLeader || ''
    evaluationDate.value = detail.evaluationDate || ''

    // 获取评估信息
    console.log('开始获取评估信息, projectId:', id)
    const evaluation = await getEvaluation(id)
    console.log('评估信息:', evaluation)

    // 如果没有评估记录，创建评估
    if (!evaluation || !evaluation.id) {
      console.log('创建评估记录')
      await createEvaluation(id)
    }

    // 加载已有评估数据
    await loadExistingEvaluation(id)
  } catch (error) {
    console.error('加载项目信息失败:', error)
    ElMessage.error('加载项目信息失败')
    router.push('/project/list')
  } finally {
    loading.value = false
  }
}

/**
 * 加载已有评估数据
 */
async function loadExistingEvaluation(projectId: number) {
  try {
    console.log('开始加载已有评估数据, projectId:', projectId)
    const evaluation = await getEvaluation(projectId)
    console.log('已有评估数据:', evaluation)
    if (evaluation && evaluation.tableCount != null) {
      // 填充指标表单
      metricsForm.tableCount = evaluation.tableCount
      metricsForm.dataVolume = evaluation.dataVolume ?? null
      metricsForm.dataVolumeLadderId = evaluation.dataVolumeLadderId ?? null
      metricsForm.dataVolumeLadderName = evaluation.dataVolumeLadderName ?? null
      metricsForm.dataVolumeWeight = evaluation.dataVolumeWeight ?? null
      metricsForm.userCount = evaluation.userCount ?? null
      metricsForm.userCountLadderId = evaluation.userCountLadderId ?? null
      metricsForm.userCountLadderName = evaluation.userCountLadderName ?? null
      metricsForm.userCountWeight = evaluation.userCountWeight ?? null
      metricsForm.reportCount = evaluation.reportCount ?? null
      metricsForm.hasCustomDev = evaluation.hasCustomDev ?? false
      metricsForm.customDevCount = evaluation.customDevCount ?? null
      metricsForm.customDevWorkload = evaluation.customDevWorkload ?? null
      metricsForm.dataCleanDesc = evaluation.dataCleanDesc ?? ''
      metricsForm.dataCleanComplexity = evaluation.dataCleanComplexityText ? 2 : undefined

      // 如果有阶梯信息，设置匹配结果
      if (evaluation.dataVolumeWeight && evaluation.dataVolumeLadderName) {
        dataVolumeMatch.value = {
          ladderId: evaluation.dataVolumeLadderId!,
          ladderName: evaluation.dataVolumeLadderName,
          weight: evaluation.dataVolumeWeight,
          ladderType: 'DATA_VOLUME',
          inputValue: evaluation.dataVolume + '万条',
          rangeText: ''
        }
      }
      if (evaluation.userCountWeight && evaluation.userCountLadderName) {
        userCountMatch.value = {
          ladderId: evaluation.userCountLadderId!,
          ladderName: evaluation.userCountLadderName,
          weight: evaluation.userCountWeight,
          ladderType: 'USER_COUNT',
          inputValue: evaluation.userCount + '人',
          rangeText: ''
        }
      }
    }
  } catch {
    // 评估不存在，无需处理
  }
}

/**
 * 加载可用模块列表（从API获取）
 */
async function loadAvailableModules() {
  try {
    const modules: ModuleConfigItem[] = await getProjectModules(projectId.value)
    availableModules.value = modules.map((mod: ModuleConfigItem) => ({
      moduleId: mod.moduleId,
      moduleName: mod.moduleName,
      category: mod.category,
      baseWorkload: mod.baseWorkload,
      defaultWeight: mod.defaultWeight,
      weight: mod.weight || mod.defaultWeight,
      checked: mod.checked || false
    }))

    // 保存初始模块列表
    initialModules.value = [...availableModules.value]

    // 加载已配置的模块
    const configuredModules = await getConfiguredModules(projectId.value)
    if (configuredModules.length > 0) {
      const configuredMap = new Map(configuredModules.map(m => [m.moduleId, m]))

      // 根据已有配置设置选中状态
      availableModules.value.forEach(mod => {
        const configured = configuredMap.get(mod.moduleId)
        if (configured) {
          mod.checked = true
          mod.weight = configured.weight || mod.defaultWeight
        }
      })

      // 更新选中列表中的模块
      selectedModules.value = availableModules.value.filter(m => m.checked)

      // 标记模块已加载
      modulesLoaded.value = true
    }
  } catch {
    ElMessage.error('加载模块列表失败')
    availableModules.value = []
  }
}

/**
 * 处理模块选择变化
 */
function handleSelectionChange(selection: ModuleConfigItem[]) {
  selectedModules.value = selection.map(mod => {
    if (!mod.weight) {
      mod.weight = mod.defaultWeight
    }
    return mod
  })
}

/**
 * 处理系数调整变化
 */
function handleWeightChange(row: ModuleConfigItem) {
  if (row.weight !== row.defaultWeight) {
    row.checked = true
  }
  // 更新选中列表中的模块
  const index = selectedModules.value.findIndex(m => m.moduleId === row.moduleId)
  if (index >= 0) {
    selectedModules.value[index] = { ...row }
  }
}

/**
 * 处理数据量变化 - 调用后端API匹配阶梯
 */
async function handleDataVolumeChange(value: number | null | undefined) {
  if (value != null && value > 0) {
    try {
      const match = await matchDataVolume(value)
      dataVolumeMatch.value = match
      if (match) {
        metricsForm.dataVolumeLadderId = match.ladderId
        metricsForm.dataVolumeLadderName = match.ladderName
        metricsForm.dataVolumeWeight = match.weight
      }
    } catch {
      ElMessage.warning('阶梯匹配失败')
      dataVolumeMatch.value = null
    }
  } else {
    dataVolumeMatch.value = null
    metricsForm.dataVolumeLadderId = null
    metricsForm.dataVolumeLadderName = null
    metricsForm.dataVolumeWeight = null
  }
}

/**
 * 处理用户数变化 - 调用后端API匹配阶梯
 */
async function handleUserCountChange(value: number | null | undefined) {
  if (value != null && value > 0) {
    try {
      const match = await matchUserCount(value)
      userCountMatch.value = match
      if (match) {
        metricsForm.userCountLadderId = match.ladderId
        metricsForm.userCountLadderName = match.ladderName
        metricsForm.userCountWeight = match.weight
      }
    } catch {
      ElMessage.warning('阶梯匹配失败')
      userCountMatch.value = null
    }
  } else {
    userCountMatch.value = null
    metricsForm.userCountLadderId = null
    metricsForm.userCountLadderName = null
    metricsForm.userCountWeight = null
  }
}

/**
 * 上一步
 */
function handlePrevStep() {
  if (currentStep.value > 1) {
    prevStep.value = currentStep.value
    currentStep.value--
  }
}

/**
 * 下一步
 */
async function handleNextStep() {
  if (currentStep.value < 4) {
    const fromStep = currentStep.value

    // 加载模块列表（只在首次进入步骤二时加载一次）
    if (currentStep.value === 1 && !modulesLoaded.value) {
      await loadAvailableModules()
    }

    // 保存当前步骤数据
    // 从步骤2进入步骤3时，保存模块配置
    if (fromStep === 2 && selectedModules.value.length > 0) {
      await saveModuleConfig(projectId.value, selectedModules.value)
      ElMessage.success('模块配置已保存')
    }

    prevStep.value = currentStep.value
    currentStep.value++
  }
}

/**
 * 保存当前步骤数据
 */
async function saveCurrentStepData() {
  try {
    // 保存指标数据（步骤三）
    if (currentStep.value === 3) {
      await saveIndicators(projectId.value, metricsForm)
      ElMessage.success('评估指标已保存')
    }
  } catch {
    ElMessage.error('保存失败')
    return false
  }
  return true
}

/**
 * 计算工作量 - 调用后端API
 */
async function handleCalculate() {
  calculating.value = true
  calculationLoading.value = true

  try {
    // 先保存当前数据
    await saveCurrentStepData()

    // 调用后端API计算工作量
    const result = await calculateWorkload(projectId.value)
    workloadResult.value = result

    calculationLoading.value = false
    currentStep.value = 4
    ElMessage.success('工作量计算完成')
  } catch {
    ElMessage.error('计算工作量失败')
    calculationLoading.value = false
  } finally {
    calculating.value = false
  }
}

/**
 * 完成评估 - 调用后端API
 */
async function handleComplete() {
  try {
    await ElMessageBox.confirm('确认完成评估吗？完成后将生成评估报告。', '确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })

    completing.value = true
    await completeEvaluation(projectId.value)

    ElMessage.success('评估完成')
    router.push(`/project/detail/${projectId.value}`)
  } catch {
    // 用户取消或API错误
  } finally {
    completing.value = false
  }
}

/**
 * 返回项目
 */
function handleBack() {
  router.push(`/project/detail/${projectId.value}`)
}

onMounted(() => {
  loadProjectInfo()
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.evaluation-steps {
  margin-bottom: 30px;
}

.step-content {
  min-height: 400px;
}

.step-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.system-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin: 20px 0;
  color: #409eff;
  font-size: 14px;

  .el-icon {
    font-size: 20px;
  }
}

.module-tip {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;

  .module-hint {
    color: #909399;
    font-size: 12px;
  }
}

.module-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.no-modules {
  color: #909399;
  font-size: 14px;
}

.selected-modules-summary {
  min-height: 40px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.metrics-form {
  max-width: 800px;

  .form-tip {
    margin-left: 10px;
    color: #909399;
    font-size: 12px;
  }

  .form-unit {
    margin-left: 10px;
    color: #606266;
    font-size: 14px;
  }
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 100px;
  color: #909399;
}

.workload-summary {
  margin-bottom: 30px;

  .total-prefix {
    font-size: 14px;
    font-weight: bold;
  }
}

.workload-value {
  color: #409eff;
  font-weight: bold;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.mb-20 {
  margin-bottom: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.ml-10 {
  margin-left: 10px;
}

.result-content {
  animation: fadeIn 0.3s ease;
}
</style>
