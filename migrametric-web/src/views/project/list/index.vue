<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目列表</span>
          <el-button type="primary" data-testid="btn-create-project" @click="router.push('/project/create')">
            <el-icon><Plus /></el-icon>
            创建项目
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="项目名称">
          <el-input v-model="searchForm.projectName" data-testid="search-project-name" placeholder="请输入项目名称" clearable />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" data-testid="search-customer-name" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="searchForm.status" data-testid="search-status" placeholder="请选择" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" data-testid="btn-search" @click="handleSearch">查询</el-button>
          <el-button data-testid="btn-reset" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableData" data-testid="table-project" stripe border style="width: 100%">
        <template #empty>
          <EmptyState description="暂无项目，请先创建项目">
            <el-button type="primary" @click="router.push('/project/create')">
              <el-icon><Plus /></el-icon>
              创建项目
            </el-button>
          </EmptyState>
        </template>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="projectName" label="项目名称" min-width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.projectName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column label="源系统 → 目标系统" width="220">
          <template #default="{ row }">
            {{ row.sourceSystemName || '-' }} → {{ row.targetSystemName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="projectLeader" label="负责人" width="100" />
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="evaluationDate" label="评估日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link data-testid="btn-view-row" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 'DRAFT'" type="warning" link data-testid="btn-edit-row" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'DRAFT'" type="danger" link data-testid="btn-delete-row" @click="handleDelete(row)">删除</el-button>
            <el-button type="info" link data-testid="btn-copy-row" @click="handleCopy(row)">复制</el-button>
            <el-button v-if="row.status === 'COMPLETED'" type="success" link data-testid="btn-archive-row" @click="handleArchive(row)">归档</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import EmptyState from '@/components/common/EmptyState.vue'
import type { ProjectVO, ProjectQuery } from '@/api/project/projects'
import { queryProjectPage, deleteProject, copyProject, archiveProject } from '@/api/project/projects'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  projectName: '',
  customerName: '',
  status: ''
})

// 分页配置
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref<ProjectVO[]>([])

// 获取状态类型
function getStatusType(status: string): 'success' | 'primary' | 'warning' | 'info' | 'danger' {
  const map: Record<string, 'success' | 'primary' | 'warning' | 'info' | 'danger'> = {
    DRAFT: 'info',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    ARCHIVED: 'warning'
  }
  return map[status] || 'info'
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params: ProjectQuery = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      projectName: searchForm.projectName || undefined,
      customerName: searchForm.customerName || undefined,
      status: searchForm.status as ProjectQuery['status'] || undefined
    }
    const res = await queryProjectPage(params)
    tableData.value = res.records
    pagination.total = res.total
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

// 重置
function handleReset() {
  searchForm.projectName = ''
  searchForm.customerName = ''
  searchForm.status = ''
  handleSearch()
}

// 分页大小改变
function handleSizeChange() {
  handleSearch()
}

// 页码改变
function handlePageChange() {
  loadData()
}

// 查看
function handleView(row: ProjectVO) {
  router.push(`/project/detail/${row.id}`)
}

// 编辑
function handleEdit(row: ProjectVO) {
  router.push(`/project/edit/${row.id}`)
}

// 删除
async function handleDelete(row: ProjectVO) {
  try {
    await ElMessageBox.confirm('确定要删除该项目吗？删除后不可恢复', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteProject(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    // 用户取消或删除失败
  }
}

// 复制
async function handleCopy(row: ProjectVO) {
  try {
    await ElMessageBox.confirm('确定要复制该项目吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const _newId = await copyProject(row.id)
    ElMessage.success('复制成功')
    loadData()
  } catch (error) {
    // 用户取消或复制失败
  }
}

// 归档
async function handleArchive(row: ProjectVO) {
  try {
    await ElMessageBox.confirm('确定要归档该项目吗？归档后项目将变为只读状态', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await archiveProject(row.id)
    ElMessage.success('归档成功')
    loadData()
  } catch (error) {
    // 用户取消或归档失败
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
