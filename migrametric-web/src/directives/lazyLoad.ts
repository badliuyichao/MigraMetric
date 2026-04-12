import { type Directive, type DirectiveBinding } from 'vue'

/**
 * v-lazy-load 图片懒加载指令
 * 使用 Intersection Observer API 实现图片懒加载
 *
 * @example
 * <img v-lazy-load="imageUrl" />
 * <img v-lazy-load:[offset]="imageUrl" />
 *
 * 修饰符：
 * - fade: 图片加载完成后淡入显示
 */
interface LazyLoadDirective extends Directive {
  observer?: IntersectionObserver
  cachedSrc?: string
}

function getImageUrl(el: HTMLElement, binding: DirectiveBinding): string | null {
  const value = binding.value
  if (typeof value === 'string') {
    return value
  }
  if (typeof value === 'object' && value !== null) {
    return (value as { src?: string }).src || null
  }
  return el.getAttribute('data-src') || null
}

function loadImage(src: string): Promise<string> {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(src)
    img.onerror = reject
    img.src = src
  })
}

const vLazyLoad: LazyLoadDirective = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    // 保存原始 src
    vLazyLoad.cachedSrc = el.getAttribute('src') || ''

    // 设置占位背景色
    el.style.backgroundColor = 'var(--el-fill-color-light)'

    // 获取图片 URL
    const src = getImageUrl(el, binding)
    if (!src) return

    // 设置 data-src 用于懒加载
    el.setAttribute('data-src', src)

    // 初始时移除 src，避免立即加载
    el.removeAttribute('src')

    // 检查是否立即加载（修饰符 immediate）
    if (binding.modifiers.immediate) {
      loadImage(src)
        .then(() => {
          el.setAttribute('src', src)
          el.style.backgroundColor = ''
        })
        .catch(() => {
          console.warn('图片加载失败:', src)
        })
      return
    }

    // 创建 Intersection Observer
    const options: IntersectionObserverInit = {
      root: null,
      rootMargin: '0px 0px 100px 0px', // 提前 100px 开始加载
      threshold: 0
    }

    // 检查是否有自定义偏移量
    const offsetValue = binding.arg
    if (offsetValue && !isNaN(Number(offsetValue))) {
      options.rootMargin = `0px 0px ${offsetValue}px 0px`
    }

    vLazyLoad.observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const currentSrc = el.getAttribute('data-src')
          if (currentSrc) {
            loadImage(currentSrc)
              .then(() => {
                el.setAttribute('src', currentSrc)

                // 淡入效果
                if (binding.modifiers.fade) {
                  el.style.opacity = '0'
                  el.style.transition = 'opacity 0.3s ease-in-out'
                  requestAnimationFrame(() => {
                    el.style.opacity = '1'
                  })
                }

                el.style.backgroundColor = ''
                // 停止观察已加载的图片
                vLazyLoad.observer?.unobserve(el)
              })
              .catch(() => {
                console.warn('图片加载失败:', currentSrc)
              })
          }
        }
      })
    }, options)

    vLazyLoad.observer.observe(el)
  },

  updated(el: HTMLElement, binding: DirectiveBinding, prevBinding: DirectiveBinding) {
    // 如果值变化，重新设置
    if (binding.value !== prevBinding.value) {
      const newSrc = getImageUrl(el, binding)
      if (newSrc) {
        el.setAttribute('data-src', newSrc)
      }
    }
  },

  unmounted() {
    // 清理 observer
    if (vLazyLoad.observer) {
      vLazyLoad.observer.disconnect()
      vLazyLoad.observer = undefined
    }
  }
}

export default vLazyLoad
