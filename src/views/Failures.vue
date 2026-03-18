<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
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
    console.error('获取失败列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 错误统计
const errorStats = computed(() => {
  const cronErrors = cronTasks.value.length
  const taskErrors = failedTasks.value.length
  return { cronErrors, taskErrors, total: cronErrors + taskErrors }
})

// 错误趋势图（模拟）
const trendChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
  xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
  yAxis: { type: 'value' },
  series: [
    { name: 'Cron失败', type: 'bar', data: [2, 1, 0, 3, 1, 0, 1], itemStyle: { color: '#f59e0b' } },
    { name: 'Task失败', type: 'bar', data: [1, 2, 1, 0, 1, 2, 0], itemStyle: { color: '#ef4444' } }
  ]
}))

// 错误分类
const errorCategories = computed(() => {
  const categories: Record<string, number> = {}
  failedTasks.value.forEach(task => {
    const type = task.type || 'unknown'
    categories[type] = (categories[type] || 0) + 1
  })
  return Object.entries(categories).map(([name, value]) => ({ name, value }))
})

// 错误分类图
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

// 查看错误详情
function viewError(task: any) {
  selectedError.value = task
}

// 关闭详情
function closeDetail() {
  selectedError.value = null
}

// 重试任务
function retryTask(task: Task) {
  console.log('重试任务:', task.id)
  // 实际实现需要调用API
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="failures-page">
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-4">
        <h2 class="text-2xl font-bold">⚠️ 失败追踪</h2>
        <el-tag type="danger" v-if="errorStats.total > 0">
          共 {{ errorStats.total }} 个失败
        </el-tag>
      </div>
      <el-button type="primary" :loading="loading" @click="refresh">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-6">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="text-center py-2">
            <div class="text-gray-500 text-sm">Cron失败</div>
            <div class="text-3xl font-bold text-orange-500">{{ errorStats.cronErrors }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="text-center py-2">
            <div class="text-gray-500 text-sm">Task失败</div>
            <div class="text-3xl font-bold text-red-500">{{ errorStats.taskErrors }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="text-center py-2">
            <div class="text-gray-500 text-sm">错误类型</div>
            <div class="text-3xl font-bold text-blue-500">{{ errorCategories.length }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="20" class="mb-6">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="font-bold">失败趋势</span></template>
          <v-chart v-if="!loading" class="h-60" :option="trendChartOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="font-bold">错误分类</span></template>
          <v-chart v-if="!loading" class="h-60" :option="categoryChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- Task失败列表 -->
    <el-card shadow="hover" class="mb-6">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-bold">Task失败列表</span>
          <el-button type="danger" size="small" plain>批量重试</el-button>
        </div>
      </template>
      <el-table :data="failedTasks" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80">
          <template #default="{ row }">
            <span class="font-mono text-xs">{{ row.id.slice(0, 8) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="150" />
        <el-table-column prop="error" label="错误信息" min-width="200">
          <template #default="{ row }">
            <span class="truncate text-red-500">{{ row.error || '未知错误' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试次数" width="100">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.retryCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="失败时间" width="180">
          <template #default="{ row }">
            <span class="text-sm">{{
              row.completedAt ? new Date(row.completedAt).toLocaleString('zh-CN') : '-'
            }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewError(row)">详情</el-button>
            <el-button type="success" size="small" @click="retryTask(row)">重试</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Cron失败列表 -->
    <el-card shadow="hover">
      <template #header><span class="font-bold">Cron失败列表</span></template>
      <el-table :data="cronTasks" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="200">
          <template #default="{ row }">
            <span class="font-mono text-xs">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="agent" label="Agent" width="120" />
        <el-table-column prop="schedule" label="调度" width="150" />
        <el-table-column label="操作" width="100">
          <template #default>
            <el-button type="primary" size="small">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 错误详情抽屉 -->
    <el-drawer
      v-model="selectedError"
      title="错误详情"
      direction="rtl"
      size="500px"
      @close="closeDetail"
    >
      <div v-if="selectedError" class="space-y-4">
        <div>
          <div class="text-gray-500 text-sm">任务ID</div>
          <div class="font-mono">{{ selectedError.id }}</div>
        </div>
        <div>
          <div class="text-gray-500 text-sm">任务类型</div>
          <div>{{ selectedError.type }}</div>
        </div>
        <div>
          <div class="text-gray-500 text-sm">错误信息</div>
          <div class="text-red-500 bg-red-50 p-3 rounded">{{ selectedError.error }}</div>
        </div>
        <div>
          <div class="text-gray-500 text-sm">重试次数</div>
          <div>{{ selectedError.retryCount }}</div>
        </div>
        <div>
          <div class="text-gray-500 text-sm">创建时间</div>
          <div>{{ new Date(selectedError.createdAt).toLocaleString('zh-CN') }}</div>
        </div>
        <div>
          <div class="text-gray-500 text-sm">完成时间</div>
          <div>
            {{
              selectedError.completedAt
                ? new Date(selectedError.completedAt).toLocaleString('zh-CN')
                : '-'
            }}
          </div>
        </div>
        <div v-if="selectedError.result">
          <div class="text-gray-500 text-sm">执行结果</div>
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
