<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getAgentList, getAgentDetail, getAllAgentDetails } from '@/api/agents'
import type { AgentInfo, AgentListItem } from '@/types/agent'

const loading = ref(true)
const agents = ref<AgentInfo[]>([])
const selectedAgent = ref<AgentInfo | null>(null)

// 刷新
async function refresh() {
  loading.value = true
  try {
    agents.value = await getAllAgentDetails()
  } catch (error) {
    console.error('获取Agent列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 打开详情
function openDetail(agent: AgentInfo) {
  selectedAgent.value = agent
}

// 关闭详情
function closeDetail() {
  selectedAgent.value = null
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="agents-page">
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold">👩‍💼 Agent管理</h2>
      <el-button type="primary" :loading="loading" @click="refresh">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- Agent列表 -->
    <el-card shadow="hover">
      <el-table :data="agents" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="150">
          <template #default="{ row }">
            <span class="font-mono text-sm">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" width="120">
          <template #default="{ row }">
            <span class="font-bold">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="封号" width="120" />
        <el-table-column prop="role" label="职责" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'online' ? 'success' : 'info'">
              {{ row.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDetail(row)"> 详情 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="selectedAgent"
      :title="selectedAgent?.name + ' - 详情'"
      direction="rtl"
      size="400px"
    >
      <div v-if="selectedAgent" class="space-y-4">
        <div class="text-center py-4 border-b">
          <div class="text-4xl mb-2">👩‍💼</div>
          <div class="text-xl font-bold">{{ selectedAgent.name }}</div>
          <div class="text-gray-500">{{ selectedAgent.title }}</div>
        </div>

        <div class="space-y-3">
          <div class="flex justify-between">
            <span class="text-gray-500">ID</span>
            <span class="font-mono">{{ selectedAgent.id }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">职责</span>
            <span>{{ selectedAgent.role }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">状态</span>
            <el-tag :type="selectedAgent.status === 'online' ? 'success' : 'info'">
              {{ selectedAgent.status }}
            </el-tag>
          </div>
          <div v-if="selectedAgent.workspace" class="flex justify-between">
            <span class="text-gray-500">工作区</span>
            <span class="text-sm font-mono truncate ml-2" style="max-width: 200px">
              {{ selectedAgent.workspace }}
            </span>
          </div>
        </div>

        <div v-if="selectedAgent.memory" class="mt-4">
          <div class="font-bold mb-2">文件大小</div>
          <div class="space-y-2 text-sm">
            <div class="flex justify-between">
              <span class="text-gray-500">SOUL.md</span>
              <span>{{
                selectedAgent.memory.soul
                  ? (selectedAgent.memory.soul / 1024).toFixed(1) + ' KB'
                  : '-'
              }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">MEMORY.md</span>
              <span>{{
                selectedAgent.memory.memory
                  ? (selectedAgent.memory.memory / 1024).toFixed(1) + ' KB'
                  : '-'
              }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script lang="ts">
import { Refresh } from '@element-plus/icons-vue'
export default {
  components: { Refresh }
}
</script>

<style scoped>
.agents-page {
  padding: 20px;
}
</style>
