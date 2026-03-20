<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报表系数配置</span>
        </div>
      </template>

      <!-- 说明提示 -->
      <el-alert
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #title>
          <span>提示：报表系数用于计算报表迁移的工作量。每个报表迁移所需的工作量（人天/个）。</span>
        </template>
      </el-alert>

      <!-- 配置表格 -->
      <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="configName" label="配置名称" min-width="200" />
        <el-table-column prop="configKey" label="配置键" width="250">
          <template #default="{ row }">
            <el-tag type="info">{{ row.configKey }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="configValue" label="配置值" width="150" align="center">
          <template #default="{ row }">
            <span class="config-value">{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑配置" width="500px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="配置名称">
          <span class="form-text">{{ formData.configName }}</span>
        </el-form-item>
        <el-form-item label="配置键">
          <span class="form-text">{{ formData.configKey }}</span>
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input-number
            v-model="formData.configValue"
            :min="0"
            :max="999.99"
            :precision="2"
            :step="0.1"
            style="width: 100%"
          />
          <span class="input-hint">人天/个</span>
        </el-form-item>
        <el-form-item label="描述">
          <span class="form-text">{{ formData.description }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import type { ReportConfigVO } from '@/api/config/reportConfig'
import {
  listAllReportConfigs,
  updateReportConfig
} from '@/api/config/reportConfig'

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false)

// 表格数据
const tableData = ref<ReportConfigVO[]>([])

// 弹窗控制
const dialogVisible = ref(false)

// 表单数据
const formData = reactive({
  id: null as number | null,
  configKey: '',
  configValue: 0,
  configName: '',
  description: ''
})

// 表单验证规则
const formRules: FormRules = {
  configValue: [
    { required: true, message: '请输入配置值', trigger: 'blur' }
  ]
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await listAllReportConfigs()
    tableData.value = res
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 编辑
function handleEdit(row: ReportConfigVO) {
  formData.id = row.id
  formData.configKey = row.configKey
  formData.configValue = parseFloat(row.configValue) || 0
  formData.configName = row.configName
  formData.description = row.description || ''
  dialogVisible.value = true
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

    await updateReportConfig({
      id: formData.id!,
      configKey: formData.configKey,
      configValue: formData.configValue.toString()
    })

    ElMessage.success('保存成功')
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

.config-value {
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
}

.form-text {
  color: #606266;
}

.input-hint {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
