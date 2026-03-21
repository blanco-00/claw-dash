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
    <Handle type="source" position="right" id="source" class="handle handle-right" />
    <Handle type="target" position="left" id="target" class="handle handle-left" />
    <div class="agent-name">{{ data.label }}</div>
  </div>
</template>

<style scoped>
.agent-node {
  padding: 12px 20px;
  background: #ffffff;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  min-width: 80px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: relative;
}

.agent-node.orphaned {
  border-color: #ef4444;
  background: #fef2f2;
}

.agent-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.handle {
  width: 12px !important;
  height: 12px !important;
  background: #722ed1 !important;
  border: 2px solid #ffffff !important;
  border-radius: 50%;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.handle:hover {
  opacity: 1;
}

.handle-left {
  left: -8px !important;
}

.handle-right {
  right: -8px !important;
}
</style>
