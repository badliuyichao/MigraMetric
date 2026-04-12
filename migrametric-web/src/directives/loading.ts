import { ElLoading } from 'element-plus'
import type { Directive, DirectiveBinding } from 'vue'

/**
 * v-loading 指令扩展
 * 支持按键触发加载状态
 *
 * @example
 * <el-button v-loading="isLoading">按钮</el-button>
 * <el-button v-loading:[loadingText]="isLoading">带文字</el-button>
 *
 * 组合按键用法：
 * <el-button v-loading Combinator @click="handleClick">点击</el-button>
 */
interface LoadingDirective extends Directive {
  loadingInstance?: ReturnType<typeof ElLoading.service>
}

const vLoading: LoadingDirective = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    if (binding.value) {
      const options: Record<string, unknown> = {
        target: el.parentElement || el,
        fullscreen: false
      }

      // 如果有修饰符，使用对应样式
      if (binding.modifiers.fullscreen) {
        options.fullscreen = true
      }

      // 如果有参数，使用参数作为文字
      if (binding.arg) {
        options.text = binding.arg
      }

      el.style.position = 'relative'
      el.dataset.loading = 'true'
      vLoading.loadingInstance = ElLoading.service(options)
    }
  },

  updated(el: HTMLElement, binding: DirectiveBinding) {
    if (binding.value !== binding.oldValue) {
      if (binding.value) {
        // 显示加载
        const options: Record<string, unknown> = {
          target: el.parentElement || el,
          fullscreen: false
        }

        if (binding.modifiers.fullscreen) {
          options.fullscreen = true
        }

        if (binding.arg) {
          options.text = binding.arg
        }

        el.style.position = 'relative'
        el.dataset.loading = 'true'
        vLoading.loadingInstance = ElLoading.service(options)
      } else {
        // 隐藏加载
        if (vLoading.loadingInstance) {
          vLoading.loadingInstance.close()
          vLoading.loadingInstance = undefined
        }
        el.style.position = ''
        delete el.dataset.loading
      }
    }
  },

  unmounted() {
    if (vLoading.loadingInstance) {
      vLoading.loadingInstance.close()
      vLoading.loadingInstance = undefined
    }
  }
}

export default vLoading
