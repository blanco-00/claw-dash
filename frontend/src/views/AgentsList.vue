<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAllAgentDetails } from '@/api/agents'
import type { AgentInfo } from '@/types/agent'
import AgentListTab from '@/components/agents/AgentListTab.vue'

const loading = ref(true)
const agents = ref<AgentInfo[]>([])

async function refresh() {
  loading.value = true
  try {
    agents.value = await getAllAgentDetails()
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
  <AgentListTab :agents="agents" :loading="loading" @refresh="refresh" />
</template>

<style scoped>
</style>
