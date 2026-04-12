import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import EmptyState from '@/components/common/EmptyState.vue'

describe('EmptyState 组件测试', () => {
  it('应该正确渲染组件', () => {
    const wrapper = mount(EmptyState, {
      global: {
        stubs: {
          'el-empty': {
            template: '<div class="el-empty"><slot /></div>',
            props: ['description', 'imageSize']
          }
        }
      }
    })
    expect(wrapper.find('.empty-state').exists()).toBe(true)
  })

  it('应该正确渲染插槽内容', () => {
    const wrapper = mount(EmptyState, {
      slots: {
        default: '<button>新增</button>'
      },
      global: {
        stubs: {
          'el-empty': {
            template: '<div class="el-empty"><slot /></div>',
            props: ['description', 'imageSize']
          }
        }
      }
    })
    expect(wrapper.find('button').exists()).toBe(true)
  })

  it('应该渲染空状态的DOM结构', () => {
    const wrapper = mount(EmptyState, {
      global: {
        stubs: {
          'el-empty': {
            template: '<div class="el-empty"></div>',
            props: ['description', 'imageSize']
          }
        }
      }
    })
    expect(wrapper.find('.empty-state').exists()).toBe(true)
  })
})
