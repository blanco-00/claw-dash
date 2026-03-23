<script setup lang="ts">
import { computed, ref } from 'vue'

export interface TaskStats {
  pending: number
  running: number
  completed: number
  failed: number
}

const props = defineProps<{
  stats: TaskStats
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'filter', status: string): void
}>()

const isDark = ref(document.documentElement.classList.contains('dark'))

// Listen for dark mode changes
if (typeof window !== 'undefined') {
  const observer = new MutationObserver(() => {
    isDark.value = document.documentElement.classList.contains('dark')
  })
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
}

const total = computed(() => 
  props.stats.pending + props.stats.running + props.stats.completed + props.stats.failed
)

const segments = computed(() => {
  const t = total.value || 1
  return [
    { label: '待处理', value: props.stats.pending, percent: Math.round(props.stats.pending / t * 100), color: '#f59e0b' },
    { label: '运行中', value: props.stats.running, percent: Math.round(props.stats.running / t * 100), color: '#8b5cf6' },
    { label: '已完成', value: props.stats.completed, percent: Math.round(props.stats.completed / t * 100), color: '#10b981' },
    { label: '失败', value: props.stats.failed, percent: Math.round(props.stats.failed / t * 100), color: '#ef4444' },
  ]
})

const radius = 60
const strokeWidth = 16
const circumference = 2 * Math.PI * radius
const center = 80

function getStrokeDashoffset(index: number): number {
  let offset = 0
  for (let i = 0; i < index; i++) {
    offset += segments.value[i].percent / 100 * circumference
  }
  return offset
}

function getStrokeDasharray(index: number): string {
  const percent = segments.value[index].percent
  return `${percent / 100 * circumference} ${circumference}`
}

function handleClick(label: string) {
  emit('filter', label)
}
</script>

<template>
  <el-card shadow="hover" class="task-dist-card">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-bold chart-title">任务分布</span>
        <span class="text-xs chart-subtitle">总计: {{ total }}</span>
      </div>
    </template>
    
    <div v-if="loading" class="h-48 flex items-center justify-center">
      <el-icon class="is-loading text-2xl text-gray-400"><Loading /></el-icon>
    </div>
    
    <div v-else class="flex flex-col items-center">
      <!-- Ring Chart -->
      <div class="relative">
        <svg width="160" height="160" viewBox="0 0 160 160">
          <!-- Background ring -->
          <circle
            :cx="center"
            :cy="center"
            :r="radius"
            fill="none"
            :stroke="isDark ? '#374151' : '#e5e7eb'"
            :stroke-width="strokeWidth"
          />
          <!-- Segments -->
          <circle
            v-for="(segment, index) in segments"
            :key="segment.label"
            :cx="center"
            :cy="center"
            :r="radius"
            fill="none"
            :stroke="segment.color"
            :stroke-width="strokeWidth"
            :stroke-dasharray="getStrokeDasharray(index)"
            :stroke-dashoffset="-getStrokeDashoffset(index)"
            stroke-linecap="butt"
            class="cursor-pointer transition-opacity hover:opacity-80"
            @click="handleClick(segment.label)"
          />
          <!-- Center text -->
          <text :x="center" :y="center - 5" text-anchor="middle" :fill="isDark ? '#f9fafb' : '#374151'" font-size="24" font-weight="bold">
            {{ total }}
          </text>
          <text :x="center" :y="center + 15" text-anchor="middle" :fill="isDark ? '#9ca3af' : '#6b7280'" font-size="12">
            总任务
          </text>
        </svg>
      </div>
      
      <!-- Legend -->
      <div class="grid grid-cols-2 gap-2 mt-4 w-full">
        <div
          v-for="segment in segments"
          :key="segment.label"
          class="flex items-center gap-2 text-xs cursor-pointer legend-item rounded px-2 py-1"
          @click="handleClick(segment.label)"
        >
          <span class="w-2 h-2 rounded-full" :style="{ backgroundColor: segment.color }"></span>
          <span class="legend-label">{{ segment.label }}</span>
          <span class="ml-auto font-bold" :style="{ color: segment.color }">{{ segment.value }}</span>
        </div>
      </div>
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
.task-dist-card {
  background-color: var(--card);
  border-color: var(--border);
}

.chart-title {
  color: var(--text-primary);
}

.chart-subtitle {
  color: var(--text-secondary);
}

.legend-item:hover {
  background-color: var(--hover-bg);
}

.legend-label {
  color: var(--text-secondary);
}

circle {
  transition: opacity 0.2s;
}
</style>
