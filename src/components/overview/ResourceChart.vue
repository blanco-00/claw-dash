<script setup lang="ts">
import { computed } from 'vue'

defineProps<{
  loading?: boolean
}>()

// 模拟数据
const cpuValue = ref(35)
const memoryValue = ref(48)
</script>

<template>
  <el-card shadow="hover">
    <template #header>
      <span class="font-bold">系统资源</span>
    </template>
    
    <div v-if="loading" class="h-64 flex items-center justify-center">
      <el-icon class="is-loading text-2xl text-gray-400"><Loading /></el-icon>
    </div>
    
    <div v-else class="space-y-4">
      <div>
        <div class="flex justify-between mb-1">
          <span>CPU 使用率</span>
          <span class="text-pink-500">{{ cpuValue }}%</span>
        </div>
        <el-progress :percentage="cpuValue" :color="'#ec4899'" />
      </div>
      
      <div>
        <div class="flex justify-between mb-1">
          <span>内存使用</span>
          <span class="text-purple-500">{{ memoryValue }}%</span>
        </div>
        <el-progress :percentage="memoryValue" :color="'#8b5cf6'" />
      </div>
    </div>
  </el-card>
</template>

<script lang="ts">
import { Loading } from '@element-plus/icons-vue'
import { ref } from 'vue'
export default {
  components: { Loading }
}
</script>
