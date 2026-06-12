<template>
  <el-dialog
    v-model="visible"
    title="导出评估报告"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form label-width="100px">
      <!-- 导出格式 -->
      <el-form-item label="导出格式" required>
        <el-radio-group v-model="exportForm.format">
          <el-radio value="EXCEL">Excel (.xlsx)</el-radio>
          <el-radio value="PDF">PDF (.pdf)</el-radio>
          <el-radio value="WORD">Word (.docx)</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 导出内容 -->
      <el-form-item label="导出内容">
        <el-checkbox-group v-model="selectedSections">
          <el-checkbox :value="true">项目基本信息</el-checkbox>
          <el-checkbox :value="true">评估指标汇总</el-checkbox>
          <el-checkbox :value="true">工作量评估明细</el-checkbox>
          <el-checkbox :value="true">可视化图表</el-checkbox>
          <el-checkbox :value="true">风险提示与建议</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <!-- 导出说明 -->
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          文件命名规则：{客户名称}_{项目名称}_工作量评估报告_{日期}.{格式}
        </template>
      </el-alert>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="exporting" @click="handleExport">确认导出</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { exportToExcel, exportToPdf, exportToWord, downloadFile } from '@/api/export/export'
import type { ExportFormat, ExportSection } from '@/api/export/export'

// Props
const props = defineProps<{
  modelValue: boolean
  projectId: number
  projectName?: string
  customerName?: string
}>()

// Emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

// 状态
const visible = ref(props.modelValue)
const exporting = ref(false)
const exportForm = ref({
  format: 'EXCEL' as ExportFormat
})

// 选中的章节
const selectedSections = ref([true, true, true, true, true])

// 监听props变化
watch(() => props.modelValue, (val) => {
  visible.value = val
})

// 监听visible变化
watch(() => visible.value, (val) => {
  emit('update:modelValue', val)
})

// 关闭弹窗
function handleClose() {
  visible.value = false
  resetForm()
}

// 重置表单
function resetForm() {
  exportForm.value.format = 'EXCEL'
  selectedSections.value = [true, true, true, true, true]
}

// 构建导出章节
function buildSections(): ExportSection {
  return {
    projectInfo: selectedSections.value[0],
    evaluationSummary: selectedSections.value[1],
    workloadDetail: selectedSections.value[2],
    charts: selectedSections.value[3],
    riskWarnings: selectedSections.value[4]
  }
}

// 导出
async function handleExport() {
  exporting.value = true

  try {
    const sections = buildSections()
    let blob: Blob
    let fileName: string

    const date = new Date().toISOString().slice(0, 10).replace(/-/g, '')
    const customer = props.customerName || '客户'
    const project = props.projectName || '项目'
    const safeCustomer = customer.replace(/[\\/:*?"<>|]/g, '_')
    const safeProject = project.replace(/[\\/:*?"<>|]/g, '_')

    switch (exportForm.value.format) {
      case 'EXCEL':
        blob = await exportToExcel(props.projectId, sections) as unknown as Blob
        fileName = `${safeCustomer}_${safeProject}_工作量评估报告_${date}.xlsx`
        break
      case 'PDF':
        blob = await exportToPdf(props.projectId, sections) as unknown as Blob
        fileName = `${safeCustomer}_${safeProject}_工作量评估报告_${date}.pdf`
        break
      case 'WORD':
        blob = await exportToWord(props.projectId, sections) as unknown as Blob
        fileName = `${safeCustomer}_${safeProject}_工作量评估报告_${date}.docx`
        break
      default:
        ElMessage.error('不支持的导出格式')
        return
    }

    downloadFile(blob, fileName)
    ElMessage.success('导出成功')

    handleClose()
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped lang="scss">
.el-form {
  .el-checkbox-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

.el-alert {
  margin-top: 16px;
}
</style>
