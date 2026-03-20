import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import 'element-plus/dist/index.css'
import '@/styles/index.scss'

import App from './App.vue'
import router from './router'
import { setupPermission } from './router/permission'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 创建Pinia实例
const pinia = createPinia()

// 使用插件
app.use(pinia)
app.use(router)
setupPermission(app)
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default'
})

// 全局错误处理
// eslint-disable-next-line no-console
app.config.errorHandler = (err, _instance, info) => {
  console.error('全局错误:', err)
  console.error('错误信息:', info)
}

app.mount('#app')
