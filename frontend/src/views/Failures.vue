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
  <div class="failures-page">
    <!-- Page Header -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-4">
        <h2 class="text-2xl font-bold">⚠️ {{ t('failures.title') }}</h2>
        <el-tag type="danger" v-if="errorStats.total > 0">
          {{ t('failures.totalFailures', { count: errorStats.total }) }}
        </el-tag>
      </div>
      <el-button type="primary" :loading="loading" @click="refresh">
        <el-icon><Refresh /></el-icon>
        {{ t('failures.refresh') }}
      </el-button>
    </div>

    <!-- Stats Cards -->
    <el-row :gutter="20" class="mb-6">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="text-center py-2">
            <div class="text-gray-500 text-sm">{{ t('failures.stats.cronFailures') }}</div>
            <div class="text-3xl font-bold text-orange-500">{{ errorStats.cronErrors }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="text-center py-2">
            <div class="text-gray-500 text-sm">{{ t('failures.stats.taskFailures') }}</div>
            <div class="text-3xl font-bold text-red-500">{{ errorStats.taskErrors }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="text-center py-2">
            <div class="text-gray-500 text-sm">{{ t('failures.stats.errorTypes') }}</div>
            <div class="text-3xl font-bold text-blue-500">{{ errorCategories.length }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="20" class="mb-6">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="font-bold">{{ t('failures.chart.failureTrend') }}</span></template>
          <v-chart v-if="!loading" class="h-60" :option="trendChartOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="font-bold">{{ t('failures.chart.errorCategories') }}</span></template>
          <v-chart v-if="!loading" class="h-60" :option="categoryChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- Task Failure List -->
    <el-card shadow="hover" class="mb-6">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-bold">{{ t('failures.taskTable.title') }}</span>
          <el-button type="danger" size="small" plain>{{ t('failures.taskTable.batchRetry') }}</el-button>
        </div>
      </template>
      <el-table :data="failedTasks" v-loading="loading" stripe>
        <el-table-column prop="id" :label="t('failures.taskTable.id')" width="80">
          <template #default="{ row }">
            <span class="font-mono text-xs">{{ row.id.slice(0, 8) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="t('failures.taskTable.type')" width="150" />
        <el-table-column prop="error" :label="t('failures.taskTable.error')" min-width="200">
          <template #default="{ row }">
            <span class="truncate text-red-500">{{ row.error || t('failures.taskTable.unknownError') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" :label="t('failures.taskTable.retryCount')" width="100">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.retryCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="t('failures.taskTable.failureTime')" width="180">
          <template #default="{ row }">
            <span class="text-sm">{{
              row.completedAt ? new Date(row.completedAt).toLocaleString() : '-'
            }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('failures.taskTable.actions')" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewError(row)">{{ t('failures.button.details') }}</el-button>
            <el-button type="success" size="small" @click="retryTask(row)">{{ t('failures.button.retry') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Cron Failure List -->
    <el-card shadow="hover">
      <template #header><span class="font-bold">{{ t('failures.cronTable.title') }}</span></template>
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
    </el-card>

    <!-- Error Details Drawer -->
    <el-drawer
      v-model="selectedError"
      :title="t('failures.detail.title')"
      direction="rtl"
      size="500px"
      @close="closeDetail"
    >
      <div v-if="selectedError" class="space-y-4">
        <div>
          <div class="text-gray-500 text-sm">{{ t('failures.detail.taskId') }}</div>
          <div class="font-mono">{{ selectedError.id }}</div>
        </div>
        <div>
          <div class="text-gray-500 text-sm">{{ t('failures.detail.taskType') }}</div>
          <div>{{ selectedError.type }}</div>
        </div>
        <div>
          <div class="text-gray-500 text-sm">{{ t('failures.detail.error') }}</div>
          <div class="text-red-500 bg-red-50 p-3 rounded">{{ selectedError.error }}</div>
        </div>
        <div>
          <div class="text-gray-500 text-sm">{{ t('failures.detail.retryCount') }}</div>
          <div>{{ selectedError.retryCount }}</div>
        </div>
        <div>
          <div class="text-gray-500 text-sm">{{ t('failures.detail.createdAt') }}</div>
          <div>{{ new Date(selectedError.createdAt).toLocaleString() }}</div>
        </div>
        <div>
          <div class="text-gray-500 text-sm">{{ t('failures.detail.completedAt') }}</div>
          <div>
            {{
              selectedError.completedAt
                ? new Date(selectedError.completedAt).toLocaleString()
                : '-'
            }}
          </div>
        </div>
        <div v-if="selectedError.result">
          <div class="text-gray-500 text-sm">{{ t('failures.detail.result') }}</div>
          <pre class="bg-gray-100 p-3 rounded text-xs overflow-auto">{{
            selectedError.result
          }}</pre>
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
.failures-page {
  padding: 20px;
}
</style>
