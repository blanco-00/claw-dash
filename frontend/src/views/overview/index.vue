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

// Use actual color values for inline styles (CSS variables don't resolve in :style bindings)
const statusColor = computed(() => isGatewayRunning.value ? '#10b981' : '#ef4444')
</script>

<template>
  <div class="overview-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">📊 系统概览</h1>
        <p class="page-subtitle">实时监控 Gateway 运行状态和任务数据</p>
      </div>
      <div class="page-header-right">
        <button class="refresh-button" :class="{ 'is-loading': loading }" @click="refresh" :disabled="loading">
          <svg class="refresh-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M23 4V10H17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M1 20V14H7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M3.51 9.00001C4.01717 7.56679 4.87913 6.28541 6.01547 5.27543C7.1518 4.26545 8.52547 3.55978 10.0083 3.22427C11.4911 2.88875 13.0348 2.93436 14.4952 3.35678C15.9556 3.7792 17.2853 4.56471 18.36 5.64001L23 10M1 14L5.64 18.36C6.71475 19.4353 8.04437 20.2208 9.50481 20.6432C10.9652 21.0656 12.5089 21.1112 13.9917 20.7757C15.4745 20.4402 16.8482 19.7345 17.9845 18.7246C19.1209 17.7146 19.9828 16.4332 20.49 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>{{ loading ? '刷新中...' : '刷新数据' }}</span>
        </button>
      </div>
    </div>

    <!-- Gateway状态横幅 -->
    <div class="gateway-banner">
      <div class="gateway-banner-left">
        <div class="gateway-icon" :class="{ 'is-running': isGatewayRunning }">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="gateway-info">
          <div class="gateway-title">
            <span class="gateway-status-badge" :class="isGatewayRunning ? 'badge-success' : 'badge-danger'"></span>
            Gateway {{ isGatewayRunning ? '运行中' : '已停止' }}
          </div>
          <div v-if="gateway.apiUrl" class="gateway-url">{{ gateway.apiUrl }}</div>
        </div>
      </div>
      <div class="gateway-banner-right">
        <div class="gateway-time">
          <span class="gateway-time-label">最后更新</span>
          <span class="gateway-time-value">{{ lastUpdated || '-' }}</span>
        </div>
        <a v-if="gateway.apiUrl" :href="gateway.apiUrl" target="_blank" class="gateway-dashboard-link">
          打开 Dashboard
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18 13V19C18 20.1046 17.1046 21 16 21H5C3.89543 21 3 20.1046 3 19V8C3 6.89543 3.89543 6 5 6H11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M15 3H21V9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M10 14L21 3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </a>
      </div>
    </div>

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
        <el-card shadow="hover" class="h-full system-info-card">
          <template #header>
            <span class="system-info-header">⏱️ 系统信息</span>
          </template>
          
          <div v-if="loading" class="system-info-loading">
            <el-icon class="is-loading text-2xl"><Loading /></el-icon>
          </div>
          
          <div v-else class="system-info-list">
            <div class="info-row">
              <span class="info-label">JVM 运行时长</span>
              <span class="info-value">{{ systemInfo.jvmUptimeFormatted || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">活跃定时任务</span>
              <el-tag type="success" size="small">{{ dashboardOverview.activeCronJobs || 0 }}</el-tag>
            </div>
            <div class="info-row">
              <span class="info-label">线程数</span>
              <span class="info-value">{{ systemInfo.threadCount || 0 }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">OpenClaw 版本</span>
              <el-tag type="primary" size="small">{{ gateway.version || 'unknown' }}</el-tag>
            </div>
            <div class="info-row">
              <span class="info-label">工作空间</span>
              <span class="info-value">{{ gateway.workspaces?.length || 0 }} 个</span>
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
  padding: 24px 28px;
  min-height: calc(100vh - 84px);
  background: var(--bg-secondary);
}

/* ==================== */
/* Page Header */
/* ==================== */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border);
}

.page-header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.page-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.refresh-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 10px;
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.refresh-button:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary);
  box-shadow: 0 2px 8px rgba(114, 46, 209, 0.12);
}

.refresh-button:active:not(:disabled) {
  transform: scale(0.98);
}

.refresh-button.is-loading {
  opacity: 0.7;
  cursor: not-allowed;
}

.refresh-button.is-loading .refresh-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ==================== */
/* Gateway Banner */
/* ==================== */
.gateway-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  margin-bottom: 24px;
  transition: all 0.25s ease;
}

.gateway-banner:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.dark .gateway-banner:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);
}

.gateway-banner-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.gateway-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: var(--bg-secondary);
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.gateway-icon.is-running {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.18) 0%, rgba(16, 185, 129, 0.06) 100%);
  color: var(--success-color);
}

.gateway-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.gateway-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.gateway-status-badge {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--danger-color);
}

.gateway-status-badge.badge-success {
  background: var(--success-color);
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.5);
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% { box-shadow: 0 0 4px rgba(16, 185, 129, 0.4); }
  50% { box-shadow: 0 0 12px rgba(16, 185, 129, 0.7); }
}

.gateway-url {
  font-size: 12px;
  color: var(--text-secondary);
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
}

.gateway-banner-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.gateway-time {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 3px;
}

.gateway-time-label {
  font-size: 11px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.gateway-time-value {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
}

.gateway-dashboard-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: var(--primary);
  color: white;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
}

.gateway-dashboard-link:hover {
  background: var(--primary-light);
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(114, 46, 209, 0.35);
}

.dark .gateway-dashboard-link:hover {
  box-shadow: 0 4px 16px rgba(139, 71, 234, 0.45);
}

/* ==================== */
/* Card Styling */
/* ==================== */
:deep(.el-card) {
  --el-card-bg-color: var(--card);
  border-color: var(--border);
  background-color: var(--card);
  border-radius: 14px;
  transition: all 0.25s ease;
}

:deep(.el-card__header) {
  border-bottom-color: var(--border);
  background-color: var(--card);
  padding: 16px 20px;
}

:deep(.el-card__body) {
  background-color: var(--card);
  color: var(--text-primary);
  padding: 18px 20px;
}

:deep(.el-row) {
  margin-bottom: 0;
}

:deep(.el-col) {
  padding: 12px;
}

/* Stat card hover effects */
:deep(.stat-card:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.dark :deep(.stat-card:hover) {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
}

/* Info label/value - system info card */
.info-label {
  color: var(--text-secondary);
  font-size: 13px;
}

.info-value {
  color: var(--text-primary);
  font-weight: 600;
  font-size: 13px;
}

/* System info card - info row */
:deep(.info-row) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}

:deep(.info-row:last-child) {
  border-bottom: none;
}

/* Page heading - legacy */
.page-heading {
  color: var(--text-primary);
}

/* System info header */
:deep(.system-info-header) {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Card section titles */
:deep(.card-section-title) {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

/* System info card */
:deep(.system-info-card) {
  height: 100%;
}

:deep(.system-info-loading) {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

:deep(.system-info-list) {
  display: flex;
  flex-direction: column;
}

:deep(.system-info-list .info-row) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}

:deep(.system-info-list .info-row:last-child) {
  border-bottom: none;
}
</style>
