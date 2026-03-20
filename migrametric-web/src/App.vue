<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 初始化用户信息
onMounted(() => {
  // 如果有token，尝试获取用户信息
  if (userStore.token) {
    userStore.getUserInfo()
  }
})
</script>

<style>
#app {
  width: 100%;
  height: 100%;
}

html,
body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
}
</style>
