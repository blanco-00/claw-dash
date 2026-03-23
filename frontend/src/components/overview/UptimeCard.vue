<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps<{
  startTime?: string
  loading?: boolean
}>()

const uptime = computed(() => {
  if (!props.startTime) return '-'
  // 简单处理，实际应该解析更详细的时间
  return props.startTime
})
</script>

<template>
  <el-card shadow="hover">
    <template #header>
      <span class="font-bold">{{ t('overview.systemInfo.jvmUptime') }}</span>
    </template>
    
    <div v-if="loading" class="h-16 flex items-center justify-center">
      <el-skeleton :rows="1" animated />
    </div>
    
    <div v-else class="text-center py-2">
      <div class="text-3xl font-bold text-pink-500">{{ uptime }}</div>
      <div class="text-sm text-gray-500 mt-1">{{ t('overview.uptime.startTime') }}</div>
    </div>
  </el-card>
</template>
