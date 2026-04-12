<template>
  <div class="enhanced-pagination">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="currentPageSize"
      :total="total"
      :page-sizes="pageSizes"
      :layout="layout"
      :background="background"
      :small="small"
      :pager-count="pagerCount"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <!-- 快速跳转 -->
    <div v-if="showQuickJump" class="quick-jump">
      <span>跳至</span>
      <el-input-number
        v-model="jumpPage"
        :min="1"
        :max="totalPages"
        size="small"
        controls-position="right"
        @change="handleQuickJump"
      />
      <span>页</span>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 增强分页组件
 * 支持快速跳转、预加载下一页等功能
 *
 * @example
 * <EnhancedPagination
 *   v-model:page="currentPage"
 *   v-model:page-size="pageSize"
 *   :total="total"
 *   :auto-fetch="true"
 *   :pre-fetch-distance="3"
 *   @page-change="handlePageChange"
 *   @pre-fetch="handlePreFetch"
 * />
 */
import { ref, computed, watch } from 'vue'

const props = defineProps({
  /**
   * 当前页码
   */
  modelValue: {
    type: Number,
    default: 1
  },
  /**
   * 每页数量
   */
  pageSize: {
    type: Number,
    default: 10
  },
  /**
   * 总记录数
   */
  total: {
    type: Number,
    default: 0
  },
  /**
   * 每页数量选项
   */
  pageSizes: {
    type: Array as () => number[],
    default: () => [10, 20, 50, 100]
  },
  /**
   * 布局
   */
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  /**
   * 是否显示背景
   */
  background: {
    type: Boolean,
    default: true
  },
  /**
   * 是否使用小型分页
   */
  small: {
    type: Boolean,
    default: false
  },
  /**
   * 页码按钮数量
   */
  pagerCount: {
    type: Number,
    default: 7
  },
  /**
   * 是否显示快速跳转
   */
  showQuickJump: {
    type: Boolean,
    default: false
  },
  /**
   * 是否启用预加载
   */
  autoPreFetch: {
    type: Boolean,
    default: false
  },
  /**
   * 预加载距离（距离底部多少页时触发）
   */
  preFetchDistance: {
    type: Number,
    default: 3
  }
})

const emit = defineEmits<{
  'update:modelValue': [page: number]
  'update:pageSize': [size: number]
  'page-change': [page: number, pageSize: number]
  'size-change': [size: number]
  'pre-fetch': [page: number, pageSize: number]
}>()

const currentPage = ref(props.modelValue)
const currentPageSize = ref(props.pageSize)
const jumpPage = ref(1)

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(props.total / currentPageSize.value)
})

// 监听外部值变化
watch(() => props.modelValue, (val) => {
  currentPage.value = val
})

watch(() => props.pageSize, (val) => {
  currentPageSize.value = val
})

// 页码变化
function handleCurrentChange(page: number) {
  emit('update:modelValue', page)
  emit('page-change', page, currentPageSize.value)

  // 检查是否需要预加载
  if (props.autoPreFetch) {
    checkPreFetch(page)
  }
}

// 每页数量变化
function handleSizeChange(size: number) {
  // 切换每页数量时，重置到第一页
  currentPage.value = 1
  emit('update:modelValue', 1)
  emit('update:pageSize', size)
  emit('size-change', size)
  emit('page-change', 1, size)
}

// 快速跳转
function handleQuickJump(page: number) {
  if (page && page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    emit('update:modelValue', page)
    emit('page-change', page, currentPageSize.value)
  }
}

// 预加载检查
function checkPreFetch(currentPage: number) {
  const distanceToEnd = totalPages.value - currentPage
  if (distanceToEnd <= props.preFetchDistance && distanceToEnd > 0) {
    emit('pre-fetch', currentPage + 1, currentPageSize.value)
  }
}
</script>

<style lang="scss" scoped>
.enhanced-pagination {
  display: flex;
  align-items: center;
  gap: 16px;

  &.is-flex-wrap {
    flex-wrap: wrap;
    gap: 12px;
  }
}

.quick-jump {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--el-text-color-regular);

  :deep(.el-input-number) {
    width: 80px;
  }
}
</style>
