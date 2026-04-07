import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,

    // 多项目配置：分离不同测试类型
    projects: [
      // 默认项目：单元测试
      {
        test: {
          name: 'unit',
          include: ['src/**/*.test.ts'],
          exclude: ['src/**/*.interaction.test.ts'],
          environment: 'jsdom',
          coverage: {
            provider: 'v8',
            reporter: ['text', 'json', 'html'],
            exclude: ['node_modules/**', 'dist/**', '*.config.*']
          }
        }
      },
      // 组件交互测试项目
      {
        test: {
          name: 'interaction',
          include: ['src/**/*.interaction.test.ts'],
          environment: 'jsdom',
          setupFiles: ['./tests/setup/component-setup.ts'],
          testTimeout: 10000,
          coverage: {
            provider: 'v8',
            reporter: ['text', 'json', 'html'],
            exclude: ['node_modules/**', 'dist/**', '*.config.*', 'tests/**']
          }
        }
      }
    ]
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  }
})