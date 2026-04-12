<template>
  <div class="loading-skeleton">
    <!-- 表格骨架屏 -->
    <template v-if="type === 'table'">
      <el-skeleton :rows="rows" animated />
    </template>

    <!-- 卡片骨架屏 -->
    <template v-else-if="type === 'card'">
      <div class="card-skeleton">
        <el-skeleton v-for="i in rows" :key="i" animated class="card-item">
          <template #template>
            <el-skeleton-item variant="h3" style="width: 50%" />
            <el-skeleton-item variant="text" style="margin-top: 12px" />
            <el-skeleton-item variant="text" style="margin-top: 8px; width: 80%" />
            <el-skeleton-item variant="text" style="margin-top: 8px; width: 60%" />
          </template>
        </el-skeleton>
      </div>
    </template>

    <!-- 列表骨架屏 -->
    <template v-else-if="type === 'list'">
      <div class="list-skeleton">
        <div v-for="i in rows" :key="i" class="list-item">
          <el-skeleton :rows="1" animated />
        </div>
      </div>
    </template>

    <!-- 自定义骨架屏 -->
    <template v-else>
      <el-skeleton :rows="rows" animated />
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 骨架屏组件
 * 用于内容加载时展示骨架效果，提升用户体验
 *
 * @example
 * <LoadingSkeleton type="table" :rows="5" />
 * <LoadingSkeleton type="card" :rows="3" />
 */
defineProps({
  /**
   * 骨架屏类型
   * - table: 表格骨架屏
   * - card: 卡片骨架屏
   * - list: 列表骨架屏
   * - custom: 自定义骨架屏
   */
  type: {
    type: String as () => 'table' | 'card' | 'list' | 'custom',
    default: 'table'
  },
  /**
   * 骨架屏行数
   */
  rows: {
    type: Number,
    default: 5
  }
})
</script>

<style lang="scss" scoped>
.loading-skeleton {
  width: 100%;
  padding: 20px 0;
}

.card-skeleton {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.card-item {
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.list-skeleton {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-item {
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 4px;
}
</style>
