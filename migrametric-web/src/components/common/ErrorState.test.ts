import { describe, it, expect } from 'vitest'
import ErrorState from './ErrorState.vue'

// 使用 get shallow wrapper
import { mount } from '@vue/test-utils'
import { h } from 'vue'

// 全局注册 Element Plus 组件
import { ElButton, ElIcon } from 'element-plus'

const TestWrapper = {
  components: { ErrorState, ElButton, ElIcon },
  template: `<ErrorState />`
}

describe('ErrorState 组件测试', () => {
  it('应该正确渲染组件', () => {
    const wrapper = mount(TestWrapper, {
      global: {
        stubs: {
          'el-button': {
            template: '<button><slot /></button>',
            props: ['loading', 'type']
          },
          'el-icon': {
            template: '<span class="el-icon"><slot /></span>'
          },
          'WarningFilled': { template: '<span class="warning-filled-icon" />' },
          'Refresh': { template: '<span class="refresh-icon" />' }
        }
      }
    })
    expect(wrapper.findComponent(ErrorState).exists()).toBe(true)
  })

  it('点击重试按钮应该触发retry事件', async () => {
    const wrapper = mount(ErrorState, {
      global: {
        stubs: {
          'el-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>',
            props: ['loading', 'type']
          },
          'el-icon': {
            template: '<span class="el-icon"><slot /></span>'
          },
          'WarningFilled': { template: '<span class="warning-filled-icon" />' },
          'Refresh': { template: '<span class="refresh-icon" />' }
        }
      }
    })

    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('retry')).toBeTruthy()
  })
})
