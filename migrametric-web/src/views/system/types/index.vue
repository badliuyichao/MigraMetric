<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统类型管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增系统类型
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="系统名称">
          <el-input v-model="searchForm.systemName" placeholder="请输入系统名称" clearable />
        </el-form-item>
        <el-form-item label="系统类型">
          <el-select v-model="searchForm.systemCategory" placeholder="请选择" clearable>
            <el-option label="源系统" :value="1" />
            <el-option label="目标系统" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
        <template #empty>
          <EmptyState description="暂无系统类型，请先添加">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增系统类型
            </el-button>
          </EmptyState>
        </template>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="systemName" label="系统名称" />
        <el-table-column prop="systemCategoryText" label="系统类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.systemCategory === 1 ? 'success' : 'warning'">
              {{ row.systemCategoryText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="statusText" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="系统名称" prop="systemName">
          <el-input v-model="formData.systemName" placeholder="请输入系统名称" />
        </el-form-item>
        <el-form-item label="系统类型" prop="systemCategory">
          <el-radio-group v-model="formData.systemCategory">
            <el-radio :label="1">源系统</el-radio>
            <el-radio :label="2">目标系统</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import EmptyState from '@/components/common/EmptyState.vue'
import type { SystemTypeVO, SystemTypeQuery, SystemTypeCreate } from '@/api/system/types'
import {
  querySystemTypePage,
  createSystemType,
  updateSystemType,
  deleteSystemType,
  enableSystemType,
  disableSystemType
} from '@/api/system/types'

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  systemName: '',
  systemCategory: null as number | null,
  status: null as number | null
})

// 分页配置
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref<SystemTypeVO[]>([])

// 弹窗控制
const dialogVisible = ref(false)
const dialogTitle = ref('新增系统类型')

// 表单数据
const formData = reactive({
  id: null as number | null,
  systemName: '',
  systemCategory: 1,
  description: ''
})

// 表单验证规则
const formRules: FormRules = {
  systemName: [
    { required: true, message: '请输入系统名称', trigger: 'blur' },
    { max: 100, message: '长度不能超过100个字符', trigger: 'blur' }
  ],
  systemCategory: [{ required: true, message: '请选择系统类型', trigger: 'change' }]
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params: SystemTypeQuery = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      systemName: searchForm.systemName || undefined,
      systemCategory: searchForm.systemCategory ?? undefined,
      status: searchForm.status ?? undefined
    }
    const res = await querySystemTypePage(params)
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
  searchForm.systemName = ''
  searchForm.systemCategory = null
  searchForm.status = null
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

// 新增
function handleAdd() {
  dialogTitle.value = '新增系统类型'
  formData.id = null
  formData.systemName = ''
  formData.systemCategory = 1
  formData.description = ''
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: SystemTypeVO) {
  dialogTitle.value = '编辑系统类型'
  formData.id = row.id
  formData.systemName = row.systemName
  formData.systemCategory = row.systemCategory
  formData.description = row.description || ''
  dialogVisible.value = true
}

// 切换状态
async function handleToggleStatus(row: SystemTypeVO) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该系统类型吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    if (row.status === 1) {
      await disableSystemType(row.id)
    } else {
      await enableSystemType(row.id)
    }
    ElMessage.success(`${action}成功`)
    loadData()
  } catch {
    // 用户取消
  }
}

// 删除
async function handleDelete(row: SystemTypeVO) {
  try {
    await ElMessageBox.confirm('确定要删除该系统类型吗？删除后不可恢复', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSystemType(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 用户取消
  }
}

// 弹窗关闭
function handleDialogClose() {
  formRef.value?.resetFields()
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    const data: SystemTypeCreate = {
      systemName: formData.systemName,
      systemCategory: formData.systemCategory,
      description: formData.description
    }

    if (formData.id) {
      // 编辑
      await updateSystemType(formData.id, { ...data, id: formData.id })
      ElMessage.success('更新成功')
    } else {
      // 新增
      await createSystemType(data)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    loadData()
  } catch {
    // 表单验证失败或其他错误
  } finally {
    submitLoading.value = false
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
