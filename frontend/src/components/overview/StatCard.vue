<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  title: string
  value: number | string
  icon: string
  color?: 'pink' | 'blue' | 'green' | 'orange' | 'purple'
  loading?: boolean
  trend?: 'up' | 'down' | 'neutral'
  subtitle?: string
}>()

const colorClass = computed(() => {
  switch (props.color) {
    case 'pink': return 'text-pink-500 dark:text-pink-400'
    case 'blue': return 'text-blue-500 dark:text-blue-400'
    case 'green': return 'text-green-500 dark:text-green-400'
    case 'orange': return 'text-orange-500 dark:text-orange-400'
    case 'purple': return 'text-purple-500 dark:text-purple-400'
    default: return 'text-purple-500 dark:text-purple-400'
  }
})

const trendIcon = computed(() => {
  switch (props.trend) {
    case 'up': return '↑'
    case 'down': return '↓'
    default: return ''
  }
})

const trendClass = computed(() => {
  if (!props.trend || props.trend === 'neutral') return ''
  return props.trend === 'up' ? 'text-green-500 dark:text-green-400' : 'text-red-500 dark:text-red-400'
})
</script>

<template>
  <el-card shadow="hover" class="stat-card">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-bold text-sm text-gray-700 dark:text-gray-200">{{ title }}</span>
        <span class="text-2xl">{{ icon }}</span>
      </div>
    </template>
    
    <div v-if="loading" class="h-16 flex items-center justify-center">
      <el-skeleton :rows="1" animated />
    </div>
    
    <div v-else class="text-center py-2">
      <div class="flex items-center justify-center gap-2">
        <span 
          class="text-3xl font-bold"
          :class="colorClass"
        >
          {{ value }}
        </span>
        <span v-if="trend" class="text-lg font-bold" :class="trendClass">
          {{ trendIcon }}
        </span>
      </div>
      <div v-if="subtitle" class="text-xs text-gray-500 dark:text-gray-400 mt-1">
        {{ subtitle }}
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.stat-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  background-color: var(--card);
  border-color: var(--border);
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(114, 46, 209, 0.15);
}
.dark .stat-card {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}
</style>
