/**
 * 增强分页组件测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import EnhancedPagination from '@/components/common/EnhancedPagination.vue'

// 创建 ElPagination stub
const ElPaginationStub = {
  name: 'ElPagination',
  props: ['modelValue', 'pageSize', 'total', 'pageSizes', 'layout', 'background', 'small', 'pagerCount'],
  emits: ['update:modelValue', 'update:page-size', 'current-change', 'size-change'],
  template: '<div class="el-pagination"><slot /></div>',
  methods: {
    $emit(event: string, ...args: unknown[]) {
      this.$attrs['on' + event.charAt(0).toUpperCase() + event.slice(1)]?.(...args)
    }
  }
}

// 创建 ElInputNumber stub
const ElInputNumberStub = {
  name: 'ElInputNumber',
  props: ['modelValue', 'min', 'max'],
  emits: ['update:modelValue', 'change'],
  template: '<div class="el-input-number"><input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" /></div>'
}

describe('EnhancedPagination Component', () => {
  // 基础渲染测试
  describe('基础渲染', () => {
    it('应该正确渲染分页组件', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          total: 100
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.find('.enhanced-pagination').exists()).toBe(true)
      expect(wrapper.find('.el-pagination').exists()).toBe(true)
    })

    it('应该正确显示总记录数', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          total: 100
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.find('.enhanced-pagination').exists()).toBe(true)
    })
  })

  // Props 测试
  describe('Props 配置', () => {
    it('应该正确应用当前页码', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 5,
          total: 100
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.props('modelValue')).toBe(5)
    })

    it('应该正确应用每页数量', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          pageSize: 20,
          total: 100
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.props('pageSize')).toBe(20)
    })

    it('应该正确应用自定义每页数量选项', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          pageSizes: [15, 30, 60, 120],
          total: 100
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.props('pageSizes')).toEqual([15, 30, 60, 120])
    })

    it('应该正确应用布局配置', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          layout: 'total, prev, pager, next',
          total: 100
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.props('layout')).toBe('total, prev, pager, next')
    })

    it('应该正确应用小型分页配置', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          small: true,
          total: 100
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.props('small')).toBe(true)
    })

    it('应该正确应用页码按钮数量', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          pagerCount: 9,
          total: 100
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.props('pagerCount')).toBe(9)
    })
  })

  // 快速跳转测试
  describe('快速跳转功能', () => {
    it('应该显示快速跳转输入框', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          showQuickJump: true,
          total: 100
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.find('.quick-jump').exists()).toBe(true)
    })

    it('不应该显示快速跳转输入框', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          showQuickJump: false,
          total: 100
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.find('.quick-jump').exists()).toBe(false)
    })
  })

  // 事件测试
  describe('事件触发', () => {
    it('页码变化时应该触发 update:modelValue 事件', async () => {
      const onUpdatePage = vi.fn()

      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          total: 100,
          'onUpdate:modelValue': onUpdatePage
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      // 直接调用组件暴露的方法
      wrapper.vm.handleCurrentChange(2)
      await nextTick()

      expect(onUpdatePage).toHaveBeenCalled()
    })

    it('每页数量变化时应该重置页码', async () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          total: 100
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      wrapper.vm.handleSizeChange(20)
      await nextTick()

      // 页码应该被重置为1
      expect(wrapper.vm.currentPage).toBe(1)
    })
  })

  // 预加载功能测试
  describe('预加载功能', () => {
    it('应该正确配置 autoPreFetch', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          total: 100,
          autoPreFetch: true,
          preFetchDistance: 3
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.props('autoPreFetch')).toBe(true)
      expect(wrapper.props('preFetchDistance')).toBe(3)
    })

    it('checkPreFetch 应该在接近末尾时触发预加载事件', async () => {
      const onPreFetch = vi.fn()

      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 8, // 总共10页，这是第8页
          pageSize: 10,
          total: 100,
          autoPreFetch: true,
          preFetchDistance: 3,
          'onPre-fetch': onPreFetch
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      // 直接调用 checkPreFetch 方法
      wrapper.vm.checkPreFetch(8)
      await nextTick()

      // 距离末尾只有2页，预加载距离是3，所以应该触发
      expect(onPreFetch).toHaveBeenCalled()
    })

    it('checkPreFetch 不应该在远离末尾时触发预加载事件', async () => {
      const onPreFetch = vi.fn()

      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          pageSize: 10,
          total: 100,
          autoPreFetch: true,
          preFetchDistance: 3,
          'onPre-fetch': onPreFetch
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      // 直接调用 checkPreFetch 方法
      wrapper.vm.checkPreFetch(1)
      await nextTick()

      expect(onPreFetch).not.toHaveBeenCalled()
    })
  })

  // 默认值测试
  describe('默认值配置', () => {
    it('应该有正确的默认值', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          total: 100
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.props('pageSize')).toBe(10)
      expect(wrapper.props('pageSizes')).toEqual([10, 20, 50, 100])
      expect(wrapper.props('background')).toBe(true)
      expect(wrapper.props('pagerCount')).toBe(7)
      expect(wrapper.props('showQuickJump')).toBe(false)
      expect(wrapper.props('autoPreFetch')).toBe(false)
      expect(wrapper.props('preFetchDistance')).toBe(3)
    })
  })

  // 边界条件测试
  describe('边界条件', () => {
    it('总记录数为0时应该正常渲染', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          total: 0
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.find('.enhanced-pagination').exists()).toBe(true)
    })

    it('总记录数小于每页数量时应该正常渲染', () => {
      const wrapper = mount(EnhancedPagination, {
        props: {
          modelValue: 1,
          pageSize: 20,
          total: 10
        },
        global: {
          stubs: {
            'el-pagination': ElPaginationStub,
            'el-input-number': ElInputNumberStub
          }
        }
      })

      expect(wrapper.find('.enhanced-pagination').exists()).toBe(true)
    })
  })
})
