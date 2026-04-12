<template>
  <div class="error-state">
    <div class="error-content">
      <el-icon class="error-icon" :size="iconSize">
        <WarningFilled />
      </el-icon>
      <h3 class="error-title">{{ title }}</h3>
      <p class="error-message">{{ message }}</p>
      <div class="error-actions">
        <slot>
          <el-button type="primary" @click="handleRetry">
            <el-icon><Refresh /></el-icon>
            重试
          </el-button>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 错误状态组件
 * 用于数据加载失败时展示错误信息和重试按钮
 *
 * @example
 * <ErrorState
 *   message="加载失败，请稍后重试"
 *   @retry="loadData"
 * />
 */
import { WarningFilled, Refresh } from '@element-plus/icons-vue'

defineProps({
  /**
   * 错误标题
   */
  title: {
    type: String,
    default: '加载失败'
  },
  /**
   * 错误信息
   */
  message: {
    type: String,
    default: '数据加载失败，请稍后重试'
  },
  /**
   * 图标大小
   */
  iconSize: {
    type: Number,
    default: 48
  }
})

const emit = defineEmits<{
  /**
   * 重试事件
   */
  retry: []
}>()

function handleRetry() {
  emit('retry')
}
</script>

<style lang="scss" scoped>
.error-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
}

.error-content {
  text-align: center;
  max-width: 400px;
}

.error-icon {
  color: var(--el-color-danger);
  margin-bottom: 16px;
}

.error-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.error-message {
  margin: 0 0 24px 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
