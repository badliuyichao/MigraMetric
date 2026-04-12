<template>
  <div class="statistics-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
            <span class="title">工作量统计图表</span>
          </div>
          <div class="right">
            <el-button type="primary" :icon="Download" @click="handleExport">导出报告</el-button>
          </div>
        </div>
      </template>

      <!-- 总工作量展示 -->
      <div class="total-workload-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="总工作量" :value="statistics.totalWorkload" suffix="人天">
              <template #prefix>
                <el-icon><Calendar /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="预估工期" :value="statistics.estimatedMonths" suffix="人月">
              <template #prefix>
                <el-icon><Clock /></el-icon>
              </template>
              <template #suffix>
                <span class="hint">(按22人天/人月)</span>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="已选模块数" :value="statistics.evaluationOverview?.moduleCount || 0" suffix="个">
              <template #prefix>
                <el-icon><Box /></el-icon>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="charts-section">
        <!-- 工作量类型分布饼图 -->
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <span class="card-title">工作量类型分布</span>
            </template>
            <div ref="pieChartRef" class="chart-container"></div>
            <div class="chart-legend">
              <div v-for="item in statistics.workloadTypeDistribution" :key="item.type" class="legend-item">
                <span class="legend-color" :style="{ backgroundColor: getPieColor(item.type) }"></span>
                <span class="legend-text">{{ item.type }}</span>
                <span class="legend-value">{{ item.workload }} 人天</span>
                <span class="legend-percent">{{ item.percentage }}%</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 模块工作量对比柱状图 -->
        <el-col :span="16">
          <el-card shadow="hover">
            <template #header>
              <span class="card-title">模块工作量对比</span>
            </template>
            <div ref="barChartRef" class="chart-container bar-chart"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 雷达图和风险提示 -->
      <el-row :gutter="20" class="charts-section">
        <!-- 多维度评估雷达图 -->
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span class="card-title">多维度评估指标</span>
            </template>
            <div ref="radarChartRef" class="chart-container"></div>
            <div class="indicators-table">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="数据量">
                  {{ statistics.multiDimensionIndicators?.dataVolumeActual }} ({{ statistics.multiDimensionIndicators?.dataVolumeLadder }})
                </el-descriptions-item>
                <el-descriptions-item label="用户数">
                  {{ statistics.multiDimensionIndicators?.userCountActual }} ({{ statistics.multiDimensionIndicators?.userCountLadder }})
                </el-descriptions-item>
                <el-descriptions-item label="模块数">
                  {{ statistics.multiDimensionIndicators?.moduleCountActual }} 个
                </el-descriptions-item>
                <el-descriptions-item label="报表数">
                  {{ statistics.multiDimensionIndicators?.reportCountActual }} 个
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>
        </el-col>

        <!-- 风险提示 -->
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <span class="card-title">风险提示与建议</span>
            </template>
            <div v-if="statistics.riskWarnings && statistics.riskWarnings.length > 0" class="risk-list">
              <el-alert
                v-for="(warning, index) in statistics.riskWarnings"
                :key="index"
                :title="warning.description"
                :type="getRiskType(warning.level)"
                :description="warning.suggestion"
                :closable="false"
                show-icon
                class="risk-item"
              />
            </div>
            <el-empty v-else description="暂无风险提示" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 评估指标概览 -->
      <el-row :gutter="20" class="overview-section">
        <el-col :span="24">
          <el-card shadow="hover">
            <template #header>
              <span class="card-title">评估指标概览</span>
            </template>
            <el-descriptions :column="4" border>
              <el-descriptions-item label="已选模块数">
                {{ statistics.evaluationOverview?.moduleCount || 0 }} 个
              </el-descriptions-item>
              <el-descriptions-item label="数据量">
                {{ statistics.evaluationOverview?.dataVolume || 0 }} 万条
                <el-tag size="small" type="info">{{ statistics.evaluationOverview?.dataVolumeLadder || '未知' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="用户数">
                {{ statistics.evaluationOverview?.userCount || 0 }} 人
                <el-tag size="small" type="info">{{ statistics.evaluationOverview?.userCountLadder || '未知' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="报表数量">
                {{ statistics.evaluationOverview?.reportCount || 0 }} 个
              </el-descriptions-item>
              <el-descriptions-item label="客开情况">
                <el-tag v-if="statistics.evaluationOverview?.hasCustomDev" type="warning">有客开</el-tag>
                <el-tag v-else type="success">无客开</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 导出弹窗 -->
    <ExportDialog
      v-model="exportDialogVisible"
      :project-id="Number(route.params.id)"
      :project-name="statistics.evaluationOverview?.moduleCount ? '评估项目' : ''"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Download, Calendar, Clock, Box } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getStatistics } from '@/api/statistics/statistics'
import type { StatisticsResultVO } from '@/api/statistics/statistics'
import ExportDialog from '@/components/export/ExportDialog.vue'

// 路由
const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(false)
const statistics = ref<StatisticsResultVO>({
  projectId: 0,
  totalWorkload: 0,
  estimatedMonths: 0,
  workloadTypeDistribution: [],
  moduleWorkloads: [],
  multiDimensionIndicators: {
    dataVolumeValue: 0,
    dataVolumeActual: '',
    dataVolumeLadder: '',
    userCountValue: 0,
    userCountActual: '',
    userCountLadder: '',
    moduleCountValue: 0,
    moduleCountActual: 0,
    reportCountValue: 0,
    reportCountActual: 0,
    customDevValue: 0,
    hasCustomDev: false,
    customDevWorkload: null
  },
  riskWarnings: [],
  evaluationOverview: {
    moduleCount: 0,
    dataVolume: null,
    dataVolumeLadder: null,
    userCount: null,
    userCountLadder: null,
    reportCount: null,
    hasCustomDev: false
  }
})

// 图表实例
let pieChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null
let radarChart: echarts.ECharts | null = null

// 图表容器
const pieChartRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()
const radarChartRef = ref<HTMLElement>()

// 饼图颜色
const pieColors: Record<string, string> = {
  '核心迁移': '#5470c6',
  '报表迁移': '#91cc75',
  '客开定制': '#fac858'
}

// 获取饼图颜色
function getPieColor(type: string): string {
  return pieColors[type] || '#999'
}

// 获取风险类型
function getRiskType(level: string): 'warning' | 'error' | 'info' {
  switch (level) {
    case '高':
      return 'error'
    case '中':
      return 'warning'
    default:
      return 'info'
  }
}

// 返回上一页
function goBack() {
  const projectId = route.params.id
  router.push(`/project/detail/${projectId}`)
}

// 导出报告
const exportDialogVisible = ref(false)

function handleExport() {
  exportDialogVisible.value = true
}

// 初始化饼图
function initPieChart() {
  if (!pieChartRef.value) return

  pieChart = echarts.init(pieChartRef.value)

  const data = statistics.value.workloadTypeDistribution.map(item => ({
    name: item.type,
    value: item.workload
  }))

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 人天 ({d}%)'
    },
    color: Object.values(pieColors),
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%'
        },
        data: data
      }
    ]
  }

  pieChart.setOption(option)
}

// 初始化柱状图
function initBarChart() {
  if (!barChartRef.value) return

  barChart = echarts.init(barChartRef.value)

  const data = statistics.value.moduleWorkloads.map(item => ({
    name: item.moduleName,
    value: item.workload
  }))

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const data = params[0]
        const moduleData = statistics.value.moduleWorkloads.find(m => m.moduleName === data.name)
        if (moduleData) {
          return `
            ${data.name}<br/>
            基础人天: ${moduleData.baseWorkload}<br/>
            加权系数: ${moduleData.weight}<br/>
            工作量: ${data.value} 人天
          `
        }
        return data.name + ': ' + data.value
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map(d => d.name),
      axisLabel: {
        rotate: 30,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '工作量（人天）',
      axisLabel: {
        formatter: '{value}'
      }
    },
    series: [
      {
        type: 'bar',
        data: data.map(d => d.value),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 1, color: '#5470c6' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: '60%'
      }
    ]
  }

  barChart.setOption(option)
}

// 初始化雷达图
function initRadarChart() {
  if (!radarChartRef.value) return

  radarChart = echarts.init(radarChartRef.value)

  const indicators = statistics.value.multiDimensionIndicators

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item'
    },
    radar: {
      indicator: [
        { name: '数据量', max: 100 },
        { name: '用户数', max: 100 },
        { name: '模块数', max: 100 },
        { name: '报表数', max: 100 },
        { name: '客开情况', max: 100 }
      ],
      shape: 'polygon',
      splitNumber: 4,
      axisName: {
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          color: '#e0e0e0'
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['#f8f8f8', '#fff']
        }
      }
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: [
              indicators.dataVolumeValue,
              indicators.userCountValue,
              indicators.moduleCountValue,
              indicators.reportCountValue,
              indicators.customDevValue
            ],
            name: '评估指标',
            areaStyle: {
              color: 'rgba(84, 112, 198, 0.3)'
            },
            lineStyle: {
              color: '#5470c6'
            },
            itemStyle: {
              color: '#5470c6'
            }
          }
        ]
      }
    ]
  }

  radarChart.setOption(option)
}

// 调整图表大小
function resizeCharts() {
  pieChart?.resize()
  barChart?.resize()
  radarChart?.resize()
}

// 加载统计数据
async function loadStatistics() {
  const projectId = Number(route.params.id)
  if (!projectId) {
    ElMessage.error('项目ID无效')
    router.push('/project/list')
    return
  }

  loading.value = true
  try {
    const data = await getStatistics(projectId)
    statistics.value = data

    // 等待DOM更新后初始化图表
    await nextTick()
    initPieChart()
    initBarChart()
    initRadarChart()
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 组件挂载
onMounted(() => {
  loadStatistics()
  window.addEventListener('resize', resizeCharts)
})

// 组件卸载
onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  pieChart?.dispose()
  barChart?.dispose()
  radarChart?.dispose()
})
</script>

<style scoped lang="scss">
.statistics-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .title {
    font-size: 18px;
    font-weight: 600;
  }
}

.total-workload-section {
  margin-bottom: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: #fff;

  :deep(.el-statistic__head) {
    color: rgba(255, 255, 255, 0.8);
  }

  :deep(.el-statistic__content) {
    color: #fff;
    font-size: 32px;
    font-weight: bold;
  }

  .hint {
    font-size: 12px;
    opacity: 0.8;
  }
}

.charts-section {
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  height: 300px;
  width: 100%;

  &.bar-chart {
    height: 350px;
  }
}

.chart-legend {
  margin-top: 16px;
  padding: 0 16px;

  .legend-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }
  }

  .legend-color {
    width: 16px;
    height: 16px;
    border-radius: 4px;
    margin-right: 8px;
  }

  .legend-text {
    flex: 1;
    font-weight: 500;
  }

  .legend-value {
    margin-right: 16px;
    font-weight: 600;
  }

  .legend-percent {
    color: #909399;
    min-width: 50px;
    text-align: right;
  }
}

.indicators-table {
  margin-top: 16px;
}

.risk-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.risk-item {
  margin-bottom: 0;
}

.overview-section {
  margin-bottom: 20px;
}
</style>
