import { describe, it, expect, vi, beforeEach } from 'vitest'
import { request } from '@/utils/request'
import {
  queryProjectPage,
  getProjectById,
  getProjectDetail,
  createProject,
  updateProject,
  copyProject,
  deleteProject,
  archiveProject,
  type ProjectVO,
  type ProjectDetailVO,
  type ProjectQuery,
  type ProjectCreate,
  type ProjectUpdate
} from './projects'

vi.mock('@/utils/request')

const mockRequest = request as ReturnType<typeof vi.fn>

describe('项目管理 API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('createProject', () => {
    it('应成功创建项目并返回ID', async () => {
      mockRequest.post.mockResolvedValue({ data: 1 })

      const data: ProjectCreate = {
        projectName: '测试项目',
        customerName: '测试客户',
        sourceSystemId: 1,
        targetSystemId: 2,
        evaluationDate: '2026-03-20'
      }

      const result = await createProject(data)

      expect(result).toBe(1)
      expect(mockRequest.post).toHaveBeenCalledWith('/api/projects', data)
    })

    it('应支持完整的创建参数', async () => {
      mockRequest.post.mockResolvedValue({ data: 2 })

      const data: ProjectCreate = {
        projectName: '完整项目',
        customerName: '客户',
        sourceSystemId: 1,
        targetSystemId: 2,
        projectLeader: '张三',
        contact: '13800138000',
        description: '测试描述',
        evaluationDate: '2026-03-20'
      }

      const result = await createProject(data)

      expect(result).toBe(2)
    })
  })

  describe('queryProjectPage', () => {
    it('应返回项目分页列表', async () => {
      const mockResponse = {
        records: [
          {
            id: 1,
            projectName: 'ERP迁移项目',
            customerName: 'XX公司',
            sourceSystemId: 1,
            sourceSystemName: 'SAP',
            targetSystemId: 2,
            targetSystemName: '用友',
            projectLeader: '张三',
            contact: '13800138000',
            description: 'SAP到用友的ERP迁移',
            evaluationDate: '2026-03-20',
            status: 'DRAFT',
            statusText: '草稿',
            userId: 1,
            createByName: '管理员',
            createTime: '2026-03-20 10:00:00',
            updateTime: '2026-03-20 10:00:00'
          }
        ],
        total: 1,
        pageNum: 1,
        pageSize: 10,
        totalPages: 1,
        hasPrevious: false,
        hasNext: false
      }
      mockRequest.get.mockResolvedValue({ data: mockResponse })

      const result = await queryProjectPage({ pageNum: 1, pageSize: 10 })

      expect(result.records).toHaveLength(1)
      expect(result.records[0].projectName).toBe('ERP迁移项目')
      expect(mockRequest.get).toHaveBeenCalledWith('/api/projects', {
        params: { pageNum: 1, pageSize: 10 }
      })
    })

    it('应正确传递查询参数', async () => {
      const mockResponse = {
        records: [],
        total: 0,
        pageNum: 1,
        pageSize: 10
      }
      mockRequest.get.mockResolvedValue({ data: mockResponse })

      const params: ProjectQuery = {
        projectName: '测试',
        customerName: '客户',
        status: 'DRAFT'
      }
      await queryProjectPage(params)

      expect(mockRequest.get).toHaveBeenCalledWith('/api/projects', {
        params: { projectName: '测试', customerName: '客户', status: 'DRAFT' }
      })
    })

    it('应支持分页参数', async () => {
      const mockResponse = {
        records: [],
        total: 0,
        pageNum: 2,
        pageSize: 20
      }
      mockRequest.get.mockResolvedValue({ data: mockResponse })

      const result = await queryProjectPage({ pageNum: 2, pageSize: 20 })

      expect(result.pageNum).toBe(2)
      expect(result.pageSize).toBe(20)
    })
  })

  describe('getProjectById', () => {
    it('应根据ID获取项目详情', async () => {
      const mockProject: ProjectVO = {
        id: 1,
        projectName: 'ERP迁移项目',
        customerName: 'XX公司',
        sourceSystemId: 1,
        sourceSystemName: 'SAP',
        targetSystemId: 2,
        targetSystemName: '用友',
        projectLeader: '张三',
        contact: '13800138000',
        description: 'SAP到用友的ERP迁移',
        evaluationDate: '2026-03-20',
        status: 'DRAFT',
        statusText: '草稿',
        userId: 1,
        createByName: '管理员',
        createTime: '2026-03-20 10:00:00',
        updateTime: '2026-03-20 10:00:00'
      }
      mockRequest.get.mockResolvedValue({ data: mockProject })

      const result = await getProjectById(1)

      expect(result.id).toBe(1)
      expect(result.projectName).toBe('ERP迁移项目')
      expect(mockRequest.get).toHaveBeenCalledWith('/api/projects/1')
    })
  })

  describe('getProjectDetail', () => {
    it('应获取包含评估概况的项目详情', async () => {
      const mockDetail: ProjectDetailVO = {
        id: 1,
        projectName: 'ERP迁移项目',
        customerName: 'XX公司',
        sourceSystemId: 1,
        sourceSystemName: 'SAP',
        targetSystemId: 2,
        targetSystemName: '用友',
        projectLeader: '张三',
        contact: '13800138000',
        description: 'SAP到用友的ERP迁移',
        evaluationDate: '2026-03-20',
        status: 'IN_PROGRESS',
        statusText: '进行中',
        userId: 1,
        createByName: '管理员',
        createTime: '2026-03-20 10:00:00',
        updateTime: '2026-03-20 10:00:00',
        hasEvaluation: true,
        selectedModuleCount: 5,
        totalWorkload: 156.38,
        coreWorkload: 120.5,
        reportWorkload: 15.88,
        customDevWorkload: 20.0,
        dataVolume: 500,
        userCount: 600,
        reportCount: 50,
        tableCount: 200,
        evaluationStatus: 'COMPLETED',
        evaluationStatusText: '已完成',
        evaluationTime: '2026-03-20 15:00:00'
      }
      mockRequest.get.mockResolvedValue({ data: mockDetail })

      const result = await getProjectDetail(1)

      expect(result.id).toBe(1)
      expect(result.projectName).toBe('ERP迁移项目')
      expect(result.hasEvaluation).toBe(true)
      expect(result.totalWorkload).toBe(156.38)
      expect(result.selectedModuleCount).toBe(5)
      expect(mockRequest.get).toHaveBeenCalledWith('/api/projects/1/detail')
    })

    it('应正确处理无评估记录的项目详情', async () => {
      const mockDetail: ProjectDetailVO = {
        id: 2,
        projectName: '新项目',
        customerName: '客户',
        sourceSystemId: 1,
        sourceSystemName: 'SAP',
        targetSystemId: 2,
        targetSystemName: '用友',
        projectLeader: '李四',
        contact: '13900139000',
        description: '测试',
        evaluationDate: '2026-03-20',
        status: 'DRAFT',
        statusText: '草稿',
        userId: 1,
        createByName: '管理员',
        createTime: '2026-03-20 10:00:00',
        updateTime: '2026-03-20 10:00:00',
        hasEvaluation: false,
        selectedModuleCount: 0,
        totalWorkload: null,
        coreWorkload: null,
        reportWorkload: null,
        customDevWorkload: null,
        dataVolume: null,
        userCount: null,
        reportCount: null,
        tableCount: null,
        evaluationStatus: null,
        evaluationStatusText: null,
        evaluationTime: null
      }
      mockRequest.get.mockResolvedValue({ data: mockDetail })

      const result = await getProjectDetail(2)

      expect(result.hasEvaluation).toBe(false)
      expect(result.totalWorkload).toBeNull()
      expect(result.selectedModuleCount).toBe(0)
    })
  })

  describe('updateProject', () => {
    it('应成功更新项目', async () => {
      mockRequest.put.mockResolvedValue({ data: undefined })

      const data: ProjectUpdate = {
        projectName: '更新后的项目名称',
        projectLeader: '李四'
      }

      const result = await updateProject(1, data)

      expect(result).toBeUndefined()
      expect(mockRequest.put).toHaveBeenCalledWith('/api/projects/1', data)
    })

    it('应支持完整更新参数', async () => {
      mockRequest.put.mockResolvedValue({ data: undefined })

      const data: ProjectUpdate = {
        projectName: '新项目名称',
        customerName: '新客户',
        sourceSystemId: 1,
        targetSystemId: 2,
        projectLeader: '负责人',
        contact: '13800138000',
        description: '新描述',
        evaluationDate: '2026-03-25'
      }

      await updateProject(1, data)

      expect(mockRequest.put).toHaveBeenCalledWith('/api/projects/1', data)
    })
  })

  describe('copyProject', () => {
    it('应成功复制项目并返回新项目ID', async () => {
      mockRequest.post.mockResolvedValue({ data: 200 })

      const result = await copyProject(1)

      expect(result).toBe(200)
      expect(mockRequest.post).toHaveBeenCalledWith('/api/projects/1/copy')
    })
  })

  describe('deleteProject', () => {
    it('应成功删除项目', async () => {
      mockRequest.delete.mockResolvedValue({ data: undefined })

      const result = await deleteProject(1)

      expect(result).toBeUndefined()
      expect(mockRequest.delete).toHaveBeenCalledWith('/api/projects/1')
    })
  })

  describe('archiveProject', () => {
    it('应成功归档项目', async () => {
      mockRequest.post.mockResolvedValue({ data: undefined })

      const result = await archiveProject(1)

      expect(result).toBeUndefined()
      expect(mockRequest.post).toHaveBeenCalledWith('/api/projects/1/archive')
    })
  })
})
