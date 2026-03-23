<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getGatewayStatus } from '@/api/gateway'
import { getAllAgentDetails } from '@/api/agents'
import { getCronTasks } from '@/api/cron'
import { getTaskCounts, listTasks } from '@/api/tasks'
import { getDashboardOverview, getSystemInfo } from '@/api/dashboard'
import StatCard from '@/components/overview/StatCard.vue'
import ResourceChart from '@/components/overview/ResourceChart.vue'
import TaskDistributionChart from '@/components/overview/TaskDistributionChart.vue'
import RecentTasksList from '@/components/overview/RecentTasksList.vue'
import ActiveAgentsPanel from '@/components/overview/ActiveAgentsPanel.vue'
import type { Task } from '@/components/overview/RecentTasksList.vue'
import type { Agent } from '@/components/overview/ActiveAgentsPanel.vue'

const loading = ref(true)
const lastUpdated = ref<string>('')
const gateway = ref<any>({ status: 'unknown', version: 'unknown', workspaces: [] })
const agents = ref<Agent[]>([])
const cronTasks = ref<any[]>([])
const taskCounts = ref({ pending: 0, running: 0, completed: 0, failed: 0, total: 0 })
const recentTasks = ref<Task[]>([])
const systemInfo = ref<any>({})
const dashboardOverview = ref<any>({})

const successRate = computed(() => {
  const total = dashboardOverview.value.completedTasks + dashboardOverview.value.failedTasks
  if (total === 0) return 0
  return Math.round((dashboardOverview.value.completedTasks / total) * 100)
})

const activeCronCount = computed(() => 
  cronTasks.value.filter((t: any) => t.status === 'ok').length
)

async function refresh() {
  loading.value = true
  try {
    const [gw, openclawAgents, cron, tasks, recent, sysInfo, overview] = await Promise.all([
      getGatewayStatus(),
      getAllAgentDetails(),
      getCronTasks(),
      getTaskCounts(),
      listTasks(50),
      getSystemInfo(),
      getDashboardOverview()
    ])

    gateway.value = gw
    // getAllAgentDetails already returns the right format
    agents.value = openclawAgents || []
    cronTasks.value = cron
    taskCounts.value = tasks
    recentTasks.value = (recent || []).slice(0, 5).map((t: any) => ({
      id: t.id || t.taskId || '',
      type: t.type || '',
      status: t.status || 'PENDING',
      createdAt: t.createdAt || t.createTime
    }))
    systemInfo.value = sysInfo
    dashboardOverview.value = overview
    
    lastUpdated.value = new Date().toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit', 
      second: '2-digit' 
    })
  } catch (error) {
    console.error('刷新数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refresh()
})

const isGatewayRunning = computed(() => gateway.value.status === 'running')
</script>

<template>
  <div class="overview-page">
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-2xl font-bold">📊 系统概览</h2>
      <el-button type="primary" :loading="loading" @click="refresh">
        🔄 刷新
      </el-button>
    </div>

    <!-- Gateway状态横幅 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="24">
        <div 
          class="gateway-banner rounded-lg p-4 flex items-center justify-between"
          :class="isGatewayRunning ? 'bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800' : 'bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800'"
        >
          <div class="flex items-center gap-3">
            <span 
              class="w-3 h-3 rounded-full animate-pulse"
              :class="isGatewayRunning ? 'bg-green-500' : 'bg-red-500'"
            ></span>
            <span class="font-bold">
              🚀 Gateway {{ isGatewayRunning ? '运行中' : '已停止' }}
            </span>
            <span v-if="gateway.apiUrl" class="text-sm text-gray-500">
              {{ gateway.apiUrl }}
            </span>
          </div>
          <div class="text-sm text-gray-500">
            最后更新: {{ lastUpdated || '-' }}
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- KPI统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="12" :sm="6">
        <StatCard
          title="总任务"
          :value="dashboardOverview.totalTasks || 0"
          icon="📋"
          color="purple"
          :loading="loading"
          subtitle="全部任务"
        />
      </el-col>
      <el-col :xs="12" :sm="6">
        <StatCard
          title="已配置 Agent"
          :value="agents.length"
          icon="👩‍💼"
          color="blue"
          :loading="loading"
          subtitle="全部 Agent"
        />
      </el-col>
      <el-col :xs="12" :sm="6">
        <StatCard
          title="运行中"
          :value="dashboardOverview.processingTasks || 0"
          icon="⚡"
          color="orange"
          :loading="loading"
          subtitle="处理中任务"
        />
      </el-col>
      <el-col :xs="12" :sm="6">
        <StatCard
          title="成功率"
          :value="`${successRate}%`"
          icon="📈"
          color="green"
          :loading="loading"
          :subtitle="`${dashboardOverview.completedTasks || 0} 已完成`"
        />
      </el-col>
    </el-row>

    <!-- 图表 + 系统信息 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :sm="8">
        <ResourceChart 
          :loading="loading"
          :cpu-usage="systemInfo.cpuUsage"
          :memory-usage="systemInfo.memoryUsagePercent"
          :memory-used="systemInfo.memoryUsed"
          :memory-total="systemInfo.memoryTotal"
        />
      </el-col>
      <el-col :xs="24" :sm="8">
        <TaskDistributionChart 
          :stats="{
            pending: dashboardOverview.pendingTasks || 0,
            running: dashboardOverview.processingTasks || 0,
            completed: dashboardOverview.completedTasks || 0,
            failed: dashboardOverview.failedTasks || 0
          }"
          :loading="loading"
        />
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="h-full">
          <template #header>
            <span class="font-bold">⏱️ 系统信息</span>
          </template>
          
          <div v-if="loading" class="h-40 flex items-center justify-center">
            <el-icon class="is-loading text-2xl text-gray-400"><Loading /></el-icon>
          </div>
          
          <div v-else class="space-y-3">
            <div class="flex justify-between items-center">
              <span class="text-gray-500 text-sm">JVM 运行时长</span>
              <span class="font-medium">{{ systemInfo.jvmUptimeFormatted || '-' }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-500 text-sm">活跃定时任务</span>
              <el-tag type="success" size="small">{{ dashboardOverview.activeCronJobs || 0 }}</el-tag>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-500 text-sm">线程数</span>
              <span class="font-medium">{{ systemInfo.threadCount || 0 }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-500 text-sm">OpenClaw 版本</span>
              <el-tag type="primary" size="small">{{ gateway.version || 'unknown' }}</el-tag>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-500 text-sm">工作空间</span>
              <span class="font-medium">{{ gateway.workspaces?.length || 0 }} 个</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近任务 + 活跃Agent -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="12">
        <RecentTasksList 
          :tasks="recentTasks"
          :loading="loading"
          @view="(id) => $router.push(`/tasks?id=${id}`)"
        />
      </el-col>
      <el-col :xs="24" :lg="12">
        <ActiveAgentsPanel
          :agents="agents"
          :loading="loading"
          @view="(id) => $router.push(`/agents?id=${id}`)"
        />
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.overview-page {
  padding: 20px;
}

.gateway-banner {
  transition: background-color 0.3s ease;
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
