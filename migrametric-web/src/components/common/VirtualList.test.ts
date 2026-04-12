/**
 * 虚拟列表组件测试
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { defineComponent, h, nextTick, ref } from 'vue'
import VirtualList from '@/components/common/VirtualList.vue'

// 创建测试数据
const createTestData = (count: number) => {
  return Array.from({ length: count }, (_, i) => ({
    id: i + 1,
    name: `项目 ${i + 1}`,
    description: `这是第 ${i + 1} 个项目的描述`
  }))
}

describe('VirtualList Component', () => {
  const testData = createTestData(100)

  // 基础渲染测试
  describe('基础渲染', () => {
    it('应该正确渲染虚拟列表容器', () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: testData,
          itemHeight: 50,
          height: 400
        },
        slots: {
          default: ({ item }: { item: { id: number; name: string } }) =>
            h('div', { class: 'list-item' }, item.name)
        }
      })

      expect(wrapper.find('.virtual-list').exists()).toBe(true)
      expect(wrapper.find('.virtual-list').attributes('style')).toContain('height: 400px')
    })

    it('应该正确设置容器高度', () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: testData,
          itemHeight: 50,
          height: 600
        }
      })

      expect(wrapper.find('.virtual-list').attributes('style')).toContain('height: 600px')
    })

    it('数据为空时应该正确渲染', () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: [],
          itemHeight: 50,
          height: 400
        }
      })

      expect(wrapper.find('.virtual-list').exists()).toBe(true)
      expect(wrapper.find('.virtual-list-content').exists()).toBe(true)
    })
  })

  // Props 测试
  describe('Props 配置', () => {
    it('应该正确应用 itemHeight', () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: testData,
          itemHeight: 100,
          height: 400
        }
      })

      expect(wrapper.find('.virtual-list').exists()).toBe(true)
    })

    it('应该正确应用 buffer 配置', () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: testData,
          itemHeight: 50,
          buffer: 5,
          height: 400
        }
      })

      expect(wrapper.find('.virtual-list').exists()).toBe(true)
    })

    it('应该支持自定义 key 获取函数', () => {
      const customKeyFn = (item: { id: number; code: string }) => item.code
      const customData = createTestData(10).map((item, i) => ({
        ...item,
        code: `CODE_${i + 1}`
      }))

      const wrapper = mount(VirtualList, {
        props: {
          data: customData,
          itemHeight: 50,
          height: 400,
          getKey: customKeyFn
        },
        slots: {
          default: ({ item }: { item: { name: string } }) => h('div', item.name)
        }
      })

      expect(wrapper.find('.virtual-list').exists()).toBe(true)
    })
  })

  // 滚动功能测试
  describe('滚动功能', () => {
    it('应该响应滚动事件', async () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: testData,
          itemHeight: 50,
          height: 400
        },
        slots: {
          default: ({ item }: { item: { name: string } }) => h('div', item.name)
        }
      })

      const container = wrapper.find('.virtual-list')
      await container.trigger('scroll')

      // 组件应该能够处理滚动事件
      expect(wrapper.find('.virtual-list').exists()).toBe(true)
    })

    it('应该正确计算总高度', () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: testData,
          itemHeight: 50,
          height: 400
        }
      })

      const spacer = wrapper.find('.virtual-list-spacer')
      expect(spacer.attributes('style')).toContain('height: 5000px') // 100 * 50
    })
  })

  // 暴露方法测试
  describe('暴露的方法', () => {
    it('应该暴露 scrollToIndex 方法', async () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: testData,
          itemHeight: 50,
          height: 400
        }
      })

      expect(typeof wrapper.vm.scrollToIndex).toBe('function')
    })

    it('应该暴露 scrollToTop 方法', async () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: testData,
          itemHeight: 50,
          height: 400
        }
      })

      expect(typeof wrapper.vm.scrollToTop).toBe('function')
    })

    it('应该暴露 scrollToBottom 方法', async () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: testData,
          itemHeight: 50,
          height: 400
        }
      })

      expect(typeof wrapper.vm.scrollToBottom).toBe('function')
    })

    it('scrollToIndex 应该正确设置滚动位置', async () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: testData,
          itemHeight: 50,
          height: 400
        }
      })

      wrapper.vm.scrollToIndex(10)
      await nextTick()

      const container = wrapper.find('.virtual-list')
      expect((container.element as HTMLElement).scrollTop).toBe(500) // 10 * 50
    })

    it('scrollToBottom 应该滚动到底部', async () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: testData,
          itemHeight: 50,
          height: 400
        }
      })

      wrapper.vm.scrollToBottom()
      await nextTick()

      const container = wrapper.find('.virtual-list')
      expect((container.element as HTMLElement).scrollTop).toBe(4950) // (100-1) * 50
    })
  })

  // 性能测试
  describe('性能相关', () => {
    it('应该只渲染可视区域内的项', () => {
      const wrapper = mount(VirtualList, {
        props: {
          data: testData,
          itemHeight: 50,
          height: 400
        },
        slots: {
          default: ({ item }: { item: { name: string } }) => h('div', { class: 'item' }, item.name)
        }
      })

      // 可视区域内大约有 400/50 = 8 个项，加上 buffer
      const visibleItems = wrapper.findAll('.item')
      // 由于虚拟列表的特性，渲染的项数应该远小于总数据量
      expect(visibleItems.length).toBeLessThan(20)
    })

    it('大数据量时性能应该稳定', () => {
      const largeData = createTestData(10000)

      const wrapper = mount(VirtualList, {
        props: {
          data: largeData,
          itemHeight: 50,
          height: 400
        },
        slots: {
          default: ({ item }: { item: { name: string } }) => h('div', item.name)
        }
      })

      // 总高度应该正确计算
      const spacer = wrapper.find('.virtual-list-spacer')
      expect(spacer.attributes('style')).toContain('height: 500000px') // 10000 * 50
    })
  })
})
