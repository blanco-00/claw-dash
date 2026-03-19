<template>
  <svg class="v-custom-edge" :class="{ 'is-selected': selected }">
    <path :d="path" class="edge-path" :class="edgeType" />
    <path :d="path" class="edge-hit-area" @click="$emit('click', $event)" />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  id: string
  sourceX: number
  sourceY: number
  targetX: number
  targetY: number
  sourcePosition?: string
  targetPosition?: string
  type?: string
  selected?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  selected: false
})

defineEmits(['click'])

const path = computed(() => {
  const { sourceX, sourceY, targetX, targetY } = props

  if (props.type === 'step' || props.type === 'reports') {
    const midX = (sourceX + targetX) / 2
    return `M ${sourceX} ${sourceY} L ${midX} ${sourceY} L ${midX} ${targetY} L ${targetX} ${targetY}`
  }

  return `M ${sourceX} ${sourceY} C ${sourceX + 50} ${sourceY}, ${targetX - 50} ${targetY}, ${targetX} ${targetY}`
})

const edgeType = computed(() => {
  return props.type === 'step' || props.type === 'reports' ? 'step' : 'default'
})
</script>

<style scoped>
.v-custom-edge {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.edge-path {
  fill: none;
  stroke: #b1b1b7;
  stroke-width: 2;
  transition:
    stroke 0.2s,
    stroke-width 0.2s;
}

.edge-path.step {
  stroke-dasharray: 5 5;
}

.edge-path.assigns {
  stroke: #409eff;
}

.edge-path.reports {
  stroke: #67c23a;
}

.edge-hit-area {
  fill: none;
  stroke: transparent;
  stroke-width: 20;
  pointer-events: stroke;
  cursor: pointer;
}

.is-selected .edge-path {
  stroke: #409eff;
  stroke-width: 3;
}
</style>
