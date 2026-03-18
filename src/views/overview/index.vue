<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getGatewayStatus } from '@/api/gateway'
import { getAgentList } from '@/api/agents'
import { getCronTasks } from '@/api/cron'
import { getSessionStats } from '@/api/sessions'
import { getTaskCounts } from '@/api/tasks'
import type { GatewayInfo } from '@/types/gateway'

import GatewayStatusCard from '@/components/overview/GatewayStatusCard.vue'
import ResourceChart from '@/components/overview/ResourceChart.vue'
import UptimeCard from '@/components/overview/UptimeCard.vue'
import VersionCard from '@/components/overview/VersionCard.vue'
import StatCard from '@/components/overview/StatCard.vue'

// 状态
const loading = ref(true)
const gateway = ref<GatewayInfo>()
const agentCount = ref(0)
const cronCount = ref(0)
const sessionCount = ref(0)
const taskCounts = ref({ pending: 0, running: 0, completed: 0, failed: 0, dead: 0, total: 0 })

// 刷新函数
async function refresh() {
  loading.value = true
  try {
    const [gw, agents, cron, sessions, tasks] = await Promise.all([
      getGatewayStatus(),
      getAgentList(),
      getCronTasks(),
      getSessionStats(),
      getTaskCounts()
    ])
    
    gateway.value = gw
    agentCount.value = agents.length
    cronCount.value = cron.length
    sessionCount.value = sessions.total
    taskCounts.value = tasks
  } catch (error) {
    console.error('刷新数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 自动刷新
let refreshInterval: ReturnType<typeof setInterval>

onMounted(() => {
  refresh()
  refreshInterval = setInterval(refresh, 30000) // 30秒自动刷新
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<template>
  <div class="overview-page">
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold">📊 系统概览</h2>
      <div class="flex items-center gap-2">
        <el-button type="primary" :loading="loading" @click="refresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-6">
      <el-col :span="6">
        <StatCard 
          title="Agent数量" 
          :value="agentCount" 
          icon="👩‍💼" 
          color="pink"
          :loading="loading"
        />
      </el-col>
      <el-col :span="6">
        <StatCard 
          title="Cron任务" 
          :value="cronCount" 
          icon="⏰" 
          color="blue"
          :loading="loading"
        />
      </el-col>
      <el-col :span="6">
        <StatCard 
          title="活跃会话" 
          :value="sessionCount" 
          icon="💬" 
          color="green"
          :loading="loading"
        />
      </el-col>
      <el-col :span="6">
        <StatCard 
          title="待处理任务" 
          :value="taskCounts.pending" 
          icon="📋" 
          color="orange"
          :loading="loading"
        />
      </el-col>
    </el-row>

    <!-- 主要内容区 -->
    <el-row :gutter="20">
      <!-- 左侧：状态卡片 -->
      <el-col :span="8">
        <div class="space-y-4">
          <GatewayStatusCard :data="gateway" :loading="loading" />
          <UptimeCard :startTime="gateway?.uptime" :loading="loading" />
          <VersionCard :version="gateway?.version" :loading="loading" />
        </div>
      </el-col>
      
      <!-- 右侧：图表 -->
      <el-col :span="16">
        <ResourceChart :loading="loading" />
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" class="mt-6">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span class="font-bold">快捷操作</span>
          </template>
          <div class="flex gap-4">
            <el-button type="primary" plain>重启Gateway</el-button>
            <el-button type="success" plain>查看日志</el-button>
            <el-button type="warning" plain>安全审计</el-button>
            <el-button type="info" plain>系统设置</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts">
import { Refresh } from '@element-plus/icons-vue'
export default {
  components: { Refresh }
}
</script>

<style scoped>
.overview-page {
  padding: 20px;
}
</style>
