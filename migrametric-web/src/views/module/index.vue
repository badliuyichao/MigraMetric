<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>模块库管理</span>
          <el-button type="primary" @click="handleAdd" data-testid="btn-add-module">
            <el-icon><Plus /></el-icon>
            新增模块
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="模块名称">
          <el-input v-model="searchForm.moduleName" data-testid="search-module-name" placeholder="请输入模块名称" clearable />
        </el-form-item>
        <el-form-item label="所属系统">
          <el-select v-model="searchForm.systemId" data-testid="search-system-id" placeholder="请选择" clearable filterable>
            <el-option
              v-for="system in systemList"
              :key="system.id"
              :label="system.systemName"
              :value="system.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模块分类">
          <el-select v-model="searchForm.category" data-testid="search-category" placeholder="请选择" clearable>
            <el-option
              v-for="cat in categoryList"
              :key="cat"
              :label="cat"
              :value="cat"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" data-testid="search-status" placeholder="请选择" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" data-testid="btn-search">查询</el-button>
          <el-button @click="handleReset" data-testid="btn-reset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableData" data-testid="table-module" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="moduleName" label="模块名称" min-width="120" />
        <el-table-column prop="systemName" label="所属系统" width="120">
          <template #default="{ row }">
            <el-tag :type="row.systemCategory === 1 ? 'success' : 'warning'">
              {{ row.systemName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="模块分类" width="120" />
        <el-table-column prop="baseWorkload" label="基础工作量(人天)" width="130" align="right" />
        <el-table-column prop="defaultWeight" label="加权系数" width="100" align="right">
          <template #default="{ row }">
            {{ row.defaultWeight?.toFixed(2) }}
          </template>
        </el-table-column>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="模块名称" prop="moduleName">
          <el-input v-model="formData.moduleName" data-testid="form-module-name" placeholder="请输入模块名称" />
        </el-form-item>
        <el-form-item label="所属系统" prop="systemId">
          <el-select v-model="formData.systemId" data-testid="form-system-id" placeholder="请选择所属系统" filterable>
            <el-option
              v-for="system in enabledSystemList"
              :key="system.id"
              :label="system.systemName"
              :value="system.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模块分类" prop="category">
          <el-input v-model="formData.category" data-testid="form-category" placeholder="请输入模块分类，如：财务模块" />
        </el-form-item>
        <el-form-item label="基础工作量" prop="baseWorkload">
          <el-input-number
            v-model="formData.baseWorkload"
            data-testid="form-base-workload"
            :min="0"
            :precision="2"
            :step="1"
            style="width: 100%"
          />
          <span class="input-suffix">人天</span>
        </el-form-item>
        <el-form-item label="加权系数" prop="defaultWeight">
          <el-input-number
            v-model="formData.defaultWeight"
            data-testid="form-default-weight"
            :min="0"
            :max="999.99"
            :precision="2"
            :step="0.1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            data-testid="form-description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" data-testid="btn-cancel">取消</el-button>
        <el-button type="primary" :loading="submitLoading" data-testid="btn-submit" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import type { ModuleVO, ModuleQuery, ModuleCreate } from '@/api/module/modules'
import {
  queryModulePage,
  listModuleCategories,
  createModule,
  updateModule,
  deleteModule,
  enableModule,
  disableModule
} from '@/api/module/modules'
import type { SystemTypeVO } from '@/api/system/types'
import { listEnabledSystemTypes } from '@/api/system/types'

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false)

// 系统列表
const systemList = ref<SystemTypeVO[]>([])
const enabledSystemList = computed(() => systemList.value.filter(s => s.status === 1))

// 分类列表
const categoryList = ref<string[]>([])

// 搜索表单
const searchForm = reactive({
  moduleName: '',
  systemId: null as number | null,
  category: '',
  status: null as number | null
})

// 分页配置
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref<ModuleVO[]>([])

// 弹窗控制
const dialogVisible = ref(false)
const dialogTitle = ref('新增模块')

// 表单数据
const formData = reactive({
  id: null as number | null,
  moduleName: '',
  systemId: null as number | null,
  category: '',
  baseWorkload: 0,
  defaultWeight: 1.0,
  description: ''
})

// 表单验证规则
const formRules: FormRules = {
  moduleName: [
    { required: true, message: '请输入模块名称', trigger: 'blur' },
    { max: 100, message: '长度不能超过100个字符', trigger: 'blur' }
  ],
  systemId: [{ required: true, message: '请选择所属系统', trigger: 'change' }],
  baseWorkload: [{ required: true, message: '请输入基础工作量', trigger: 'blur' }],
  defaultWeight: [{ required: true, message: '请输入加权系数', trigger: 'blur' }]
}

// 加载系统列表
async function loadSystemList() {
  try {
    const res = await listEnabledSystemTypes()
    systemList.value = res
  } catch {
    ElMessage.error('加载系统列表失败')
  }
}

// 加载分类列表
async function loadCategoryList() {
  try {
    const res = await listModuleCategories()
    categoryList.value = res
  } catch {
    ElMessage.error('加载分类列表失败')
  }
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params: ModuleQuery = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      moduleName: searchForm.moduleName || undefined,
      systemId: searchForm.systemId ?? undefined,
      category: searchForm.category || undefined,
      status: searchForm.status ?? undefined
    }
    const res = await queryModulePage(params)
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
  searchForm.moduleName = ''
  searchForm.systemId = null
  searchForm.category = ''
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
  dialogTitle.value = '新增模块'
  formData.id = null
  formData.moduleName = ''
  formData.systemId = null
  formData.category = ''
  formData.baseWorkload = 0
  formData.defaultWeight = 1.0
  formData.description = ''
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: ModuleVO) {
  dialogTitle.value = '编辑模块'
  formData.id = row.id
  formData.moduleName = row.moduleName
  formData.systemId = row.systemId
  formData.category = row.category || ''
  formData.baseWorkload = row.baseWorkload
  formData.defaultWeight = row.defaultWeight
  formData.description = row.description || ''
  dialogVisible.value = true
}

// 切换状态
async function handleToggleStatus(row: ModuleVO) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该模块吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    if (row.status === 1) {
      await disableModule(row.id)
    } else {
      await enableModule(row.id)
    }
    ElMessage.success(`${action}成功`)
    loadData()
  } catch {
    // 用户取消
  }
}

// 删除
async function handleDelete(row: ModuleVO) {
  try {
    await ElMessageBox.confirm('确定要删除该模块吗？删除后不可恢复', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteModule(row.id)
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

    const data: ModuleCreate = {
      moduleName: formData.moduleName,
      systemId: formData.systemId!,
      category: formData.category || undefined,
      baseWorkload: formData.baseWorkload,
      defaultWeight: formData.defaultWeight,
      description: formData.description
    }

    if (formData.id) {
      // 编辑
      await updateModule(formData.id, { ...data, id: formData.id })
      ElMessage.success('更新成功')
    } else {
      // 新增
      await createModule(data)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    loadData()
    loadCategoryList()
  } catch {
    // 表单验证失败或其他错误
  } finally {
    submitLoading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadSystemList()
  loadCategoryList()
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

.input-suffix {
  margin-left: 8px;
  color: #909399;
}
</style>
