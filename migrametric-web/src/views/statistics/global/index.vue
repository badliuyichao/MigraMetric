<template>
  <div class="statistics-global-container" v-loading="loading">
    <!-- 标题 -->
    <el-card shadow="hover" class="mb-16">
      <template #header>
        <div class="card-header">
          <span class="title">全局统计仪表盘</span>
          <span class="subtitle">跨项目聚合 · 仅显示已评估（COMPLETED）项目</span>
        </div>
      </template>

      <!-- 筛选条 -->
      <el-form :inline="true" :model="filters" class="filter-form" data-testid="filter-form">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="filters.dateFrom"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="不限"
            data-testid="filter-date-from"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="filters.dateTo"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="不限"
            data-testid="filter-date-to"
          />
        </el-form-item>
        <el-form-item label="源系统">
          <el-select v-model="filters.sourceSystemId" placeholder="不限" clearable data-testid="filter-source-system" style="width: 160px">
            <el-option v-for="s in sourceSystems" :key="s.id" :label="s.systemName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标系统">
          <el-select v-model="filters.targetSystemId" placeholder="不限" clearable data-testid="filter-target-system" style="width: 160px">
            <el-option v-for="s in targetSystems" :key="s.id" :label="s.systemName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="默认 COMPLETED" clearable data-testid="filter-status" style="width: 140px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery" data-testid="btn-query">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset" data-testid="btn-reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 空数据 -->
    <el-empty
      v-if="isEmpty"
      description="当前筛选条件下暂无已评估项目"
      data-testid="empty-state"
    >
      <el-button type="primary" @click="router.push('/project/create')">前往创建项目</el-button>
    </el-empty>

    <template v-else>
      <!-- 1. 全局概览 -->
      <el-row :gutter="16" class="mb-16">
        <el-col :span="6">
          <el-card shadow="hover" data-testid="card-project-count">
            <el-statistic title="已评估项目" :value="overview.projectCount" suffix="个">
              <template #prefix><el-icon><Folder /></el-icon></template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" data-testid="card-total-workload">
            <el-statistic title="总工作量" :value="overview.totalWorkload" :precision="2" suffix="人天">
              <template #prefix><el-icon><TrendCharts /></el-icon></template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" data-testid="card-avg-workload">
            <el-statistic title="平均工作量" :value="overview.avgWorkload" :precision="2" suffix="人天/项目">
              <template #prefix><el-icon><DataLine /></el-icon></template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" data-testid="card-estimated-months">
            <el-statistic title="预估工时" :value="overview.estimatedMonths" :precision="2" suffix="人月">
              <template #prefix><el-icon><Clock /></el-icon></template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>

      <!-- 2. 按工作量类型 + 按模块 -->
      <el-row :gutter="16" class="mb-16">
        <el-col :span="12">
          <el-card shadow="hover" data-testid="card-type-pie">
            <template #header><span class="card-title">按工作量类型</span></template>
            <div ref="typePieRef" class="chart-container" data-testid="chart-type-pie"></div>
            <div class="chart-legend" v-if="typeItems.length > 0" data-testid="legend-type">
              <div v-for="item in typeItems" :key="item.name" class="legend-item">
                <span class="legend-color" :style="{ backgroundColor: getColor(item.name) }"></span>
                <span class="legend-text">{{ item.name }}</span>
                <span class="legend-value">{{ item.value }} 人天</span>
                <span class="legend-percent">{{ item.percentage }}%</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" data-testid="card-module-bar">
            <template #header><span class="card-title">按模块（Top 10）</span></template>
            <div ref="moduleBarRef" class="chart-container" data-testid="chart-module-bar"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 3. 复杂度分布 -->
      <el-row :gutter="16" class="mb-16">
        <el-col :span="12">
          <el-card shadow="hover" data-testid="card-data-volume-ladder">
            <template #header><span class="card-title">数据量阶梯分布</span></template>
            <div ref="dvBarRef" class="chart-container" data-testid="chart-data-volume-ladder"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" data-testid="card-user-count-ladder">
            <template #header><span class="card-title">用户数阶梯分布</span></template>
            <div ref="ucBarRef" class="chart-container" data-testid="chart-user-count-ladder"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 4. 高复杂度模块 -->
      <el-card shadow="hover" class="mb-16" data-testid="card-high-complexity">
        <template #header><span class="card-title">高复杂度模块（系数 ≥ 1.5）</span></template>
        <div v-if="highComplexityModules.length === 0" class="empty-hint">暂无高复杂度模块</div>
        <el-tag
          v-for="m in highComplexityModules"
          :key="m.moduleName"
          type="warning"
          effect="light"
          class="mr-8 mb-8"
          data-testid="tag-high-complexity"
        >
          {{ m.moduleName }} (系数 {{ m.weight }}，{{ m.projectCount }} 个项目)
        </el-tag>
      </el-card>

      <!-- 5. 排行榜 -->
      <el-card shadow="hover" data-testid="card-ranking">
        <template #header>
          <div class="card-header">
            <span class="card-title">项目排行榜</span>
            <div class="ranking-controls">
              <el-select v-model="rankingMetric" @change="loadRanking" data-testid="ranking-metric" style="width: 120px">
                <el-option label="按工作量" value="workload" />
                <el-option label="按用户数" value="userCount" />
                <el-option label="按数据量" value="dataVolume" />
              </el-select>
              <el-select v-model="rankingLimit" @change="loadRanking" data-testid="ranking-limit" style="width: 100px; margin-left: 8px">
                <el-option v-for="n in [5, 10, 20, 50]" :key="n" :label="'Top ' + n" :value="n" />
              </el-select>
            </div>
          </div>
        </template>
        <el-table :data="rankingItems" stripe data-testid="table-ranking">
          <el-table-column type="index" label="#" width="60" />
          <el-table-column prop="projectName" label="项目名称" />
          <el-table-column prop="customerName" label="客户名称" />
          <el-table-column prop="value" label="指标值" width="120" sortable>
            <template #default="{ row }">
              <span class="value-cell">{{ row.value }} {{ row.unit }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Folder, TrendCharts, DataLine, Clock, Search, Refresh
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getGlobalAggregations,
  getGlobalRanking,
  type AggregationItem,
  type GlobalAggregationVO,
  type GlobalRankingVO,
  type HighComplexityModule,
  type LadderBucket,
  type RankingItem
} from '@/api/statistics/global'

const router = useRouter()

// ========== 状态 ==========
const loading = ref(false)
const filters = ref({
  dimension: 'type' as 'module' | 'type' | 'complexity',
  dateFrom: undefined as string | undefined,
  dateTo: undefined as string | undefined,
  sourceSystemId: undefined as number | undefined,
  targetSystemId: undefined as number | undefined,
  status: 'COMPLETED' as string | undefined
})

const sourceSystems = ref<{ id: number; systemName: string }[]>([])
const targetSystems = ref<{ id: number; systemName: string }[]>([])

const typeAggregation = ref<GlobalAggregationVO | null>(null)
const moduleAggregation = ref<GlobalAggregationVO | null>(null)
const complexityAggregation = ref<GlobalAggregationVO | null>(null)
const overview = ref({ projectCount: 0, totalWorkload: 0, avgWorkload: 0, estimatedMonths: 0 })

const rankingMetric = ref<'workload' | 'userCount' | 'dataVolume'>('workload')
const rankingLimit = ref(10)
const rankingItems = ref<RankingItem[]>([])

const typePieRef = ref<HTMLDivElement>()
const moduleBarRef = ref<HTMLDivElement>()
const dvBarRef = ref<HTMLDivElement>()
const ucBarRef = ref<HTMLDivElement>()

let typePieChart: echarts.ECharts | null = null
let moduleBarChart: echarts.ECharts | null = null
let dvBarChart: echarts.ECharts | null = null
let ucBarChart: echarts.ECharts | null = null

const typeItems = computed<AggregationItem[]>(() => typeAggregation.value?.items || [])
const highComplexityModules = computed<HighComplexityModule[]>(() => complexityAggregation.value?.highComplexityModules || [])

const isEmpty = computed(() =>
  typeItems.value.length === 0 &&
  (moduleAggregation.value?.items?.length || 0) === 0 &&
  (complexityAggregation.value?.dataVolumeDistribution?.length || 0) === 0 &&
  rankingItems.value.length === 0
)

// ========== 颜色映射 ==========
const colorMap: Record<string, string> = {
  '核心迁移': '#409EFF',
  '报表迁移': '#67C23A',
  '客开定制': '#E6A23C',
  '总账管理': '#409EFF',
  '采购管理': '#67C23A',
  '销售管理': '#E6A23C',
  '生产计划': '#F56C6C',
  '人事管理': '#909399',
  '应收管理': '#9C27B0',
  '应付管理': '#FF9800',
  '库存管理': '#00BCD4'
}
function getColor(name: string): string {
  return colorMap[name] || '#909399'
}

// ========== 加载逻辑 ==========
async function loadAll() {
  loading.value = true
  try {
    const [type, module, complexity] = await Promise.all([
      getGlobalAggregations({ ...filters.value, dimension: 'type' }),
      getGlobalAggregations({ ...filters.value, dimension: 'module' }),
      getGlobalAggregations({ ...filters.value, dimension: 'complexity' })
    ])
    typeAggregation.value = type
    moduleAggregation.value = module
    complexityAggregation.value = complexity

    // 从 type 维度数据计算概览
    const total = Number(type.total || 0)
    const projectCount = typeItems.value[0]?.projectCount || 0
    overview.value = {
      projectCount,
      totalWorkload: total,
      avgWorkload: projectCount > 0 ? total / projectCount : 0,
      estimatedMonths: total / 22
    }

    await nextTick()
    renderTypePie()
    renderModuleBar()
    renderLadderBar(dvBarRef.value, complexity.dataVolumeDistribution, '数据量', dvBarChart, c => { dvBarChart = c })
    renderLadderBar(ucBarRef.value, complexity.userCountDistribution, '用户数', ucBarChart, c => { ucBarChart = c })

    await loadRanking()
  } catch (e) {
    ElMessage.error('加载全局统计失败：' + (e instanceof Error ? e.message : '未知错误'))
  } finally {
    loading.value = false
  }
}

async function loadRanking() {
  try {
    const r = await getGlobalRanking(rankingMetric.value, rankingLimit.value)
    rankingItems.value = r.items
  } catch (e) {
    ElMessage.error('加载排行榜失败：' + (e instanceof Error ? e.message : '未知错误'))
  }
}

async function loadSystems() {
  // 简化：通过现有的 listEnabledSystemTypes 拉源/目标系统
  const { listEnabledSystemTypes } = await import('@/api/system/types')
  const [src, tgt] = await Promise.all([
    listEnabledSystemTypes(1),
    listEnabledSystemTypes(2)
  ])
  sourceSystems.value = src as any
  targetSystems.value = tgt as any
}

// ========== ECharts 渲染 ==========
function renderTypePie() {
  if (!typePieRef.value) return
  if (typePieChart) typePieChart.dispose()
  typePieChart = echarts.init(typePieRef.value)
  typePieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 人天 ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: true,
      data: typeItems.value.map(i => ({ name: i.name, value: i.value, itemStyle: { color: getColor(i.name) } }))
    }]
  })
}

function renderModuleBar() {
  if (!moduleBarRef.value) return
  if (moduleBarChart) moduleBarChart.dispose()
  moduleBarChart = echarts.init(moduleBarRef.value)
  const items = (moduleAggregation.value?.items || []).slice(0, 10)
  // 按 value 升序，让最大 bar 在最上
  const sorted = [...items].sort((a, b) => a.value - b.value)
  moduleBarChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: sorted.map(i => i.name) },
    series: [{
      type: 'bar',
      data: sorted.map(i => i.value),
      itemStyle: { color: '#409EFF' },
      label: { show: true, position: 'right', formatter: '{c} 人天' }
    }]
  })
}

function renderLadderBar(
  el: HTMLDivElement | undefined,
  data: LadderBucket[] | null,
  _name: string,
  _old: echarts.ECharts | null,
  setNew: (c: echarts.ECharts) => void
) {
  if (!el) return
  if (_old) _old.dispose()
  const chart = echarts.init(el)
  setNew(chart)
  const list = data || []
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: list.map(b => b.ladderName) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '项目数',
        type: 'bar',
        data: list.map(b => b.projectCount),
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '累计工作量',
        type: 'bar',
        data: list.map(b => b.totalWorkload),
        itemStyle: { color: '#409EFF' }
      }
    ],
    legend: { data: ['项目数', '累计工作量'], top: 0 }
  })
}

// ========== 操作 ==========
function handleQuery() { loadAll() }
function handleReset() {
  filters.value = {
    dimension: 'type',
    dateFrom: undefined,
    dateTo: undefined,
    sourceSystemId: undefined,
    targetSystemId: undefined,
    status: 'COMPLETED'
  }
  loadAll()
}

onMounted(() => {
  loadSystems()
  loadAll()
})

onBeforeUnmount(() => {
  typePieChart?.dispose()
  moduleBarChart?.dispose()
  dvBarChart?.dispose()
  ucBarChart?.dispose()
})
</script>

<style lang="scss" scoped>
.statistics-global-container {
  padding: 0;
}
.mb-16 { margin-bottom: 16px; }
.mr-8 { margin-right: 8px; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title { font-size: 18px; font-weight: 600; }
.subtitle { font-size: 13px; color: #909399; }
.card-title { font-size: 15px; font-weight: 500; }
.filter-form { margin-bottom: 0; }
.chart-container {
  height: 320px;
  width: 100%;
}
.chart-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
  padding: 0 8px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  flex-shrink: 0;
}
.legend-text { flex: 1; }
.legend-value { color: #606266; }
.legend-percent { color: #909399; min-width: 50px; text-align: right; }
.empty-hint { color: #909399; font-size: 13px; padding: 16px 0; text-align: center; }
.ranking-controls { display: flex; align-items: center; }
.value-cell { font-weight: 500; color: #409EFF; }
</style>
