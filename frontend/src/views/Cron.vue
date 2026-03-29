<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getCronTasks, getCronStats } from '@/api/cron'
import type { CronTask } from '@/types/cron'

const { t } = useI18n()
const loading = ref(true)
const tasks = ref<CronTask[]>([])
const stats = ref({ total: 0, enabled: 0, disabled: 0, errors: 0 })

async function refresh() {
  loading.value = true
  try {
    tasks.value = await getCronTasks()
    stats.value = await getCronStats()
  } catch (error) {
    console.error(t('cron.message.fetchError'), error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">⏰</div>
        <div class="header-text">
          <h2 class="page-title">{{ t('cron.title') }}</h2>
          <p class="page-subtitle">共 <span class="count">{{ stats.total }}</span> 个定时任务</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" :loading="loading" @click="refresh">
          <el-icon><Refresh /></el-icon>
          {{ t('cron.refresh') }}
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-card-grid">
      <div class="stat-card info">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">{{ t('cron.stats.total') }}</div>
      </div>
      <div class="stat-card success">
        <div class="stat-value">{{ stats.enabled }}</div>
        <div class="stat-label">{{ t('cron.stats.running') }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: var(--text-secondary);">{{ stats.disabled }}</div>
        <div class="stat-label">{{ t('cron.stats.disabled') }}</div>
      </div>
      <div class="stat-card danger">
        <div class="stat-value">{{ stats.errors }}</div>
        <div class="stat-label">{{ t('cron.stats.errors') }}</div>
      </div>
    </div>

    <!-- 任务列表 -->
    <div class="table-panel">
      <el-table :data="tasks" v-loading="loading" stripe>
        <el-table-column prop="id" :label="t('cron.table.id')" width="200">
          <template #default="{ row }">
            <span class="font-mono text-xs">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="t('cron.table.name')" />
        <el-table-column prop="agent" :label="t('cron.table.agent')" width="120" />
        <el-table-column prop="schedule" :label="t('cron.table.schedule')" width="150">
          <template #default="{ row }">
            <el-tag size="small">{{ row.schedule }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="t('cron.table.status')" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'ok' ? 'success' : row.status === 'error' ? 'danger' : 'info'"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nextRun" :label="t('cron.table.nextRun')" width="180">
          <template #default="{ row }">
            <span v-if="row.nextRun" class="time-text">
              {{ new Date(row.nextRun).toLocaleString('zh-CN') }}
            </span>
            <span v-else class="stat-num muted">-</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('cron.table.actions')" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small">{{ t('cron.button.details') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script lang="ts">
import { Refresh } from '@element-plus/icons-vue'
export default {
  components: { Refresh }
}
</script>

<style scoped>
/* All styles now use global classes from style.css */
</style>
