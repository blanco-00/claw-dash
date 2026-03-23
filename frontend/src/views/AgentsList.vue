<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getAllAgentDetails } from '@/api/agents'
import type { AgentInfo } from '@/types/agent'
import AgentListTab from '@/components/agents/AgentListTab.vue'

const loading = ref(true)
const agents = ref<AgentInfo[]>([])
const lastSyncTime = ref<string | null>(null)

const lastSyncDisplay = computed(() => {
  if (!lastSyncTime.value) return ''
  return new Date(lastSyncTime.value).toLocaleTimeString()
})

async function refresh() {
  loading.value = true
  try {
    agents.value = await getAllAgentDetails()
    lastSyncTime.value = new Date().toISOString()
  } catch (error) {
    console.error('Failed to load agents:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="agents-list-view">
    <div class="sync-info" v-if="lastSyncTime">
      <span class="sync-time">Last synced: {{ lastSyncDisplay }}</span>
    </div>
    <AgentListTab :agents="agents" :loading="loading" @refresh="refresh" />
  </div>
</template>

<style scoped>
.agents-list-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sync-info {
  padding: 8px 16px;
  color: var(--text-secondary);
  font-size: 12px;
}
</style>

<style scoped>
</style>
