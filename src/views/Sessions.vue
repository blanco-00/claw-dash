<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getSessions, getSessionStats } from '@/api/sessions'
import type { SessionInfo, SessionStats } from '@/types/session'

const loading = ref(true)
const sessions = ref<SessionInfo[]>([])
const stats = ref<SessionStats>({ total: 0, active: 0, byAgent: {} })
const searchAgent = ref('')

async function refresh() {
  loading.value = true
  try {
    sessions.value = await getSessions()
    stats.value = await getSessionStats()
  } catch (error) {
    console.error('获取会话失败:', error)
  } finally {
    loading.value = false
  }
}

const filteredSessions = computed(() => {
  if (!searchAgent.value) return sessions.value
  return sessions.value.filter(s => s.agentId.includes(searchAgent.value))
})

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="sessions-page">
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold">💬 会话管理</h2>
      <el-button type="primary" :loading="loading" @click="refresh">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-6">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="text-center">
            <div class="text-3xl font-bold text-blue-500">{{ stats.total }}</div>
            <div class="text-gray-500">总会话数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="text-center">
            <div class="text-3xl font-bold text-green-500">{{ stats.active }}</div>
            <div class="text-gray-500">活跃会话</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="text-center">
            <div class="text-3xl font-bold text-pink-500">{{ Object.keys(stats.byAgent).length }}</div>
            <div class="text-gray-500">Agent数量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索过滤 -->
    <div class="mb-4">
      <el-input
        v-model="searchAgent"
        placeholder="搜索Agent..."
        clearable
        style="width: 300px"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 会话列表 -->
    <el-card shadow="hover">
      <el-table :data="filteredSessions" v-loading="loading" stripe>
        <el-table-column prop="key" label="会话Key" width="250">
          <template #default="{ row }">
            <span class="font-mono text-xs">{{ row.key.slice(0, 40) }}...</span>
          </template>
        </el-table-column>
        <el-table-column prop="agentId" label="Agent" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.agentId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="kind" label="类型" width="100" />
        <el-table-column prop="age" label="活跃时间" width="120" />
        <el-table-column prop="model" label="模型" width="150">
          <template #default="{ row }">
            <span class="text-sm">{{ row.model }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tokens" label="Token">
          <template #default="{ row }">
            <span class="text-sm font-mono">{{ row.tokens }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script lang="ts">
import { Refresh, Search } from '@element-plus/icons-vue'
export default {
  components: { Refresh, Search }
}
</script>

<style scoped>
.sessions-page {
  padding: 20px;
}
</style>
