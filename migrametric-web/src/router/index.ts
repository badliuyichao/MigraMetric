import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/',
    component: () => import('@/layouts/index.vue'),
    redirect: '/dashboard',
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '首页',
          icon: 'HomeFilled'
        }
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('@/layouts/index.vue'),
        redirect: '/system/types',
        meta: {
          title: '系统管理',
          icon: 'Setting',
          roles: ['ADMIN']
        },
        children: [
          {
            path: 'types',
            name: 'SystemTypes',
            component: () => import('@/views/system/types/index.vue'),
            meta: {
              title: '系统类型管理',
              roles: ['ADMIN']
            }
          },
          {
            path: 'modules',
            name: 'SystemModules',
            component: () => import('@/views/module/index.vue'),
            meta: {
              title: '模块库管理',
              roles: ['ADMIN']
            }
          }
        ]
      },
      {
        path: 'ladder',
        name: 'Ladder',
        component: () => import('@/layouts/index.vue'),
        redirect: '/ladder/data-volume',
        meta: {
          title: '阶梯配置',
          icon: 'TrendCharts',
          roles: ['ADMIN']
        },
        children: [
          {
            path: 'data-volume',
            name: 'DataVolumeLadder',
            component: () => import('@/views/ladder/dataVolume.vue'),
            meta: {
              title: '数据量阶梯',
              roles: ['ADMIN']
            }
          },
          {
            path: 'user-count',
            name: 'UserCountLadder',
            component: () => import('@/views/ladder/userCount.vue'),
            meta: {
              title: '用户数阶梯',
              roles: ['ADMIN']
            }
          }
        ]
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/layouts/index.vue'),
        redirect: '/config/report',
        meta: {
          title: '系统配置',
          icon: 'Setting',
          roles: ['ADMIN']
        },
        children: [
          {
            path: 'report',
            name: 'ReportConfig',
            component: () => import('@/views/config/reportConfig.vue'),
            meta: {
              title: '报表系数配置',
              roles: ['ADMIN']
            }
          }
        ]
      },
      {
        path: 'project',
        name: 'Project',
        component: () => import('@/layouts/index.vue'),
        redirect: '/project/list',
        meta: {
          title: '项目管理',
          icon: 'Folder'
        },
        children: [
          {
            path: 'list',
            name: 'ProjectList',
            component: () => import('@/views/project/list/index.vue'),
            meta: {
              title: '项目列表'
            }
          },
          {
            path: 'create',
            name: 'ProjectCreate',
            component: () => import('@/views/project/create/index.vue'),
            meta: {
              title: '创建项目'
            }
          },
          {
            path: 'detail/:id',
            name: 'ProjectDetail',
            component: () => import('@/views/project/detail/index.vue'),
            meta: {
              title: '项目详情',
              hidden: true
            }
          }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在'
    }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

export default router
