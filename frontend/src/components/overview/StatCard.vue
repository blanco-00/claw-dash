<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

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
    case 'pink': return 'stat-value-pink'
    case 'blue': return 'stat-value-blue'
    case 'green': return 'stat-value-green'
    case 'orange': return 'stat-value-orange'
    case 'purple': return 'stat-value-purple'
    default: return 'stat-value-purple'
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
  return props.trend === 'up' ? 'stat-trend-up' : 'stat-trend-down'
})
</script>

<template>
  <el-card shadow="hover" class="stat-card">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-bold text-sm stat-title">{{ title }}</span>
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
      <div v-if="subtitle" class="text-xs stat-subtitle mt-1">
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

/* Stat value colors - use CSS variables for dark mode support */
.stat-value-pink { color: var(--primary); }
.stat-value-blue { color: var(--info-color); }
.stat-value-green { color: var(--success-color); }
.stat-value-orange { color: var(--warning-color); }
.stat-value-purple { color: var(--primary); }
.stat-trend-up { color: var(--success-color); }
.stat-trend-down { color: var(--danger-color); }

/* Title and subtitle - use CSS variables for proper dark mode */
.stat-title {
  color: var(--text-primary);
}

.stat-subtitle {
  color: var(--text-secondary);
}
</style>
