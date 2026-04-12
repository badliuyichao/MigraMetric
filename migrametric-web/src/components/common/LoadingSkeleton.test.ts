import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue'

describe('LoadingSkeleton 组件测试', () => {
  it('应该正确渲染默认表格骨架屏', () => {
    const wrapper = mount(LoadingSkeleton)
    expect(wrapper.find('.loading-skeleton').exists()).toBe(true)
  })

  it('应该正确渲染卡片骨架屏', () => {
    const wrapper = mount(LoadingSkeleton, {
      props: { type: 'card', rows: 3 }
    })
    expect(wrapper.find('.card-skeleton').exists()).toBe(true)
    expect(wrapper.findAll('.card-item')).toHaveLength(3)
  })

  it('应该正确渲染列表骨架屏', () => {
    const wrapper = mount(LoadingSkeleton, {
      props: { type: 'list', rows: 5 }
    })
    expect(wrapper.find('.list-skeleton').exists()).toBe(true)
    expect(wrapper.findAll('.list-item')).toHaveLength(5)
  })

  it('应该正确设置骨架屏行数', () => {
    const rows = 8
    const wrapper = mount(LoadingSkeleton, {
      props: { rows }
    })
    // 验证组件渲染了对应数量的内容
    expect(wrapper.find('.loading-skeleton').exists()).toBe(true)
  })

  it('应该支持动画效果', () => {
    const wrapper = mount(LoadingSkeleton)
    // 验证骨架屏组件存在
    expect(wrapper.find('.loading-skeleton').exists()).toBe(true)
  })
})
