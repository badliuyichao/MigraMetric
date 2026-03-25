<template>
  <div class="empty-state">
    <el-empty :image-size="imageSize" :description="description">
      <template v-if="$slots.image" #image>
        <slot name="image" />
      </template>
      <template v-if="$slots.default" #default>
        <slot />
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
/**
 * 空状态组件
 * 用于表格、列表等数据为空时展示
 *
 * @example
 * <EmptyState description="暂无数据">
 *   <el-button type="primary">新增</el-button>
 * </EmptyState>
 */
defineProps({
  /**
   * 描述文字
   */
  description: {
    type: String,
    default: '暂无数据'
  },
  /**
   * 图片大小
   */
  imageSize: {
    type: Number,
    default: 100
  }
})
</script>

<style lang="scss" scoped>
.empty-state {
  padding: 40px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
