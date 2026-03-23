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
  <el-card shadow="hover" class="agents-panel-card">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-bold panel-title">活跃 Agent</span>
        <span class="text-xs panel-subtitle">
          {{ onlineCount }}/{{ agents.length }} 在线
        </span>
      </div>
    </template>
    
    <div v-if="loading" class="h-48 flex items-center justify-center">
      <el-icon class="is-loading text-2xl text-gray-400"><Loading /></el-icon>
    </div>
    
    <div v-else-if="agents.length === 0" class="h-48 flex items-center justify-center empty-text">
      <div class="text-center">
        <div class="text-4xl mb-2">👩‍💼</div>
        <div>暂无 Agent</div>
      </div>
    </div>
    
    <div v-else class="agent-grid">
      <div
        v-for="agent in agents"
        :key="agent.id"
        class="agent-item"
        :class="{ online: isOnline(agent.status) }"
        @click="handleClick(agent.id)"
      >
        <div class="agent-status-dot"></div>
        <div class="agent-name">{{ agent.name }}</div>
        <div v-if="agent.title" class="agent-title">{{ agent.title }}</div>
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

<style scoped>
.agents-panel-card {
  background-color: var(--card) !important;
  border-color: var(--border) !important;
}

.panel-title {
  color: var(--text-primary);
}

.panel-subtitle {
  color: var(--text-secondary);
}

.empty-text {
  color: var(--text-secondary);
  opacity: 0.7;
}

.agent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
}

.agent-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  position: relative;
}

.agent-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.agent-item.online {
  border-color: var(--primary);
  background: linear-gradient(135deg, var(--bg-secondary) 0%, rgba(114, 46, 209, 0.08) 100%);
}

.dark .agent-item {
  background: var(--card);
}

.dark .agent-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.agent-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--text-secondary);
  margin-bottom: 8px;
}

.agent-item.online .agent-status-dot {
  background: var(--success-color);
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.5);
}

.agent-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  text-align: center;
  word-break: break-word;
}

.agent-title {
  font-size: 11px;
  color: var(--text-secondary);
  text-align: center;
  margin-top: 4px;
}
</style>
