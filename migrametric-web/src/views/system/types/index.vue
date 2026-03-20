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
            <el-option label="源系统" value="SOURCE" />
            <el-option label="目标系统" value="TARGET" />
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
      <el-table :data="tableData" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="systemName" label="系统名称" />
        <el-table-column prop="systemCategory" label="系统类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.systemCategory === 'SOURCE' ? 'success' : 'warning'">
              {{ row.systemCategory === 'SOURCE' ? '源系统' : '目标系统' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleToggleStatus(row)">
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
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="系统名称" prop="systemName">
          <el-input v-model="formData.systemName" placeholder="请输入系统名称" />
        </el-form-item>
        <el-form-item label="系统类型" prop="systemCategory">
          <el-radio-group v-model="formData.systemCategory">
            <el-radio label="SOURCE">源系统</el-radio>
            <el-radio label="TARGET">目标系统</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'

const formRef = ref<FormInstance>()

// 搜索表单
const searchForm = reactive({
  systemName: '',
  systemCategory: '',
  status: null as number | null
})

// 分页配置
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref([
  {
    id: 1,
    systemName: 'SAP',
    systemCategory: 'SOURCE',
    description: 'SAP ERP系统',
    status: 1,
    createTime: '2026-03-19 10:00:00'
  },
  {
    id: 2,
    systemName: '用友',
    systemCategory: 'TARGET',
    description: '用友U8系统',
    status: 1,
    createTime: '2026-03-19 10:00:00'
  }
])

// 弹窗控制
const dialogVisible = ref(false)
const dialogTitle = ref('新增系统类型')

// 表单数据
const formData = reactive({
  id: null as number | null,
  systemName: '',
  systemCategory: 'SOURCE',
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

// 搜索
function handleSearch() {
  pagination.pageNum = 1
  // TODO: 调用API获取数据
}

// 重置
function handleReset() {
  searchForm.systemName = ''
  searchForm.systemCategory = ''
  searchForm.status = null
  handleSearch()
}

// 分页大小改变
function handleSizeChange() {
  handleSearch()
}

// 页码改变
function handlePageChange() {
  handleSearch()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增系统类型'
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: { id: number }) {
  dialogTitle.value = '编辑系统类型'
  // TODO: 填充表单数据
  dialogVisible.value = true
}

// 切换状态
function handleToggleStatus(row: { id: number; status: number }) {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}该系统类型吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success(`${action}成功`)
    handleSearch()
  })
}

// 删除
function handleDelete(row: { id: number }) {
  ElMessageBox.confirm('确定要删除该系统类型吗？删除后不可恢复', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    handleSearch()
  })
}

// 弹窗关闭
function handleDialogClose() {
  formRef.value?.resetFields()
}

// 提交表单
function handleSubmit() {
  formRef.value?.validate((valid) => {
    if (valid) {
      // TODO: 调用API保存数据
      ElMessage.success('保存成功')
      dialogVisible.value = false
      handleSearch()
    }
  })
}
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
