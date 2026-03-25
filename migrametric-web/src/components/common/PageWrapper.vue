<template>
  <div class="page-wrapper" :class="{ 'is-loading': loading, 'is-empty': isEmpty, 'is-error': hasError }">
    <!-- 加载状态 -->
    <template v-if="loading">
      <slot name="loading">
        <LoadingSkeleton v-if="showSkeleton" :type="skeletonType" :rows="skeletonRows" />
      </slot>
    </template>

    <!-- 错误状态 -->
    <template v-else-if="hasError">
      <slot name="error">
        <ErrorState
          :title="errorTitle"
          :message="errorMessage"
          :icon-size="errorIconSize"
          @retry="handleRetry"
        />
      </slot>
    </template>

    <!-- 空状态 -->
    <template v-else-if="isEmpty && showEmpty">
      <slot name="empty">
        <EmptyState :description="emptyDescription" />
      </slot>
    </template>

    <!-- 正常内容 -->
    <template v-else>
      <slot />
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 页面状态包装组件
 * 统一处理加载、空数据、错误三种状态
 *
 * @example
 * <PageWrapper
 *   :loading="loading"
 *   :data="tableData"
 *   empty-description="暂无项目"
 *   @retry="loadData"
 * >
 *   <el-table :data="tableData">...</el-table>
 * </PageWrapper>
 */
import EmptyState from './EmptyState.vue'
import ErrorState from './ErrorState.vue'
import LoadingSkeleton from './LoadingSkeleton.vue'
import { computed } from 'vue'

const props = defineProps({
  /**
   * 是否加载中
   */
  loading: {
    type: Boolean,
    default: false
  },
  /**
   * 数据列表
   */
  data: {
    type: Array,
    default: () => []
  },
  /**
   * 是否有错误
   */
  hasError: {
    type: Boolean,
    default: false
  },
  /**
   * 错误标题
   */
  errorTitle: {
    type: String,
    default: '加载失败'
  },
  /**
   * 错误信息
   */
  errorMessage: {
    type: String,
    default: '数据加载失败，请稍后重试'
  },
  /**
   * 错误图标大小
   */
  errorIconSize: {
    type: Number,
    default: 48
  },
  /**
   * 是否显示空状态
   */
  showEmpty: {
    type: Boolean,
    default: true
  },
  /**
   * 空状态描述
   */
  emptyDescription: {
    type: String,
    default: '暂无数据'
  },
  /**
   * 是否显示骨架屏
   */
  showSkeleton: {
    type: Boolean,
    default: true
  },
  /**
   * 骨架屏类型
   */
  skeletonType: {
    type: String as () => 'table' | 'card' | 'list' | 'custom',
    default: 'table'
  },
  /**
   * 骨架屏行数
   */
  skeletonRows: {
    type: Number,
    default: 5
  }
})

const emit = defineEmits<{
  /**
   * 重试事件
   */
  retry: []
}>()

// 是否为空（数据为空）
const isEmpty = computed(() => {
  return props.data && Array.isArray(props.data) && props.data.length === 0
})

function handleRetry() {
  emit('retry')
}
</script>

<style lang="scss" scoped>
.page-wrapper {
  width: 100%;
  min-height: 200px;
}
</style>
