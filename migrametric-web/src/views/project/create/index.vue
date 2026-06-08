<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创建项目</span>
          <el-button @click="router.push('/project/list')">返回列表</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="project-form"
      >
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="formData.projectName" placeholder="请输入项目名称" />
        </el-form-item>

        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="formData.customerName" placeholder="请输入客户名称" />
        </el-form-item>

        <el-form-item label="源系统" prop="sourceSystemId">
          <el-select
            v-model="formData.sourceSystemId"
            data-testid="select-source-system"
            placeholder="请选择源系统"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="system in sourceSystemList"
              :key="system.id"
              :label="system.systemName"
              :value="system.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="目标系统" prop="targetSystemId">
          <el-select
            v-model="formData.targetSystemId"
            data-testid="select-target-system"
            placeholder="请选择目标系统"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="system in targetSystemList"
              :key="system.id"
              :label="system.systemName"
              :value="system.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="项目负责人" prop="projectLeader">
          <el-input v-model="formData.projectLeader" placeholder="请输入项目负责人" />
        </el-form-item>

        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="formData.contact" placeholder="请输入联系方式" />
        </el-form-item>

        <el-form-item label="评估日期" prop="evaluationDate">
          <el-date-picker
            v-model="formData.evaluationDate"
            type="date"
            placeholder="请选择评估日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入项目描述"
          />
        </el-form-item>

        <el-form-item>
          <el-button @click="handleCancel">取消</el-button>
          <el-button data-testid="btn-save-project" type="primary" :loading="submitLoading" @click="handleSave">保存</el-button>
          <el-button data-testid="btn-save-and-evaluate" type="success" :loading="submitLoading" @click="handleSaveAndEvaluate">
            保存并开始评估
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import type { SystemTypeVO } from '@/api/system/types'
import { listEnabledSystemTypes } from '@/api/system/types'
import { createProject, type ProjectCreate } from '@/api/project/projects'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const systemList = ref<SystemTypeVO[]>([])

const formData = reactive({
  projectName: '',
  customerName: '',
  sourceSystemId: null as number | null,
  targetSystemId: null as number | null,
  projectLeader: '',
  contact: '',
  evaluationDate: '',
  description: ''
})

// 源系统列表（源系统类别=1）
const sourceSystemList = computed(() =>
  systemList.value.filter(s => s.systemCategory === 1)
)

// 目标系统列表（目标系统类别=2）
const targetSystemList = computed(() =>
  systemList.value.filter(s => s.systemCategory === 2)
)

const formRules: FormRules = {
  projectName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { max: 200, message: '长度不能超过200个字符', trigger: 'blur' }
  ],
  customerName: [
    { required: true, message: '请输入客户名称', trigger: 'blur' },
    { max: 200, message: '长度不能超过200个字符', trigger: 'blur' }
  ],
  sourceSystemId: [
    { required: true, message: '请选择源系统', trigger: 'change' }
  ],
  targetSystemId: [
    { required: true, message: '请选择目标系统', trigger: 'change' },
    {
      validator: (_rule: unknown, value: number, callback) => {
        if (value && value === formData.sourceSystemId) {
          callback(new Error('目标系统不能与源系统相同'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  evaluationDate: [
    { required: true, message: '请选择评估日期', trigger: 'change' }
  ]
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

function handleCancel() {
  router.push('/project/list')
}

async function handleSave() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    const data: ProjectCreate = {
      projectName: formData.projectName,
      customerName: formData.customerName,
      sourceSystemId: formData.sourceSystemId!,
      targetSystemId: formData.targetSystemId!,
      projectLeader: formData.projectLeader || undefined,
      contact: formData.contact || undefined,
      description: formData.description || undefined,
      evaluationDate: formData.evaluationDate
    }

    await createProject(data)
    ElMessage.success('保存成功')
    router.push('/project/list')
  } catch {
    // 表单验证失败或其他错误
  } finally {
    submitLoading.value = false
  }
}

async function handleSaveAndEvaluate() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    const data: ProjectCreate = {
      projectName: formData.projectName,
      customerName: formData.customerName,
      sourceSystemId: formData.sourceSystemId!,
      targetSystemId: formData.targetSystemId!,
      projectLeader: formData.projectLeader || undefined,
      contact: formData.contact || undefined,
      description: formData.description || undefined,
      evaluationDate: formData.evaluationDate
    }

    const projectId = await createProject(data)
    ElMessage.success('保存成功，开始评估')
    router.push(`/project/detail/${projectId}`)
  } catch {
    // 表单验证失败或其他错误
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadSystemList()
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.project-form {
  max-width: 600px;
}
</style>
