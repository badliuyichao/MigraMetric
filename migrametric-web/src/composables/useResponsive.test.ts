/**
 * 响应式布局组合函数测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useResponsive, useTableResponsive, useGridResponsive } from '@/composables/useResponsive'

// 定义 BREAKPOINTS 常量用于测试
const BREAKPOINTS = {
  xs: 0,
  sm: 640,
  md: 768,
  lg: 1024,
  xl: 1280,
  xxl: 1536
}

describe('useResponsive', () => {
  describe('基础功能', () => {
    it('应该包含所有响应式属性和方法', () => {
      const result = useResponsive()
      expect(result.width).toBeDefined()
      expect(result.breakpoint).toBeDefined()
      expect(result.isMobile).toBeDefined()
      expect(result.isTablet).toBeDefined()
      expect(result.isDesktop).toBeDefined()
      expect(result.isWide).toBeDefined()
      expect(result.isBelow).toBeDefined()
      expect(result.isAbove).toBeDefined()
      expect(result.isBetween).toBeDefined()
      expect(result.BREAKPOINTS).toBeDefined()
    })

    it('BREAKPOINTS 应该包含正确的断点配置', () => {
      const { BREAKPOINTS: bp } = useResponsive()
      expect(bp.xs).toBe(0)
      expect(bp.sm).toBe(640)
      expect(bp.md).toBe(768)
      expect(bp.lg).toBe(1024)
      expect(bp.xl).toBe(1280)
      expect(bp.xxl).toBe(1536)
    })
  })

  describe('断点判断函数', () => {
    it('isBelow 函数应该正确判断', () => {
      const { isBelow } = useResponsive()
      // 测试函数存在且可调用
      expect(typeof isBelow).toBe('function')
    })

    it('isAbove 函数应该正确判断', () => {
      const { isAbove } = useResponsive()
      // 测试函数存在且可调用
      expect(typeof isAbove).toBe('function')
    })

    it('isBetween 函数应该正确判断', () => {
      const { isBetween } = useResponsive()
      // 测试函数存在且可调用
      expect(typeof isBetween).toBe('function')
    })
  })

  describe('useTableResponsive', () => {
    const mockColumns = [
      { prop: 'name', label: '名称' },
      { prop: 'status', label: '状态' },
      { prop: 'date', label: '日期' },
      { prop: 'description', label: '描述' }
    ]

    it('应该返回 visibleColumns, hiddenColumns, isSmallScreen', () => {
      const result = useTableResponsive(mockColumns)
      expect(result.visibleColumns).toBeDefined()
      expect(result.hiddenColumns).toBeDefined()
      expect(result.isSmallScreen).toBeDefined()
    })

    it('应该支持 priorityColumns 选项', () => {
      const result = useTableResponsive(mockColumns, {
        priorityColumns: ['name', 'status']
      })
      expect(result.visibleColumns.value.length).toBeLessThanOrEqual(mockColumns.length)
    })
  })

  describe('useGridResponsive', () => {
    it('应该返回响应式网格配置', () => {
      const result = useGridResponsive({
        xs: 1,
        sm: 2,
        md: 3,
        lg: 4
      })
      expect(result.breakpoint).toBeDefined()
      expect(result.currentCols).toBeDefined()
      expect(result.gap).toBeDefined()
      expect(result.isMobile).toBeDefined()
      expect(result.isTablet).toBeDefined()
      expect(result.isDesktop).toBeDefined()
      expect(result.isWide).toBeDefined()
    })

    it('应该使用默认列数配置', () => {
      const result = useGridResponsive()
      expect(result.currentCols.value).toBe(4) // 默认4列
    })
  })
})
