<template>
  <el-button :loading="loading" @click="handleSync">
    <el-icon v-if="!loading"><Refresh /></el-icon>
    {{ t('agentGraph.sync') }}
  </el-button>
</template>

<script setup lang="ts">
import { Refresh } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

interface Props {
  loading?: boolean
}

interface Emits {
  (e: 'sync'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const { t } = useI18n()

async function handleSync() {
  emit('sync')
}
</script>
