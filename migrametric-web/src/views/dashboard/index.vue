<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon project-icon">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.projectCount }}</div>
              <div class="stat-label">项目总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon evaluation-icon">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.evaluationCount }}</div>
              <div class="stat-label">评估次数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon workload-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalWorkload }}</div>
              <div class="stat-label">总工作量(人天)</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近项目</span>
              <el-button type="primary" link @click="router.push('/project/list')">
                查看更多
              </el-button>
            </div>
          </template>
          <el-table :data="recentProjects" style="width: 100%">
            <el-table-column prop="projectName" label="项目名称" />
            <el-table-column prop="customerName" label="客户名称" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="router.push('/project/create')">
              <el-icon><Plus /></el-icon>
              创建项目
            </el-button>
            <el-button type="success" @click="router.push('/project/list')">
              <el-icon><Folder /></el-icon>
              管理项目
            </el-button>
            <el-button v-if="userStore.isAdmin" type="warning" @click="router.push('/system/types')">
              <el-icon><Setting /></el-icon>
              系统配置
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 统计数据
const stats = reactive({
  projectCount: 0,
  evaluationCount: 0,
  totalWorkload: 0,
  userCount: 0
})

// 最近项目
const recentProjects = ref([
  {
    projectName: '示例项目1',
    customerName: 'XX公司',
    status: '已完成',
    createTime: '2026-03-19 10:00:00'
  },
  {
    projectName: '示例项目2',
    customerName: 'YY公司',
    status: '进行中',
    createTime: '2026-03-18 14:30:00'
  }
])

// 获取状态类型
function getStatusType(status: string): 'success' | 'primary' | 'warning' | 'info' | 'danger' {
  const map: Record<string, 'success' | 'primary' | 'warning' | 'info' | 'danger'> = {
    草稿: 'info',
    进行中: 'primary',
    已完成: 'success',
    已归档: 'warning'
  }
  return map[status] || 'info'
}

// 获取状态文本
function getStatusText(status: string) {
  return status
}

// 初始化数据
onMounted(() => {
  // TODO: 从API获取数据
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 0;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .stat-icon {
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    font-size: 28px;
    color: #fff;

    &.project-icon {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.evaluation-icon {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    &.workload-icon {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }

    &.user-icon {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }
  }

  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 5px;
    }
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;

  .el-button {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
