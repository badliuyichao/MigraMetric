import { ref, type Ref } from 'vue'
import { ElMessage, ElMessageBox, ElNotification, type MessageOptions } from 'element-plus'

/**
 * 异步操作结果
 */
interface AsyncResult<T> {
  data: Ref<T | null>
  loading: Ref<boolean>
  error: Ref<Error | null>
}

/**
 * 确认操作选项
 */
interface ConfirmOptions {
  title?: string
  message: string
  confirmText?: string
  cancelText?: string
  type?: 'warning' | 'info' | 'error' | 'success'
}

/**
 * 通知选项
 */
interface NotifyOptions {
  title: string
  message: string
  type?: 'success' | 'warning' | 'info' | 'error'
  duration?: number
}

/**
 * useAsync 组合函数
 * 提供统一的异步操作处理，包括加载状态、错误处理和用户反馈
 *
 * @example
 * const { data, loading, error, execute } = useAsync(fetchData)
 * await execute()
 *
 * // 带消息提示
 * const { execute } = useAsyncWithNotify(fetchData, {
 *   success: '操作成功',
 *   error: '操作失败'
 * })
 *
 * // 确认操作
 * const { execute } = useAsyncWithConfirm(submitForm, {
 *   title: '确认提交',
 *   message: '确定要提交吗？'
 * })
 */
export function useAsync<T>(
  asyncFn: (...args: unknown[]) => Promise<T>
): AsyncResult<T> & {
  execute: (...args: unknown[]) => Promise<T | null>
} {
  const data: Ref<T | null> = ref(null)
  const loading = ref(false)
  const error = ref<Error | null>(null)

  async function execute(...args: unknown[]): Promise<T | null> {
    loading.value = true
    error.value = null

    try {
      const result = await asyncFn(...args)
      data.value = result
      return result
    } catch (e) {
      error.value = e instanceof Error ? e : new Error(String(e))
      return null
    } finally {
      loading.value = false
    }
  }

  return {
    data,
    loading,
    error,
    execute
  }
}

/**
 * 带消息提示的异步操作
 *
 * @example
 * const { execute } = useAsyncWithNotify(
 *   () => api.save(data),
 *   {
 *     success: { title: '成功', message: '保存成功' },
 *     error: { title: '失败', message: '保存失败' }
 *   }
 * )
 */
export function useAsyncWithNotify<T>(
  asyncFn: (...args: unknown[]) => Promise<T>,
  options: {
    success?: string | MessageOptions
    error?: string | MessageOptions
    before?: () => void
  }
): AsyncResult<T> & {
  execute: (...args: unknown[]) => Promise<T | null>
} {
  const result = useAsync(asyncFn)

  const execute = async (...args: unknown[]): Promise<T | null> => {
    try {
      options.before?.()
      const data = await result.execute(...args)

      if (options.success) {
        if (typeof options.success === 'string') {
          ElMessage.success(options.success)
        } else {
          ElMessage.success(options.success as MessageOptions)
        }
      }

      return data
    } catch {
      if (options.error) {
        if (typeof options.error === 'string') {
          ElMessage.error(options.error)
        } else {
          ElMessage.error(options.error as MessageOptions)
        }
      }
      return null
    }
  }

  return {
    ...result,
    execute
  }
}

/**
 * 带确认框的异步操作
 *
 * @example
 * const { execute } = useAsyncWithConfirm(
 *   () => api.delete(id),
 *   { message: '确定要删除吗？', type: 'warning' }
 * )
 */
export function useAsyncWithConfirm<T>(
  asyncFn: (...args: unknown[]) => Promise<T>,
  confirmOptions: ConfirmOptions
): AsyncResult<T> & {
  execute: (...args: unknown[]) => Promise<T | null>
} {
  const result = useAsync(asyncFn)

  const execute = async (...args: unknown[]): Promise<T | null> => {
    try {
      await ElMessageBox.confirm(confirmOptions.message, confirmOptions.title || '确认', {
        confirmButtonText: confirmOptions.confirmText || '确定',
        cancelButtonText: confirmOptions.cancelText || '取消',
        type: confirmOptions.type || 'warning'
      })

      const data = await result.execute(...args)
      return data
    } catch {
      // 用户取消
      return null
    }
  }

  return {
    ...result,
    execute
  }
}

/**
 * 发送通知
 *
 * @example
 * notifySuccess('操作成功')
 * notifyError('操作失败')
 * notifyWarning('请注意')
 */
export function notify(options: NotifyOptions) {
  ElNotification({
    duration: options.duration ?? 3000,
    ...options
  })
}

export function notifySuccess(message: string, title = '成功') {
  notify({ title, message, type: 'success' })
}

export function notifyError(message: string, title = '错误') {
  notify({ title, message, type: 'error' })
}

export function notifyWarning(message: string, title = '警告') {
  notify({ title, message, type: 'warning' })
}

export function notifyInfo(message: string, title = '提示') {
  notify({ title, message, type: 'info' })
}
