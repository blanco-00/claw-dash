<script setup lang="ts">
import { Handle } from '@vue-flow/core'

const props = defineProps<{
  data: {
    label: string
    orphaned?: boolean
  }
}>()
</script>

<template>
  <div class="agent-node" :class="{ orphaned: data.orphaned }">
    <div class="agent-name">{{ data.label }}</div>
    <Handle type="source" position="right" id="source" :style="{ background: '#722ed1', width: '10px', height: '10px' }" />
    <Handle type="target" position="left" id="target" :style="{ background: '#722ed1', width: '10px', height: '10px' }" />
  </div>
</template>

<style scoped>
.agent-node {
  padding: 10px 14px;
  background: var(--card);
  border: 2px solid var(--border-color);
  border-radius: 8px;
  min-width: 80px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.agent-node.orphaned {
  border-color: #ef4444;
  background: #fef2f2;
}

.agent-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}
</style>
