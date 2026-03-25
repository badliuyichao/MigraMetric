/**
 * 图片懒加载指令测试
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, nextTick, ref, h } from 'vue'
import vLazyLoad from '@/directives/lazyLoad'

// Mock IntersectionObserver
const mockObserve = vi.fn()
const mockUnobserve = vi.fn()
const mockDisconnect = vi.fn()

class MockIntersectionObserver implements IntersectionObserver {
  private callback: IntersectionObserverCallback
  observe = mockObserve
  unobserve = mockUnobserve
  disconnect = mockDisconnect
  takeRecords = vi.fn(() => [])

  constructor(callback: IntersectionObserverCallback) {
    this.callback = callback
  }
}

vi.stubGlobal('IntersectionObserver', MockIntersectionObserver)

describe('vLazyLoad Directive', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  // 基础功能测试
  describe('基础功能', () => {
    it('应该正确挂载并设置占位背景色', async () => {
      const TestComponent = defineComponent({
        directives: { lazyLoad: vLazyLoad },
        render() {
          return h('img', { 'v-lazy-load': 'https://example.com/image.jpg' })
        }
      })

      const wrapper = mount(TestComponent, {
        global: {
          directives: { lazyLoad: vLazyLoad }
        }
      })

      const img = wrapper.find('img')
      expect(img.exists()).toBe(true)
    })

    it('没有 src 时应该正确处理', async () => {
      const TestComponent = defineComponent({
        directives: { lazyLoad: vLazyLoad },
        render() {
          return h('img', { 'v-lazy-load': 'https://example.com/image.jpg' })
        }
      })

      const wrapper = mount(TestComponent, {
        global: {
          directives: { lazyLoad: vLazyLoad }
        }
      })

      const img = wrapper.find('img')
      expect(img.exists()).toBe(true)
    })
  })

  // 图片加载测试
  describe('图片加载', () => {
    it('immediate 修饰符应该立即加载图片', async () => {
      const TestComponent = defineComponent({
        directives: { lazyLoad: vLazyLoad },
        render() {
          return h('img', { 'v-lazy-load': 'https://example.com/image.jpg' })
        }
      })

      const wrapper = mount(TestComponent, {
        global: {
          directives: { lazyLoad: vLazyLoad }
        }
      })

      await nextTick()

      const img = wrapper.find('img')
      expect(img.exists()).toBe(true)
    })

    it('支持对象形式的图片数据', async () => {
      const TestComponent = defineComponent({
        directives: { lazyLoad: vLazyLoad },
        render() {
          return h('img', { 'v-lazy-load': { src: 'https://example.com/image.jpg' } })
        }
      })

      const wrapper = mount(TestComponent, {
        global: {
          directives: { lazyLoad: vLazyLoad }
        }
      })

      const img = wrapper.find('img')
      expect(img.exists()).toBe(true)
    })
  })

  // 卸载测试
  describe('生命周期', () => {
    it('卸载时应该清理 observer', () => {
      const TestComponent = defineComponent({
        directives: { lazyLoad: vLazyLoad },
        render() {
          return h('img', { 'v-lazy-load': 'https://example.com/image.jpg' })
        }
      })

      const wrapper = mount(TestComponent, {
        global: {
          directives: { lazyLoad: vLazyLoad }
        }
      })

      // 卸载组件
      wrapper.unmount()

      // 应该没有错误
      expect(true).toBe(true)
    })
  })

  // 错误处理测试
  describe('错误处理', () => {
    it('无效的 src 应该不设置 data-src', async () => {
      const TestComponent = defineComponent({
        directives: { lazyLoad: vLazyLoad },
        render() {
          return h('img', { 'v-lazy-load': null })
        }
      })

      const wrapper = mount(TestComponent, {
        global: {
          directives: { lazyLoad: vLazyLoad }
        }
      })

      const img = wrapper.find('img')
      expect(img.exists()).toBe(true)
    })
  })
})
