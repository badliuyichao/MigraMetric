import type { App } from 'vue'
import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'

/**
 * 设置路由权限控制
 */
export function setupPermission(_app: App, router: Router): void {
  router.beforeEach(async (
    to: RouteLocationNormalized,
    _from: RouteLocationNormalized,
    next: NavigationGuardNext
  ) => {
    // 设置页面标题
    document.title = `${to.meta.title || 'MigraMetric'} - 工作量评估系统`

    const userStore = useUserStore()
    const requiresAuth = to.meta.requiresAuth !== false

    // 需要登录
    if (requiresAuth) {
      // 检查是否已登录
      if (!userStore.token) {
        // 没有token，跳转登录页
        ElMessage.warning('请先登录')
        return next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
      }

      // 有token但没有用户信息，获取用户信息
      if (!userStore.userInfo) {
        try {
          await userStore.getUserInfo()
        } catch {
          // 获取失败，可能token已过期
          userStore.logout()
          return next({
            path: '/login',
            query: { redirect: to.fullPath }
          })
        }
      }

      // 检查角色权限
      const requiredRoles = to.meta.roles as string[] | undefined
      if (requiredRoles && requiredRoles.length > 0) {
        const hasPermission = userStore.hasRole(requiredRoles)
        if (!hasPermission) {
          ElMessage.error('您没有权限访问该页面')
          return next({ path: '/dashboard' })
        }
      }
    } else {
      // 不需要登录的页面
      // 如果已登录且访问登录页，跳转首页
      if (to.path === '/login' && userStore.token) {
        return next({ path: '/' })
      }
    }

    next()
  })
}
