<script setup lang="ts">
import { ref, onMounted } from 'vue'

// 模拟数据
const gateway = ref({
  status: 'running',
  pid: 34745,
  version: 'v1.0.0',
  uptime: '3小时24分钟',
  port: 18789
})

const agents = ref([
  { id: 'main', name: '瑾儿', status: 'online' },
  { id: 'menxiasheng', name: '卿酒', status: 'online' },
  { id: 'shangshusheng', name: '红袖', status: 'online' },
  { id: 'jinyiwei', name: '灵鸢', status: 'online' },
  { id: 'libu4', name: '珊瑚', status: 'online' },
  { id: 'hubu', name: '琉璃', status: 'online' },
  { id: 'libu3', name: '书瑶', status: 'online' },
  { id: 'bingbu', name: '魅羽', status: 'online' },
  { id: 'xingbu', name: '如意', status: 'online' },
  { id: 'gongbu', name: '灵犀', status: 'online' },
  { id: 'jishu', name: '青岚', status: 'online' },
  { id: 'shangshiju', name: '婉儿', status: 'offline' },
  { id: 'shangyaosi', name: '允贤', status: 'offline' }
])

const cronTasks = ref([
  { id: 'health-check', name: '健康检查', schedule: '*/30 * * * *', status: 'ok', enabled: true },
  { id: 'task-poll', name: '任务轮询', schedule: '*/10 * * * *', status: 'ok', enabled: true },
  { id: 'memory-cleanup', name: '内存清理', schedule: '0 2 * * *', status: 'idle', enabled: false },
  { id: 'stats-report', name: '统计报告', schedule: '0 8 * * *', status: 'ok', enabled: true }
])

const sessions = ref([
  { agent: '瑾儿', messages: 156 },
  { agent: '青岚', messages: 89 },
  { agent: '灵犀', messages: 45 },
  { agent: '红袖', messages: 23 },
  { agent: '婉儿', messages: 12 }
])

const loading = ref(false)

function refresh() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
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
                {{ gateway.status === 'running' ? '运行中' : '已停止' }}
              </el-tag>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="text-center">
                <div class="text-gray-500 text-sm">进程ID</div>
                <div class="text-xl font-mono">{{ gateway.pid }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="text-center">
                <div class="text-gray-500 text-sm">版本</div>
                <div class="text-xl">{{ gateway.version }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="text-center">
                <div class="text-gray-500 text-sm">运行时长</div>
                <div class="text-xl">{{ gateway.uptime }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="text-center">
                <div class="text-gray-500 text-sm">端口</div>
                <div class="text-xl">{{ gateway.port }}</div>
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
              <span class="text-gray-500">总数</span>
              <span class="font-bold">{{ agents.length }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">在线</span>
              <span class="text-green-500 font-bold">{{ agents.filter(a => a.status === 'online').length }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">离线</span>
              <span class="text-gray-400">{{ agents.filter(a => a.status === 'offline').length }}</span>
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
              <span class="font-bold">{{ sessions.length }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">总消息</span>
              <span class="font-bold">{{ sessions.reduce((sum, s) => sum + s.messages, 0) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 资源使用 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span class="font-bold">💻 资源使用</span>
          </template>
          <div class="space-y-3">
            <div>
              <div class="flex justify-between text-sm mb-1">
                <span>CPU</span>
                <span class="text-pink-500">35%</span>
              </div>
              <el-progress :percentage="35" :color="'#ec4899'" />
            </div>
            <div>
              <div class="flex justify-between text-sm mb-1">
                <span>内存</span>
                <span class="text-purple-500">48%</span>
              </div>
              <el-progress :percentage="48" :color="'#8b5cf6'" />
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
          <el-table :data="agents" stripe size="small">
            <el-table-column prop="id" label="ID" width="150">
              <template #default="{ row }">
                <span class="font-mono text-xs">{{ row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="名称" />
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
