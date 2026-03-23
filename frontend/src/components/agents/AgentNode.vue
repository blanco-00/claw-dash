<template>
  <div class="agent-node" :class="{ orphaned: data.orphaned }">
    <!-- 每个位置只有一个通用 handle，可以发起或接收连接 -->
    <!-- 方向完全由拖拽方向决定 -->
    <Handle type="source" position="top" id="top" class="handle handle-top" />
    <Handle type="source" position="bottom" id="bottom" class="handle handle-bottom" />
    <Handle type="source" position="left" id="left" class="handle handle-left" />
    <Handle type="source" position="right" id="right" class="handle handle-right" />
    <div class="agent-name">{{ data.label }}</div>
  </div>
</template>

<script setup lang="ts">
import { Handle } from '@vue-flow/core'

const props = defineProps<{
  data: {
    label: string
    orphaned?: boolean
  }
}>()
</script>

<style scoped>
.agent-node {
  padding: 12px 20px;
  background: var(--card);
  border: 2px solid var(--border);
  border-radius: 8px;
  min-width: 80px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: relative;
}

.agent-node.orphaned {
  border-color: var(--danger-color);
  background: var(--destructive);
}

.agent-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.handle {
  width: 12px !important;
  height: 12px !important;
  background: var(--primary) !important;
  border: 2px solid var(--border-color) !important;
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

.handle-top {
  top: -8px !important;
}

.handle-bottom {
  bottom: -8px !important;
}
</style>
