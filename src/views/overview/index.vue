<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getGatewayStatus } from '@/api/gateway'
import { getAgentList } from '@/api/agents'
import { getCronTasks, getCronStats } from '@/api/cron'
import { getSessionStats } from '@/api/sessions'
import { getTaskCounts } from '@/api/tasks'

// 状态
const loading = ref(true)
const gateway = ref<any>({ status: 'unknown', pid: '-', version: '-', uptime: '-', port: '-' })
const agents = ref<any[]>([])
const cronTasks = ref<any[]>([])
const sessions = ref<any>({ total: 0 })
const taskCounts = ref<any>({ pending: 0, running: 0, completed: 0, failed: 0, total: 0 })

// 刷新函数
async function refresh() {
  loading.value = true
  try {
    const [gw, agentList, cron, sessionData, tasks] = await Promise.all([
      getGatewayStatus(),
      getAgentList(),
      getCronTasks(),
      getSessionStats(),
      getTaskCounts()
    ])
    
    gateway.value = gw
    agents.value = agentList
    cronTasks.value = cron
    sessions.value = sessionData
    taskCounts.value = tasks
  } catch (error) {
    console.error('刷新数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="overview-page">
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold">📊 系统概览</h2>
      <el-button type="primary" :loading="loading" @click="refresh">
        刷新
      </el-button>
    </div>

    <!-- Gateway状态卡片 -->
    <el-row :gutter="20" class="mb-6">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-bold">🚀 Gateway 状态</span>
              <el-tag :type="gateway.status === 'running' ? 'success' : 'danger'">
                {{ gateway.status === 'running' ? '运行中' : gateway.status === 'unknown' ? '未知' : '已停止' }}
              </el-tag>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="text-center">
                <div class="text-gray-500 text-sm">进程ID</div>
                <div class="text-xl font-mono">{{ gateway.pid || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="text-center">
                <div class="text-gray-500 text-sm">版本</div>
                <div class="text-xl">{{ gateway.version || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="text-center">
                <div class="text-gray-500 text-sm">运行时长</div>
                <div class="text-xl">{{ gateway.uptime || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="text-center">
                <div class="text-gray-500 text-sm">端口</div>
                <div class="text-xl">{{ gateway.port || '-' }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-6">
      <!-- Agent统计 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span class="font-bold">👩‍💼 Agent统计</span>
          </template>
          <div class="space-y-2">
            <div class="flex justify-between">
              <span class="text-gray-500">已配置</span>
              <span class="font-bold">{{ agents.length }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">活跃</span>
              <span class="text-green-500 font-bold">{{ agents.filter(a => a.status === 'online').length }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Cron任务统计 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span class="font-bold">⏰ Cron任务</span>
          </template>
          <div class="space-y-2">
            <div class="flex justify-between">
              <span class="text-gray-500">总任务</span>
              <span class="font-bold">{{ cronTasks.length }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">运行中</span>
              <span class="text-green-500 font-bold">{{ cronTasks.filter(t => t.status === 'ok').length }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">已禁用</span>
              <span class="text-gray-400">{{ cronTasks.filter(t => t.status === 'idle').length }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 会话统计 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span class="font-bold">💬 会话统计</span>
          </template>
          <div class="space-y-2">
            <div class="flex justify-between">
              <span class="text-gray-500">总会话</span>
              <span class="font-bold">{{ sessions.total }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 任务统计 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span class="font-bold">📋 任务队列</span>
          </template>
          <div class="space-y-2">
            <div class="flex justify-between">
              <span class="text-gray-500">待处理</span>
              <span class="text-orange-500 font-bold">{{ taskCounts.pending }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">运行中</span>
              <span class="text-blue-500 font-bold">{{ taskCounts.running }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">已完成</span>
              <span class="text-green-500 font-bold">{{ taskCounts.completed }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Agent列表预览 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span class="font-bold">👩‍💼 Agent列表</span>
          </template>
          <el-table :data="agents" stripe size="small" v-loading="loading">
            <el-table-column prop="id" label="ID" width="150">
              <template #default="{ row }">
                <span class="font-mono text-xs">{{ row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="title" label="封号" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'online' ? 'success' : 'info'" size="small">
                  {{ row.status === 'online' ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.overview-page {
  padding: 20px;
}
</style>
