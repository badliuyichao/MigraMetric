<template>
  <div class="not-found-container">
    <div class="content">
      <h1>404</h1>
      <p>抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style lang="scss" scoped>
.not-found-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f2f3f5;

  .content {
    text-align: center;

    h1 {
      font-size: 120px;
      color: #409eff;
      margin: 0;
    }

    p {
      font-size: 18px;
      color: #606266;
      margin: 20px 0 30px;
    }
  }
}
</style>
