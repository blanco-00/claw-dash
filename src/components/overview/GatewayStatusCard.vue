<script setup lang="ts">
import { computed } from 'vue'
import type { GatewayInfo } from '@/types/gateway'

const props = defineProps<{
  data?: GatewayInfo
  loading?: boolean
}>()

const statusColor = computed(() => {
  if (!props.data) return 'gray'
  switch (props.data.status) {
    case 'running': return 'green'
    case 'stopped': return 'red'
    default: return 'yellow'
  }
})

const statusText = computed(() => {
  if (!props.data) return '未知'
  switch (props.data.status) {
    case 'running': return '运行中'
    case 'stopped': return '已停止'
    default: return '错误'
  }
})
</script>

<template>
  <el-card shadow="hover" class="gateway-status-card">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-bold">Gateway 状态</span>
        <div class="flex items-center gap-2">
          <span 
            class="w-3 h-3 rounded-full"
            :class="{
              'bg-green-500': statusColor === 'green',
              'bg-red-500': statusColor === 'red',
              'bg-yellow-500': statusColor === 'yellow',
              'bg-gray-400': statusColor === 'gray'
            }"
          />
          <span>{{ statusText }}</span>
        </div>
      </div>
    </template>
    
    <div v-if="loading" class="h-20 flex items-center justify-center">
      <el-icon class="is-loading text-2xl text-gray-400"><Loading /></el-icon>
    </div>
    
    <div v-else-if="data" class="space-y-3">
      <div class="flex justify-between items-center">
        <span class="text-gray-500">进程ID</span>
        <span class="font-mono">{{ data.pid || '-' }}</span>
      </div>
      <div class="flex justify-between items-center">
        <span class="text-gray-500">端口</span>
        <span class="font-mono">{{ data.port || '-' }}</span>
      </div>
      <div class="flex justify-between items-center">
        <span class="text-gray-500">运行时长</span>
        <span>{{ data.uptime || '-' }}</span>
      </div>
    </div>
    
    <div v-else class="h-20 flex items-center justify-center text-gray-400">
      无数据
    </div>
  </el-card>
</template>

<script lang="ts">
import { Loading } from '@element-plus/icons-vue'
export default {
  components: { Loading }
}
</script>

<style scoped>
.gateway-status-card {
  transition: transform 0.2s;
}
.gateway-status-card:hover {
  transform: translateY(-2px);
}
</style>
