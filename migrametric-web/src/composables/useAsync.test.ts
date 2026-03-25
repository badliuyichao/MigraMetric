import { describe, it, expect, vi } from 'vitest'
import { useAsync, useAsyncWithNotify } from '@/composables/useAsync'

describe('useAsync 组合函数测试', () => {
  it('应该正确处理异步操作成功', async () => {
    const mockFn = vi.fn().mockResolvedValue('success')
    const { data, loading, error, execute } = useAsync(mockFn)

    expect(loading.value).toBe(false)
    expect(data.value).toBe(null)

    await execute()

    expect(loading.value).toBe(false)
    expect(data.value).toBe('success')
    expect(error.value).toBe(null)
    expect(mockFn).toHaveBeenCalledTimes(1)
  })

  it('应该正确处理异步操作失败', async () => {
    const testError = new Error('Test error')
    const mockFn = vi.fn().mockRejectedValue(testError)
    const { data, loading, error: catchedError, execute } = useAsync(mockFn)

    await execute()

    expect(loading.value).toBe(false)
    expect(data.value).toBe(null)
    expect(catchedError.value).toBe(testError)
  })

  it('应该在加载中状态时设置loading为true', async () => {
    let resolveFn: (value: string) => void
    const mockFn = vi.fn().mockImplementation(() => {
      return new Promise<string>((resolve) => {
        resolveFn = resolve
      })
    })

    const { loading, execute } = useAsync(mockFn)

    // 启动但不等待
    const promise = execute()

    // 此时应该是加载中
    expect(loading.value).toBe(true)

    // 完成
    resolveFn!('done')
    await promise

    expect(loading.value).toBe(false)
  })

  it('应该正确传递参数给异步函数', async () => {
    const mockFn = vi.fn().mockResolvedValue('result')
    const { execute } = useAsync(mockFn)

    await execute('arg1', 'arg2')

    expect(mockFn).toHaveBeenCalledWith('arg1', 'arg2')
  })
})

describe('useAsyncWithNotify 组合函数测试', () => {
  it('应该正确使用成功消息', async () => {
    const mockFn = vi.fn().mockResolvedValue('success')
    const { execute } = useAsyncWithNotify(mockFn, {
      success: '操作成功'
    })

    await execute()

    expect(mockFn).toHaveBeenCalled()
  })

  it('应该正确使用错误消息', async () => {
    const mockFn = vi.fn().mockRejectedValue(new Error('error'))
    const { execute } = useAsyncWithNotify(mockFn, {
      error: '操作失败'
    })

    await execute()

    expect(mockFn).toHaveBeenCalled()
  })
})
