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
          <el-select v-model="formData.sourceSystemId" placeholder="请选择源系统">
            <el-option label="SAP" value="1" />
            <el-option label="Oracle" value="2" />
            <el-option label="金蝶" value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="目标系统" prop="targetSystemId">
          <el-select v-model="formData.targetSystemId" placeholder="请选择目标系统">
            <el-option label="用友" value="4" />
            <el-option label="SAP" value="5" />
            <el-option label="浪潮" value="6" />
          </el-select>
        </el-form-item>

        <el-form-item label="项目负责人">
          <el-input v-model="formData.manager" placeholder="请输入项目负责人" />
        </el-form-item>

        <el-form-item label="联系方式">
          <el-input v-model="formData.contact" placeholder="请输入联系方式" />
        </el-form-item>

        <el-form-item label="评估日期" prop="evaluationDate">
          <el-date-picker
            v-model="formData.evaluationDate"
            type="date"
            placeholder="请选择评估日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="项目描述">
          <el-input v-model="formData.description" type="textarea" rows="4" placeholder="请输入项目描述" />
        </el-form-item>

        <el-form-item>
          <el-button @click="handleCancel">取消</el-button>
          <el-button @click="handleSave">保存</el-button>
          <el-button type="primary" @click="handleSaveAndEvaluate">保存并开始评估</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()

const formData = reactive({
  projectName: '',
  customerName: '',
  sourceSystemId: '',
  targetSystemId: '',
  manager: '',
  contact: '',
  evaluationDate: '',
  description: ''
})

const formRules: FormRules = {
  projectName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { max: 200, message: '长度不能超过200个字符', trigger: 'blur' }
  ],
  customerName: [
    { required: true, message: '请输入客户名称', trigger: 'blur' },
    { max: 200, message: '长度不能超过200个字符', trigger: 'blur' }
  ],
  sourceSystemId: [{ required: true, message: '请选择源系统', trigger: 'change' }],
  targetSystemId: [
    { required: true, message: '请选择目标系统', trigger: 'change' },
    {
      validator: (_rule, value, callback) => {
        if (value === formData.sourceSystemId) {
          callback(new Error('目标系统不能与源系统相同'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  evaluationDate: [{ required: true, message: '请选择评估日期', trigger: 'change' }]
}

function handleCancel() {
  router.push('/project/list')
}

function handleSave() {
  formRef.value?.validate((valid) => {
    if (valid) {
      // TODO: 调用API保存数据
      ElMessage.success('保存成功')
      router.push('/project/list')
    }
  })
}

function handleSaveAndEvaluate() {
  formRef.value?.validate((valid) => {
    if (valid) {
      // TODO: 调用API保存数据并跳转到评估页面
      ElMessage.success('保存成功，开始评估')
      router.push('/project/detail/1')
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

.project-form {
  max-width: 600px;
}
</style>
