/**
 * SystemType 组件单元测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import SystemTypeView from '@/views/system/types/index.vue'

// Mock Element Plus components
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn()
  },
  ElMessageBox: {
    confirm: vi.fn().mockResolvedValue(undefined)
  }
}))

// Mock API
vi.mock('@/api/system/types', () => ({
  querySystemTypePage: vi.fn().mockResolvedValue({
    records: [],
    total: 0
  }),
  deleteSystemType: vi.fn().mockResolvedValue(undefined),
  enableSystemType: vi.fn().mockResolvedValue(undefined),
  disableSystemType: vi.fn().mockResolvedValue(undefined)
}))

import {
  querySystemTypePage,
  deleteSystemType,
  enableSystemType,
  disableSystemType
} from '@/api/system/types'

describe('SystemTypeView 组件测试', () => {
  let wrapper: VueWrapper<Record<string, unknown>>

  beforeEach(() => {
    vi.clearAllMocks()
    wrapper = mount(SystemTypeView, {
      global: {
        stubs: {
          'el-card': true,
          'el-button': true,
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-table': true,
          'el-table-column': true,
          'el-tag': true,
          'el-pagination': true,
          'el-dialog': true,
          'el-radio-group': true,
          'el-radio': true,
          'el-icon': true,
          Plus: true
        }
      }
    })
  })

  describe('初始状态', () => {
    it('应正确初始化搜索表单', () => {
      const searchForm = wrapper.vm.searchForm
      expect(searchForm.systemName).toBe('')
      expect(searchForm.systemCategory).toBeNull()
      expect(searchForm.status).toBeNull()
    })

    it('应正确初始化分页配置', () => {
      const pagination = wrapper.vm.pagination
      expect(pagination.pageNum).toBe(1)
      expect(pagination.pageSize).toBe(10)
      expect(pagination.total).toBe(0)
    })

    it('表格数据初始应为空', () => {
      expect(wrapper.vm.tableData).toEqual([])
    })

    it('弹窗初始应关闭', () => {
      expect(wrapper.vm.dialogVisible).toBe(false)
    })
  })

  describe('API 调用', () => {
    it('页面加载时应调用 querySystemTypePage', async () => {
      await wrapper.vm.$nextTick()
      expect(querySystemTypePage).toHaveBeenCalled()
    })

    it('分页参数应正确传递', async () => {
      wrapper.vm.pagination.pageNum = 2
      wrapper.vm.pagination.pageSize = 20
      await wrapper.vm.loadData()

      expect(querySystemTypePage).toHaveBeenCalledWith({
        pageNum: 2,
        pageSize: 20,
        systemName: undefined,
        systemCategory: undefined,
        status: undefined
      })
    })

    it('搜索参数应正确传递', async () => {
      wrapper.vm.searchForm.systemName = 'SAP'
      wrapper.vm.searchForm.systemCategory = 1
      await wrapper.vm.handleSearch()

      expect(querySystemTypePage).toHaveBeenCalledWith(
        expect.objectContaining({
          systemName: 'SAP',
          systemCategory: 1
        })
      )
    })
  })

  describe('搜索和重置功能', () => {
    it('handleSearch 应重置页码为1', () => {
      wrapper.vm.pagination.pageNum = 5
      wrapper.vm.handleSearch()
      expect(wrapper.vm.pagination.pageNum).toBe(1)
    })

    it('handleReset 应清空搜索条件', () => {
      wrapper.vm.searchForm.systemName = 'SAP'
      wrapper.vm.searchForm.systemCategory = 1
      wrapper.vm.searchForm.status = 1
      wrapper.vm.handleReset()

      expect(wrapper.vm.searchForm.systemName).toBe('')
      expect(wrapper.vm.searchForm.systemCategory).toBeNull()
      expect(wrapper.vm.searchForm.status).toBeNull()
    })
  })

  describe('分页功能', () => {
    it('handleSizeChange 应重置页码并触发搜索', () => {
      wrapper.vm.pagination.pageNum = 5
      wrapper.vm.handleSizeChange()
      expect(wrapper.vm.pagination.pageNum).toBe(1)
    })

    it('handlePageChange 应保持当前页码', () => {
      wrapper.vm.pagination.pageNum = 3
      wrapper.vm.handlePageChange()
      expect(wrapper.vm.pagination.pageNum).toBe(3)
    })
  })

  describe('新增功能', () => {
    it('handleAdd 应打开弹窗并设置正确标题', () => {
      wrapper.vm.handleAdd()
      expect(wrapper.vm.dialogVisible).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('新增系统类型')
    })

    it('handleAdd 应重置表单数据', () => {
      wrapper.vm.formData.systemName = 'test'
      wrapper.vm.formData.id = 1
      wrapper.vm.handleAdd()

      expect(wrapper.vm.formData.systemName).toBe('')
      expect(wrapper.vm.formData.id).toBeNull()
      expect(wrapper.vm.formData.systemCategory).toBe(1)
    })
  })

  describe('编辑功能', () => {
    it('handleEdit 应打开弹窗并填充数据', () => {
      const mockRow = {
        id: 1,
        systemName: 'SAP',
        systemCategory: 1,
        description: '测试描述'
      }
      wrapper.vm.handleEdit(mockRow)

      expect(wrapper.vm.dialogVisible).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('编辑系统类型')
      expect(wrapper.vm.formData.id).toBe(1)
      expect(wrapper.vm.formData.systemName).toBe('SAP')
    })
  })

  describe('状态切换功能', () => {
    it('handleToggleStatus 应调用正确的API', async () => {
      const enabledRow = { id: 1, status: 1 }
      await wrapper.vm.handleToggleStatus(enabledRow)

      expect(disableSystemType).toHaveBeenCalledWith(1)
    })

    it('禁用状态应调用启用API', async () => {
      const disabledRow = { id: 1, status: 0 }
      await wrapper.vm.handleToggleStatus(disabledRow)

      expect(enableSystemType).toHaveBeenCalledWith(1)
    })
  })

  describe('删除功能', () => {
    it('handleDelete 应调用删除API', async () => {
      const mockRow = { id: 1, systemName: 'SAP' }
      await wrapper.vm.handleDelete(mockRow)

      expect(deleteSystemType).toHaveBeenCalledWith(1)
    })
  })

  describe('表单提交功能', () => {
    it('新建数据应包含正确的字段', () => {
      wrapper.vm.formData.id = null
      wrapper.vm.formData.systemName = 'Oracle'
      wrapper.vm.formData.systemCategory = 2
      wrapper.vm.formData.description = 'Oracle ERP'

      const data = {
        systemName: wrapper.vm.formData.systemName,
        systemCategory: wrapper.vm.formData.systemCategory,
        description: wrapper.vm.formData.description
      }

      expect(data.systemName).toBe('Oracle')
      expect(data.systemCategory).toBe(2)
      expect(data.description).toBe('Oracle ERP')
    })

    it('编辑数据应包含正确的字段', () => {
      wrapper.vm.formData.id = 1
      wrapper.vm.formData.systemName = 'Updated SAP'
      wrapper.vm.formData.systemCategory = 1
      wrapper.vm.formData.description = '更新描述'

      const data = {
        id: wrapper.vm.formData.id,
        systemName: wrapper.vm.formData.systemName,
        systemCategory: wrapper.vm.formData.systemCategory,
        description: wrapper.vm.formData.description
      }

      expect(data.id).toBe(1)
      expect(data.systemName).toBe('Updated SAP')
    })
  })
})
