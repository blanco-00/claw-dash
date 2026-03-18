<script setup lang="ts">
defineProps<{
  version?: string
  loading?: boolean
}>()
</script>

<template>
  <el-card shadow="hover">
    <template #header>
      <span class="font-bold">版本信息</span>
    </template>
    
    <div v-if="loading" class="space-y-2">
      <el-skeleton :rows="2" animated />
    </div>
    
    <div v-else class="space-y-2">
      <div class="flex justify-between items-center">
        <span class="text-gray-500">OpenClaw</span>
        <el-tag type="primary">{{ version || 'unknown' }}</el-tag>
      </div>
      <div class="flex justify-between items-center">
        <span class="text-gray-500">Node.js</span>
        <span class="font-mono text-sm">{{ version || '-' }}</span>
      </div>
    </div>
  </el-card>
</template>
