<script setup lang="ts">
import { computed } from 'vue'

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

// SVG ring chart calculations
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

function getSegmentColor(index: number): string {
  return segments.value[index].color
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
  <el-card shadow="hover">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-bold">任务分布</span>
        <span class="text-xs text-gray-500">总计: {{ total }}</span>
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
            stroke="#e5e7eb"
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
          <text :x="center" :y="center - 5" text-anchor="middle" class="text-2xl font-bold fill-gray-700">
            {{ total }}
          </text>
          <text :x="center" :y="center + 15" text-anchor="middle" class="text-xs fill-gray-500">
            总任务
          </text>
        </svg>
      </div>
      
      <!-- Legend -->
      <div class="grid grid-cols-2 gap-2 mt-4 w-full">
        <div
          v-for="segment in segments"
          :key="segment.label"
          class="flex items-center gap-2 text-xs cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 rounded px-2 py-1"
          @click="handleClick(segment.label)"
        >
          <span class="w-2 h-2 rounded-full" :style="{ backgroundColor: segment.color }"></span>
          <span class="text-gray-600 dark:text-gray-400">{{ segment.label }}</span>
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
circle {
  transition: opacity 0.2s;
}
</style>
