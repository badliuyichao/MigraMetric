<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd" data-testid="btn-add-user">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" data-testid="search-username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" data-testid="search-name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" data-testid="search-role" placeholder="请选择" clearable style="width: 140px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" data-testid="search-status" placeholder="请选择" clearable style="width: 110px">
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
      <el-table v-loading="loading" :data="tableData" data-testid="table-user" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="roleName" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">
              {{ row.roleName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" data-testid="btn-edit">编辑</el-button>
            <el-button type="warning" link @click="handleResetPwd(row)" data-testid="btn-reset-pwd">重置密码</el-button>
            <el-button type="success" link @click="handleToggleStatus(row)" data-testid="btn-toggle-status">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)" data-testid="btn-delete">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
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
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            data-testid="form-username"
            placeholder="4-20位字母数字下划线"
            :disabled="isEdit"
          />
          <span v-if="isEdit" class="form-tip">创建后不可修改</span>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input
            v-model="formData.password"
            data-testid="form-password"
            type="password"
            placeholder="6-20位字符"
            show-password
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" data-testid="form-name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="formData.role" data-testid="form-role">
            <el-radio value="ADMIN">管理员</el-radio>
            <el-radio value="USER">普通用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" data-testid="form-email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" data-testid="form-phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" data-testid="form-remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" data-testid="btn-cancel">取消</el-button>
        <el-button type="primary" :loading="submitLoading" data-testid="btn-submit" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetPwdVisible" title="重置密码" width="400px">
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="80px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            data-testid="form-new-password"
            type="password"
            placeholder="6-20位字符"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetForm.confirmPassword"
            data-testid="form-confirm-password"
            type="password"
            placeholder="再次输入密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetLoading" data-testid="btn-reset-submit" @click="handleResetPwdSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { UserVO, UserCreate, UserUpdate } from '@/api/user/users'
import {
  queryUserPage,
  createUser,
  updateUser,
  deleteUser,
  resetPassword,
  updateUserStatus
} from '@/api/user/users'

const loading = ref(false)
const submitLoading = ref(false)
const resetLoading = ref(false)
const formRef = ref<FormInstance>()
const resetFormRef = ref<FormInstance>()

const searchForm = reactive({
  username: '',
  name: '',
  role: '',
  status: null as number | null
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<UserVO[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)

const formData = reactive({
  id: null as number | null,
  username: '',
  password: '',
  name: '',
  role: 'USER',
  email: '',
  phone: '',
  remark: ''
})

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '4-20位字母数字下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '6-20位字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const resetPwdVisible = ref(false)
const resetUserId = ref<number | null>(null)
const resetForm = reactive({ newPassword: '', confirmPassword: '' })
const resetRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '6-20位字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' }
  ]
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      name: searchForm.name || undefined,
      role: searchForm.role || undefined,
      status: searchForm.status ?? undefined
    }
    const res = await queryUserPage(params)
    tableData.value = res.records
    pagination.total = res.total

    const totalPages = Math.max(1, Math.ceil(res.total / pagination.pageSize))
    if (pagination.current > totalPages && res.total > 0) {
      pagination.current = totalPages
      await loadData()
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  searchForm.username = ''
  searchForm.name = ''
  searchForm.role = ''
  searchForm.status = null
  handleSearch()
}

function handleSizeChange() {
  handleSearch()
}

function handlePageChange() {
  loadData()
}

function handleAdd() {
  dialogTitle.value = '新增用户'
  isEdit.value = false
  formData.id = null
  formData.username = ''
  formData.password = ''
  formData.name = ''
  formData.role = 'USER'
  formData.email = ''
  formData.phone = ''
  formData.remark = ''
  dialogVisible.value = true
}

function handleEdit(row: UserVO) {
  dialogTitle.value = '编辑用户'
  isEdit.value = true
  formData.id = row.id
  formData.username = row.username
  formData.password = ''
  formData.name = row.name
  formData.role = row.role
  formData.email = row.email || ''
  formData.phone = row.phone || ''
  formData.remark = row.remark || ''
  dialogVisible.value = true
}

async function handleToggleStatus(row: UserVO) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户"${row.username}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateUserStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch {
    // 用户取消
  }
}

async function handleDelete(row: UserVO) {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？删除后不可恢复`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 用户取消
  }
}

function handleResetPwd(row: UserVO) {
  resetUserId.value = row.id
  resetForm.newPassword = ''
  resetForm.confirmPassword = ''
  resetPwdVisible.value = true
}

async function handleResetPwdSubmit() {
  if (!resetFormRef.value || !resetUserId.value) return
  try {
    await resetFormRef.value.validate()
    if (resetForm.newPassword !== resetForm.confirmPassword) {
      ElMessage.error('两次输入的密码不一致')
      return
    }
    resetLoading.value = true
    await resetPassword(resetUserId.value, resetForm.newPassword, resetForm.confirmPassword)
    ElMessage.success('密码已重置')
    resetPwdVisible.value = false
  } catch {
    // 表单验证失败
  } finally {
    resetLoading.value = false
  }
}

function handleDialogClose() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (isEdit.value && formData.id) {
      const data: UserUpdate = {
        name: formData.name,
        role: formData.role,
        email: formData.email || undefined,
        phone: formData.phone || undefined,
        remark: formData.remark || undefined
      }
      await updateUser(formData.id, data)
      ElMessage.success('更新成功')
    } else {
      const data: UserCreate = {
        username: formData.username,
        password: formData.password,
        name: formData.name,
        role: formData.role,
        email: formData.email || undefined,
        phone: formData.phone || undefined,
        remark: formData.remark || undefined
      }
      await createUser(data)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    loadData()
  } catch {
    // 表单验证失败
  } finally {
    submitLoading.value = false
  }
}

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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}
</style>
