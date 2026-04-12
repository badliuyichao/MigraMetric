<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户数阶梯配置</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增阶梯
          </el-button>
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
          <span>提示：用户数阶梯用于根据系统用户数量自动匹配工作量系数。请按从小到大的顺序排列，用户数上限为空表示无上限。</span>
        </template>
      </el-alert>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="ladderName" label="阶梯名称" width="150">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.ladderName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="countRangeText" label="用户数范围" width="200" />
        <el-table-column prop="weight" label="工作量系数" width="120" align="center">
          <template #default="{ row }">
            <span class="weight-value">{{ row.weight?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row, $index }">
            <el-button type="primary" link size="small" :disabled="$index === 0" @click="handleMoveUp(row, $index)">
              <el-icon><Top /></el-icon>上移
            </el-button>
            <el-button type="primary" link size="small" :disabled="$index === tableData.length - 1" @click="handleMoveDown(row, $index)">
              <el-icon><Bottom /></el-icon>下移
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="阶梯名称" prop="ladderName">
          <el-input v-model="formData.ladderName" placeholder="如：小规模、中规模、大规模" />
        </el-form-item>
        <el-form-item label="用户数下限" prop="minCount">
          <el-input-number
            v-model="formData.minCount"
            :min="0"
            :step="1"
            style="width: 100%"
          />
          <span class="input-suffix">人（含）</span>
        </el-form-item>
        <el-form-item label="用户数上限" prop="maxCount">
          <el-input-number
            v-model="formData.maxCount"
            :min="0"
            :step="1"
            style="width: 100%"
            placeholder="留空表示无上限"
          />
          <span class="input-suffix">人（不含）</span>
        </el-form-item>
        <el-form-item label="工作量系数" prop="weight">
          <el-input-number
            v-model="formData.weight"
            :min="0"
            :max="99.99"
            :precision="2"
            :step="0.1"
            style="width: 100%"
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
import type { UserCountLadderVO, UserCountLadderCreate } from '@/api/ladder/userCount'
import {
  listAllUserCountLadders,
  createUserCountLadder,
  updateUserCountLadder,
  deleteUserCountLadder,
  moveUpUserCountLadder,
  moveDownUserCountLadder
} from '@/api/ladder/userCount'

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false)

// 表格数据
const tableData = ref<UserCountLadderVO[]>([])

// 弹窗控制
const dialogVisible = ref(false)
const dialogTitle = ref('新增阶梯')

// 表单数据
const formData = reactive({
  id: null as number | null,
  ladderName: '',
  minCount: 0,
  maxCount: null as number | null,
  weight: 1.0
})

// 表单验证规则
const formRules: FormRules = {
  ladderName: [
    { required: true, message: '请输入阶梯名称', trigger: 'blur' },
    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
  ],
  minCount: [
    { required: true, message: '请输入用户数下限', trigger: 'blur' }
  ],
  weight: [
    { required: true, message: '请输入工作量系数', trigger: 'blur' }
  ]
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await listAllUserCountLadders()
    tableData.value = res
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增阶梯'
  formData.id = null
  formData.ladderName = ''
  formData.minCount = 0
  formData.maxCount = null
  formData.weight = 1.0
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: UserCountLadderVO) {
  dialogTitle.value = '编辑阶梯'
  formData.id = row.id
  formData.ladderName = row.ladderName
  formData.minCount = row.minCount
  formData.maxCount = row.maxCount || null
  formData.weight = row.weight
  dialogVisible.value = true
}

// 上移
async function handleMoveUp(row: UserCountLadderVO, index: number) {
  try {
    await ElMessageBox.confirm('确定要将该阶梯上移吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await moveUpUserCountLadder(row.id)
    ElMessage.success('上移成功')
    loadData()
  } catch {
    // 用户取消
  }
}

// 下移
async function handleMoveDown(row: UserCountLadderVO, index: number) {
  try {
    await ElMessageBox.confirm('确定要将该阶梯下移吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await moveDownUserCountLadder(row.id)
    ElMessage.success('下移成功')
    loadData()
  } catch {
    // 用户取消
  }
}

// 删除
async function handleDelete(row: UserCountLadderVO) {
  try {
    await ElMessageBox.confirm('确定要删除该阶梯吗？删除后不可恢复', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteUserCountLadder(row.id)
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

    const data: UserCountLadderCreate = {
      ladderName: formData.ladderName,
      minCount: formData.minCount,
      maxCount: formData.maxCount || undefined,
      weight: formData.weight
    }

    if (formData.id) {
      // 编辑
      await updateUserCountLadder(formData.id, { ...data, id: formData.id, sortOrder: 0 })
      ElMessage.success('更新成功')
    } else {
      // 新增
      await createUserCountLadder(data)
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

.weight-value {
  font-weight: bold;
  color: #409eff;
}

.input-suffix {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
