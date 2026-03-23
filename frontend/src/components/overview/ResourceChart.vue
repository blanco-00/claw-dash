<script setup lang="ts">
const props = defineProps<{
  loading?: boolean
  cpuUsage?: number
  memoryUsage?: number
  memoryUsed?: number
  memoryTotal?: number
}>()

function formatMemory(mb?: number): string {
  if (!mb) return '0MB'
  if (mb >= 1024) {
    return (mb / 1024).toFixed(1) + 'GB'
  }
  return mb + 'MB'
}
</script>

<template>
  <el-card shadow="hover">
    <template #header>
      <span class="font-bold">系统资源</span>
    </template>
    
    <div v-if="loading" class="h-40 flex items-center justify-center">
      <el-icon class="is-loading text-2xl text-gray-400"><Loading /></el-icon>
    </div>
    
    <div v-else class="space-y-4">
      <div>
        <div class="flex justify-between mb-1">
          <span class="text-sm text-gray-600">CPU 使用率</span>
          <span class="text-sm font-bold text-pink-500">
            {{ cpuUsage !== undefined && cpuUsage >= 0 ? cpuUsage + '%' : '不支持' }}
          </span>
        </div>
        <el-progress 
          v-if="cpuUsage !== undefined && cpuUsage >= 0"
          :percentage="cpuUsage" 
          :color="'#ec4899'" 
          :show-text="false" 
          :stroke-width="8" 
        />
        <div v-else class="text-xs text-gray-400">当前平台不支持获取 CPU 使用率</div>
      </div>
      
      <div>
        <div class="flex justify-between mb-1">
          <span class="text-sm text-gray-600">内存使用</span>
          <span class="text-sm font-bold text-purple-500">
            {{ memoryUsage || 0 }}%
            <span class="text-xs text-gray-400">
              ({{ formatMemory(memoryUsed) }} / {{ formatMemory(memoryTotal) }})
            </span>
          </span>
        </div>
        <el-progress 
          :percentage="memoryUsage || 0" 
          :color="'#8b5cf6'" 
          :show-text="false" 
          :stroke-width="8" 
        />
      </div>
    </div>
  </el-card>
</template>

<script lang="ts">
import { Loading } from '@element-plus/icons-vue'
export default {
  components: { Loading }
}
</script>
