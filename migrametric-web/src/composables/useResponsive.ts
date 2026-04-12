import { ref, computed, onMounted, onUnmounted } from 'vue'

/**
 * 响应式断点
 */
export type Breakpoint = 'xs' | 'sm' | 'md' | 'lg' | 'xl' | 'xxl'

/**
 * 断点宽度配置
 */
const BREAKPOINTS = {
  xs: 0,
  sm: 640,
  md: 768,
  lg: 1024,
  xl: 1280,
  xxl: 1536
}

/**
 * useResponsive 组合函数
 * 提供响应式布局支持
 *
 * @example
 * const { breakpoint, isMobile, isTablet, isDesktop } = useResponsive()
 *
 * // 在模板中使用
 * <div v-if="isMobile">移动端内容</div>
 * <div v-else>桌面端内容</div>
 */
export function useResponsive() {
  const width = ref(typeof window !== 'undefined' ? window.innerWidth : 1920)

  // 当前断点
  const breakpoint = computed<Breakpoint>(() => {
    const w = width.value
    if (w < BREAKPOINTS.sm) return 'xs'
    if (w < BREAKPOINTS.md) return 'sm'
    if (w < BREAKPOINTS.lg) return 'md'
    if (w < BREAKPOINTS.xl) return 'lg'
    if (w < BREAKPOINTS.xxl) return 'xl'
    return 'xxl'
  })

  // 设备类型
  const isMobile = computed(() => width.value < BREAKPOINTS.md)
  const isTablet = computed(() => width.value >= BREAKPOINTS.md && width.value < BREAKPOINTS.lg)
  const isDesktop = computed(() => width.value >= BREAKPOINTS.lg)
  const isWide = computed(() => width.value >= BREAKPOINTS.xl)

  // 是否小于指定断点
  function isBelow(bp: Breakpoint): boolean {
    return width.value < BREAKPOINTS[bp]
  }

  // 是否大于等于指定断点
  function isAbove(bp: Breakpoint): boolean {
    return width.value >= BREAKPOINTS[bp]
  }

  // 是否在指定范围内
  function isBetween(start: Breakpoint, end: Breakpoint): boolean {
    return width.value >= BREAKPOINTS[start] && width.value < BREAKPOINTS[end]
  }

  // 更新宽度
  function updateWidth() {
    if (typeof window !== 'undefined') {
      width.value = window.innerWidth
    }
  }

  onMounted(() => {
    if (typeof window !== 'undefined') {
      window.addEventListener('resize', updateWidth)
      updateWidth()
    }
  })

  onUnmounted(() => {
    if (typeof window !== 'undefined') {
      window.removeEventListener('resize', updateWidth)
    }
  })

  return {
    width,
    breakpoint,
    isMobile,
    isTablet,
    isDesktop,
    isWide,
    isBelow,
    isAbove,
    isBetween,
    BREAKPOINTS
  }
}

/**
 * useTableResponsive 组合函数
 * 表格响应式适配
 *
 * @example
 * const { visibleColumns, isSmallScreen } = useTableResponsive(columns, {
 *   priorityColumns: ['name', 'status'],
 *   minWidth: 800
 * })
 */
export function useTableResponsive<T extends { prop: string }>(
  columns: T[],
  options: {
    priorityColumns?: string[]
    minWidth?: number
  } = {}
) {
  const { isMobile, isTablet } = useResponsive()
  const isSmallScreen = computed(() => isMobile.value || isTablet.value)

  const priorityColumns = options.priorityColumns || []

  const visibleColumns = computed(() => {
    // 在小屏幕上隐藏非优先列
    if (isSmallScreen.value) {
      return columns.filter(
        col => priorityColumns.length === 0 || priorityColumns.includes(col.prop)
      )
    }
    return columns
  })

  const hiddenColumns = computed(() => {
    if (isSmallScreen.value) {
      return columns.filter(
        col => priorityColumns.length > 0 && !priorityColumns.includes(col.prop)
      )
    }
    return []
  })

  return {
    visibleColumns,
    hiddenColumns,
    isSmallScreen
  }
}

/**
 * useGridResponsive 组合函数
 * 网格布局响应式适配
 *
 * @example
 * const { cols, gap } = useGridResponsive({
 *   xs: 1,  // 手机
 *   sm: 2,  // 平板竖屏
 *   md: 3,  // 平板横屏
 *   lg: 4,  // 小桌面
 *   xl: 5,  // 桌面
 *   xxl: 6  // 大桌面
 * })
 */
export function useGridResponsive(
  cols: Partial<Record<Breakpoint, number>> = {}
) {
  const { breakpoint, isMobile, isTablet, isDesktop, isWide } = useResponsive()

  const currentCols = computed(() => {
    if (isMobile.value && cols.xs) return cols.xs
    if (isTablet.value && (cols.sm || cols.md)) return cols.sm || cols.md
    if (isDesktop.value && (cols.lg || cols.md || cols.sm)) {
      return cols.lg || cols.md || cols.sm
    }
    if (isWide.value && cols.xl) return cols.xl
    if (cols.xxl) return cols.xxl
    return 4 // 默认4列
  })

  const gap = computed(() => {
    if (isMobile.value) return '12px'
    if (isTablet.value) return '16px'
    return '20px'
  })

  return {
    breakpoint,
    currentCols,
    gap,
    isMobile,
    isTablet,
    isDesktop,
    isWide
  }
}
