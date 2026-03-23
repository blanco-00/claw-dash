<script setup lang="ts">
export interface Agent {
  id: string
  name: string
  title?: string
  status: string
}

const props = defineProps<{
  agents: Agent[]
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'view', agentId: string): void
}>()

const onlineCount = computed(() => 
  props.agents.filter(a => a.status === 'online' || a.status === 'active').length
)

function isOnline(status: string): boolean {
  return status === 'online' || status === 'active'
}

function handleClick(agentId: string) {
  emit('view', agentId)
}
</script>

<template>
  <el-card shadow="hover">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-bold">活跃 Agent</span>
        <span class="text-xs text-gray-500">
          {{ onlineCount }}/{{ agents.length }} 在线
        </span>
      </div>
    </template>
    
    <div v-if="loading" class="h-48 flex items-center justify-center">
      <el-icon class="is-loading text-2xl text-gray-400"><Loading /></el-icon>
    </div>
    
    <div v-else-if="agents.length === 0" class="h-48 flex items-center justify-center text-gray-400">
      <div class="text-center">
        <div class="text-4xl mb-2">👩‍💼</div>
        <div>暂无 Agent</div>
      </div>
    </div>
    
    <div v-else class="space-y-2">
      <div
        v-for="agent in agents"
        :key="agent.id"
        class="flex items-center gap-3 p-2 hover:bg-gray-50 dark:hover:bg-gray-800 rounded cursor-pointer transition-colors"
        @click="handleClick(agent.id)"
      >
        <!-- Status indicator -->
        <span
          class="w-2.5 h-2.5 rounded-full flex-shrink-0"
          :class="isOnline(agent.status) ? 'bg-green-500' : 'bg-gray-300'"
        ></span>
        
        <!-- Agent info -->
        <div class="flex-1 min-w-0">
          <div class="font-medium text-sm truncate">
            {{ agent.name }}
          </div>
          <div v-if="agent.title" class="text-xs text-gray-500 truncate">
            {{ agent.title }}
          </div>
        </div>
        
        <!-- Status text -->
        <span
          class="text-xs px-2 py-0.5 rounded"
          :class="isOnline(agent.status) 
            ? 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300' 
            : 'bg-gray-100 text-gray-500 dark:bg-gray-700 dark:text-gray-400'"
        >
          {{ isOnline(agent.status) ? '在线' : '离线' }}
        </span>
      </div>
    </div>
  </el-card>
</template>

<script lang="ts">
import { Loading } from '@element-plus/icons-vue'
import { computed } from 'vue'
export default {
  components: { Loading }
}
</script>
