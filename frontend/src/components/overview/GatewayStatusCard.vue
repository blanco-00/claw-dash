<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { GatewayInfo } from '@/types/gateway'

const { t } = useI18n()

const props = defineProps<{
  data?: GatewayInfo
  loading?: boolean
}>()

const statusColorVar = computed(() => {
  if (!props.data) return 'var(--text-secondary)'
  switch (props.data.status) {
    case 'running': return 'var(--success-color)'
    case 'stopped': return 'var(--danger-color)'
    default: return 'var(--warning-color)'
  }
})

const statusText = computed(() => {
  if (!props.data) return t('overview.gateway.unknown')
  switch (props.data.status) {
    case 'running': return t('overview.gateway.running')
    case 'stopped': return t('overview.gateway.stopped')
    default: return t('overview.gateway.error')
  }
})
</script>

<template>
  <el-card shadow="hover" class="gateway-status-card">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-bold">{{ t('overview.gateway.title') }}</span>
        <div class="flex items-center gap-2">
          <span 
            class="w-3 h-3 rounded-full gateway-dot"
            :style="{ backgroundColor: statusColorVar }"
          />
          <span class="gateway-text">{{ statusText }}</span>
        </div>
      </div>
    </template>
    
    <div v-if="loading" class="h-20 flex items-center justify-center">
      <el-icon class="is-loading text-2xl"><Loading /></el-icon>
    </div>
    
    <div v-else-if="data" class="space-y-3">
      <div class="flex justify-between items-center">
        <span class="gateway-label">{{ t('overview.gateway.pid') }}</span>
        <span class="font-mono gateway-value">{{ data.pid || '-' }}</span>
      </div>
      <div class="flex justify-between items-center">
        <span class="gateway-label">{{ t('overview.gateway.port') }}</span>
        <span class="font-mono gateway-value">{{ data.port || '-' }}</span>
      </div>
      <div class="flex justify-between items-center">
        <span class="gateway-label">{{ t('overview.gateway.uptime') }}</span>
        <span class="gateway-value">{{ data.uptime || '-' }}</span>
      </div>
    </div>
    
    <div v-else class="h-20 flex items-center justify-center gateway-no-data">
      {{ t('overview.gateway.noData') }}
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
.gateway-status-card {
  transition: transform 0.2s;
}
.gateway-status-card:hover {
  transform: translateY(-2px);
}

.gateway-dot {
  flex-shrink: 0;
}

.gateway-text {
  color: var(--text-primary);
}

.gateway-label {
  color: var(--text-secondary);
}

.gateway-value {
  color: var(--text-primary);
}

.gateway-no-data {
  color: var(--text-secondary);
}
</style>
