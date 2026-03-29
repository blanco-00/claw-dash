<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { getCronTasks } from '@/api/cron'
import { listTasks } from '@/api/tasks'
import type { CronTask } from '@/types/cron'
import type { Task } from '@/types/task'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent, LegendComponent])

const { t } = useI18n()

const loading = ref(true)
const cronTasks = ref<CronTask[]>([])
const failedTasks = ref<Task[]>([])
const selectedError = ref<any>(null)

async function refresh() {
  loading.value = true
  try {
    const allCronTasks = await getCronTasks()
    cronTasks.value = allCronTasks.filter((t: any) => t.status === 'error')
    failedTasks.value = await listTasks(50, 'FAILED')
  } catch (error) {
    console.error(t('failures.message.fetchError'), error)
  } finally {
    loading.value = false
  }
}

// Error statistics
const errorStats = computed(() => {
  const cronErrors = cronTasks.value.length
  const taskErrors = failedTasks.value.length
  return { cronErrors, taskErrors, total: cronErrors + taskErrors }
})

// Error trend chart
const trendChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
  xAxis: { type: 'category', data: t('failures.chart.days') },
  yAxis: { type: 'value' },
  series: [
    { name: t('failures.chart.cronFailures'), type: 'bar', data: [1, 1, 0, 3, 1, 0, 1], itemStyle: { color: '#f59e0b' } },
    { name: t('failures.chart.taskFailures'), type: 'bar', data: [1, 2, 1, 0, 1, 2, 0], itemStyle: { color: '#ef4444' } }
  ]
}))

// Error categories
const errorCategories = computed(() => {
  const categories: Record<string, number> = {}
  failedTasks.value.forEach(task => {
    const type = task.type || 'unknown'
    categories[type] = (categories[type] || 0) + 1
  })
  return Object.entries(categories).map(([name, value]) => ({ name, value }))
})

// Error categories chart
const categoryChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: '60%',
      data: errorCategories.value,
      label: { formatter: '{b}: {c}' }
    }
  ]
}))

// View error details
function viewError(task: any) {
  selectedError.value = task
}

// Close detail
function closeDetail() {
  selectedError.value = null
}

// Retry task
function retryTask(task: Task) {
  console.log(t('failures.message.retryTask'), task.id)
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="page-container">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">⚠️</div>
        <div class="header-text">
          <h2 class="page-title">{{ t('failures.title') }}</h2>
          <p class="page-subtitle">
            <el-tag type="danger" v-if="errorStats.total > 0" size="small">
              {{ t('failures.totalFailures', { count: errorStats.total }) }}
            </el-tag>
          </p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" :loading="loading" @click="refresh">
          <el-icon><Refresh /></el-icon>
          {{ t('failures.refresh') }}
        </el-button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stat-card-grid">
      <div class="stat-card warning">
        <div class="stat-value">{{ errorStats.cronErrors }}</div>
        <div class="stat-label">{{ t('failures.stats.cronFailures') }}</div>
      </div>
      <div class="stat-card danger">
        <div class="stat-value">{{ errorStats.taskErrors }}</div>
        <div class="stat-label">{{ t('failures.stats.taskFailures') }}</div>
      </div>
      <div class="stat-card info">
        <div class="stat-value">{{ errorCategories.length }}</div>
        <div class="stat-label">{{ t('failures.stats.errorTypes') }}</div>
      </div>
    </div>

    <!-- Charts -->
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px;">
      <div class="table-panel">
        <div style="font-weight: 600; margin-bottom: 16px;">{{ t('failures.chart.failureTrend') }}</div>
        <v-chart v-if="!loading" style="height: 240px;" :option="trendChartOption" autoresize />
      </div>
      <div class="table-panel">
        <div style="font-weight: 600; margin-bottom: 16px;">{{ t('failures.chart.errorCategories') }}</div>
        <v-chart v-if="!loading" style="height: 240px;" :option="categoryChartOption" autoresize />
      </div>
    </div>

    <!-- Task Failure List -->
    <div class="table-panel" style="margin-bottom: 24px;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <span style="font-weight: 600;">{{ t('failures.taskTable.title') }}</span>
        <el-button type="danger" size="small">{{ t('failures.taskTable.batchRetry') }}</el-button>
      </div>
      <el-table :data="failedTasks" v-loading="loading" stripe>
        <el-table-column prop="id" :label="t('failures.taskTable.id')" width="80">
          <template #default="{ row }">
            <span class="font-mono text-xs">{{ row.id.slice(0, 8) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="t('failures.taskTable.type')" width="150" />
        <el-table-column prop="error" :label="t('failures.taskTable.error')" min-width="200">
          <template #default="{ row }">
            <span class="truncate stat-num danger">{{ row.error || t('failures.taskTable.unknownError') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" :label="t('failures.taskTable.retryCount')" width="100">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.retryCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="t('failures.taskTable.failureTime')" width="180">
          <template #default="{ row }">
            <span class="time-text">{{ row.completedAt ? new Date(row.completedAt).toLocaleString() : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('failures.taskTable.actions')" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewError(row)">{{ t('failures.button.details') }}</el-button>
            <el-button type="success" size="small" @click="retryTask(row)">{{ t('failures.button.retry') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Cron Failure List -->
    <div class="table-panel">
      <div style="font-weight: 600; margin-bottom: 16px;">{{ t('failures.cronTable.title') }}</div>
      <el-table :data="cronTasks" v-loading="loading" stripe>
        <el-table-column prop="id" :label="t('failures.cronTable.id')" width="200">
          <template #default="{ row }">
            <span class="font-mono text-xs">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="t('failures.cronTable.name')" />
        <el-table-column prop="agent" :label="t('failures.cronTable.agent')" width="120" />
        <el-table-column prop="schedule" :label="t('failures.cronTable.schedule')" width="150" />
        <el-table-column :label="t('failures.cronTable.actions')" width="100">
          <template #default>
            <el-button type="primary" size="small">{{ t('common.view') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Error Details Drawer -->
    <el-drawer
      v-model="selectedError"
      :title="t('failures.detail.title')"
      direction="rtl"
      size="500px"
      @close="closeDetail"
    >
      <div v-if="selectedError" style="display: flex; flex-direction: column; gap: 16px;">
        <div class="section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            错误详情
          </div>
          <div class="section-content">
            <div style="margin-bottom: 12px;">
              <div style="color: var(--text-secondary); font-size: 13px;">{{ t('failures.detail.taskId') }}</div>
              <div class="font-mono" style="word-break: break-all;">{{ selectedError.id }}</div>
            </div>
            <div style="margin-bottom: 12px;">
              <div style="color: var(--text-secondary); font-size: 13px;">{{ t('failures.detail.taskType') }}</div>
              <div>{{ selectedError.type }}</div>
            </div>
            <div style="margin-bottom: 12px;">
              <div style="color: var(--text-secondary); font-size: 13px;">{{ t('failures.detail.error') }}</div>
              <div class="stat-num danger" style="background: rgba(239, 68, 68, 0.1); padding: 12px; border-radius: 8px; word-break: break-all;">{{ selectedError.error }}</div>
            </div>
            <div style="margin-bottom: 12px;">
              <div style="color: var(--text-secondary); font-size: 13px;">{{ t('failures.detail.retryCount') }}</div>
              <div>{{ selectedError.retryCount }}</div>
            </div>
            <div style="margin-bottom: 12px;">
              <div style="color: var(--text-secondary); font-size: 13px;">{{ t('failures.detail.createdAt') }}</div>
              <div>{{ new Date(selectedError.createdAt).toLocaleString() }}</div>
            </div>
            <div style="margin-bottom: 12px;">
              <div style="color: var(--text-secondary); font-size: 13px;">{{ t('failures.detail.completedAt') }}</div>
              <div>{{ selectedError.completedAt ? new Date(selectedError.completedAt).toLocaleString() : '-' }}</div>
            </div>
            <div v-if="selectedError.result">
              <div style="color: var(--text-secondary); font-size: 13px;">{{ t('failures.detail.result') }}</div>
              <pre style="background: var(--bg-secondary); padding: 12px; border-radius: 8px; font-size: 12px; overflow: auto; max-height: 300px;">{{ selectedError.result }}</pre>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script lang="ts">
import { Refresh } from '@element-plus/icons-vue'
export default { components: { Refresh } }
</script>

<style scoped>
/* All styles now use global classes from style.css */
</style>
