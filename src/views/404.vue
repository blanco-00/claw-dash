<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="h-screen flex items-center justify-center bg-gray-50">
    <div class="text-center">
      <h1 class="text-9xl font-bold text-pink-500">404</h1>
      <p class="text-xl text-gray-600 mt-4">页面不存在</p>
      <el-button type="primary" class="mt-6" @click="router.push('/')">
        返回首页
      </el-button>
    </div>
  </div>
</template>
