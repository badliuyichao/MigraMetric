<template>
  <div
    ref="containerRef"
    class="virtual-list"
    :style="{ height: `${height}px`, overflowY: 'auto' }"
    @scroll="handleScroll"
  >
    <div class="virtual-list-spacer" :style="{ height: `${totalHeight}px` }">
      <div
        class="virtual-list-content"
        :style="{ transform: `translateY(${offsetY}px)` }"
      >
        <slot
          v-for="item in visibleItems"
          :key="getItemKey(item, startIndex + visibleItems.indexOf(item))"
          :item="item"
          :index="startIndex + visibleItems.indexOf(item)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 虚拟列表组件
 * 用于大数据列表的高性能渲染，仅渲染可视区域内的数据
 *
 * @example
 * <VirtualList
 *   :data="largeDataList"
 *   :item-height="50"
 *   :buffer="5"
 *   :height="400"
 * >
 *   <template #default="{ item, index }">
 *     <div class="list-item">{{ item.name }} - {{ index }}</div>
 *   </template>
 * </VirtualList>
 */
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  /**
   * 数据列表
   */
  data: {
    type: Array,
    required: true
  },
  /**
   * 每项高度（px）
   */
  itemHeight: {
    type: Number,
    default: 50
  },
  /**
   * 缓冲项数量（上下各渲染的额外项数）
   */
  buffer: {
    type: Number,
    default: 3
  },
  /**
   * 容器高度（px）
   */
  height: {
    type: Number,
    default: 400
  },
  /**
   * 获取唯一键的函数
   */
  getKey: {
    type: Function as () => (item: unknown, index: number) => string | number,
    default: (item: { id?: string | number }, index: number) => item.id ?? index
  }
})

const containerRef = ref<HTMLElement>()
const scrollTop = ref(0)

// 计算总高度
const totalHeight = computed(() => props.data.length * props.itemHeight)

// 计算可视区域能显示的项数
const visibleCount = computed(() => {
  return Math.ceil(props.height / props.itemHeight) + props.buffer * 2
})

// 计算起始索引
const startIndex = computed(() => {
  return Math.max(0, Math.floor(scrollTop.value / props.itemHeight) - props.buffer)
})

// 计算偏移量
const offsetY = computed(() => {
  return startIndex.value * props.itemHeight
})

// 计算可见项
const visibleItems = computed(() => {
  const end = Math.min(startIndex.value + visibleCount.value, props.data.length)
  return props.data.slice(startIndex.value, end)
})

// 获取项的唯一键
function getItemKey(item: unknown, index: number): string | number {
  return props.getKey(item, index)
}

// 处理滚动
function handleScroll() {
  if (containerRef.value) {
    scrollTop.value = containerRef.value.scrollTop
  }
}

// 滚动到指定索引
function scrollToIndex(index: number) {
  if (containerRef.value) {
    containerRef.value.scrollTop = index * props.itemHeight
  }
}

// 滚动到顶部
function scrollToTop() {
  scrollToIndex(0)
}

// 滚动到底部
function scrollToBottom() {
  scrollToIndex(props.data.length - 1)
}

// 暴露方法给父组件
defineExpose({
  scrollToIndex,
  scrollToTop,
  scrollToBottom
})
</script>

<style lang="scss" scoped>
.virtual-list {
  position: relative;
  will-change: transform;
}

.virtual-list-spacer {
  position: relative;
}

.virtual-list-content {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  will-change: transform;
}
</style>
