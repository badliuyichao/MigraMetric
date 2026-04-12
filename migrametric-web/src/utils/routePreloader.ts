import router from '@/router'

/**
 * 路由预加载管理器
 * 用于在空闲时预加载可能访问的路由组件
 *
 * @example
 * // 在 App.vue 中初始化
 * const routePreloader = new RoutePreloader()
 * routePreloader.init()
 *
 * // 预加载指定路由
 * routePreloader.preload('/project/detail/1')
 *
 * // 预加载下级路由
 * routePreloader.preloadChildren('/project')
 */
export class RoutePreloader {
  private preloadedRoutes: Set<string> = new Set()
  private isIdle: boolean = false
  private idleCallbackId?: number

  /**
   * 初始化路由预加载
   */
  init() {
    if (typeof window === 'undefined') return

    // 使用 requestIdleCallback 在浏览器空闲时预加载
    if ('requestIdleCallback' in window) {
      this.idleCallbackId = window.requestIdleCallback(() => {
        this.preloadAllRoutes()
      }, { timeout: 5000 })
    } else {
      // 降级处理：延迟 3 秒后预加载
      setTimeout(() => {
        this.preloadAllRoutes()
      }, 3000)
    }

    // 监听路由变化，预加载即将访问的路由
    router.beforeEach((to, from, next) => {
      // 在导航完成前开始预加载
      this.preloadRelatedRoutes(to.path)
      next()
    })
  }

  /**
   * 预加载指定路由
   */
  async preload(routePath: string) {
    if (this.preloadedRoutes.has(routePath)) {
      return // 已预加载
    }

    try {
      // 查找匹配的路由记录
      const matchedRoutes = router.getRoutes().filter(route => {
        return this.matchRoute(route.path, routePath)
      })

      // 预加载所有匹配的路由组件
      for (const route of matchedRoutes) {
        if (route.components?.default) {
          // 已经是加载过的组件
          continue
        }

        // 使用 Vue Router 的 addRoute 触发懒加载
        const component = await this.loadRouteComponent(route)
        if (component) {
          this.preloadedRoutes.add(routePath)
        }
      }
    } catch (error) {
      console.warn('路由预加载失败:', routePath, error)
    }
  }

  /**
   * 预加载路由的子路由
   */
  async preloadChildren(parentPath: string) {
    const childRoutes = router.getRoutes().filter(route => {
      return route.path.startsWith(parentPath + '/')
    })

    for (const route of childRoutes) {
      await this.preload(route.path)
    }
  }

  /**
   * 预加载相关路由（当前路由的父路由和常用路由）
   */
  async preloadRelatedRoutes(currentPath: string) {
    // 预加载父路由
    const parentPath = this.getParentPath(currentPath)
    if (parentPath) {
      this.preload(parentPath)
    }

    // 预加载兄弟路由（同一父路由下的其他路由）
    const siblingPaths = this.getSiblingPaths(currentPath)
    for (const path of siblingPaths) {
      this.preload(path)
    }
  }

  /**
   * 预加载所有路由
   */
  private async preloadAllRoutes() {
    for (const route of router.getRoutes()) {
      if (route.children && route.children.length > 0) {
        // 跳过有子路由的父路由
        continue
      }
      await this.preload(route.path)
    }
  }

  /**
   * 加载路由组件
   */
  private async loadRouteComponent(route: { components?: { default?: unknown }; children?: unknown[]; path?: string }): Promise<unknown | null> {
    try {
      if (route.children && route.children.length > 0) {
        return null
      }

      // 触发动态导入
      const path = route.path
      const importPath = this.getImportPath(path)
      if (importPath) {
        return await import(/* @vite-ignore */ importPath)
      }
      return null
    } catch {
      return null
    }
  }

  /**
   * 获取导入路径
   */
  private getImportPath(routePath: string): string | null {
    // 根据路由路径推断组件路径
    const pathMap: Record<string, string> = {
      '/login': '@/views/login/index.vue',
      '/dashboard': '@/views/dashboard/index.vue',
      '/system/types': '@/views/system/types/index.vue',
      '/system/modules': '@/views/module/index.vue',
      '/ladder/data-volume': '@/views/ladder/dataVolume.vue',
      '/ladder/user-count': '@/views/ladder/userCount.vue',
      '/config/report': '@/views/config/reportConfig.vue',
      '/project/list': '@/views/project/list/index.vue',
      '/project/create': '@/views/project/create/index.vue',
      '/project/detail': '@/views/project/detail/index.vue',
      '/project/edit': '@/views/project/edit/index.vue',
      '/project/evaluate': '@/views/project/evaluate/index.vue',
      '/project/statistics': '@/views/project/statistics/index.vue'
    }

    return pathMap[routePath] || null
  }

  /**
   * 匹配路由
   */
  private matchRoute(routePattern: string, path: string): boolean {
    // 简单匹配：路由模式去除参数部分
    const pattern = routePattern.replace(/:[^/]+/g, '')
    return path.startsWith(pattern) || pattern.startsWith(path)
  }

  /**
   * 获取父路径
   */
  private getParentPath(path: string): string | null {
    const segments = path.split('/').filter(Boolean)
    if (segments.length <= 1) return null
    segments.pop()
    return '/' + segments.join('/')
  }

  /**
   * 获取兄弟路径
   */
  private getSiblingPaths(path: string): string[] {
    const parentPath = this.getParentPath(path)
    if (!parentPath) return []

    return router.getRoutes()
      .filter(route => {
        return route.path.startsWith(parentPath + '/') && route.path !== path
      })
      .map(route => route.path)
  }

  /**
   * 清理预加载缓存
   */
  clearCache() {
    this.preloadedRoutes.clear()
  }

  /**
   * 销毁
   */
  destroy() {
    if (this.idleCallbackId) {
      cancelAnimationFrame(this.idleCallbackId)
    }
    this.clearCache()
  }
}

// 创建单例
export const routePreloader = new RoutePreloader()

export default routePreloader
