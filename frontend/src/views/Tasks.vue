<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listTasks, getTaskCounts } from '@/api/tasks'
import type { Task, TaskCounts } from '@/types/task'

const loading = ref(true)
const tasks = ref<Task[]>([])
const counts = ref<TaskCounts>({
  pending: 0,
  running: 0,
  completed: 0,
  failed: 0,
  dead: 0,
  total: 0
})
const currentStatus = ref<string>('')

async function refresh() {
  loading.value = true
  try {
    tasks.value = await listTasks(50, currentStatus.value || undefined)
    counts.value = await getTaskCounts()
  } catch (error) {
    console.error('获取任务失败:', error)
  } finally {
    loading.value = false
  }
}

function filterByStatus(status: string) {
  currentStatus.value = status
  refresh()
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="tasks-page">
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold">📋 任务队列</h2>
      <el-button type="primary" :loading="loading" @click="refresh">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-6">
      <el-col :span="4">
        <el-card
          shadow="hover"
          class="cursor-pointer"
          :class="{ 'border-pink-500': currentStatus === '' }"
          @click="filterByStatus('')"
        >
          <div class="text-center">
            <div class="text-2xl font-bold">{{ counts.total }}</div>
            <div class="text-gray-500 text-sm">全部</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card
          shadow="hover"
          class="cursor-pointer"
          :class="{ 'border-orange-500': currentStatus === 'PENDING' }"
          @click="filterByStatus('PENDING')"
        >
          <div class="text-center">
            <div class="text-2xl font-bold text-orange-500">{{ counts.pending }}</div>
            <div class="text-gray-500 text-sm">待处理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card
          shadow="hover"
          class="cursor-pointer"
          :class="{ 'border-blue-500': currentStatus === 'RUNNING' }"
          @click="filterByStatus('RUNNING')"
        >
          <div class="text-center">
            <div class="text-2xl font-bold text-blue-500">{{ counts.running }}</div>
            <div class="text-gray-500 text-sm">运行中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card
          shadow="hover"
          class="cursor-pointer"
          :class="{ 'border-green-500': currentStatus === 'COMPLETED' }"
          @click="filterByStatus('COMPLETED')"
        >
          <div class="text-center">
            <div class="text-2xl font-bold text-green-500">{{ counts.completed }}</div>
            <div class="text-gray-500 text-sm">已完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card
          shadow="hover"
          class="cursor-pointer"
          :class="{ 'border-red-500': currentStatus === 'FAILED' }"
          @click="filterByStatus('FAILED')"
        >
          <div class="text-center">
            <div class="text-2xl font-bold text-red-500">{{ counts.failed }}</div>
            <div class="text-gray-500 text-sm">失败</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card
          shadow="hover"
          class="cursor-pointer"
          :class="{ 'border-gray-500': currentStatus === 'DEAD' }"
          @click="filterByStatus('DEAD')"
        >
          <div class="text-center">
            <div class="text-2xl font-bold text-gray-500">{{ counts.dead }}</div>
            <div class="text-gray-500 text-sm">终止</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务列表 -->
    <el-card shadow="hover">
      <el-table :data="tasks" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80">
          <template #default="{ row }">
            <span class="font-mono text-xs">{{ row.id.slice(0, 8) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="150" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag
              :type="
                row.priority === 'high' ? 'danger' : row.priority === 'medium' ? 'warning' : 'info'
              "
              size="small"
            >
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag
              :type="
                row.status === 'COMPLETED'
                  ? 'success'
                  : row.status === 'FAILED'
                    ? 'danger'
                    : row.status === 'RUNNING'
                      ? 'primary'
                      : 'info'
              "
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            <span class="text-sm">{{ new Date(row.createdAt).toLocaleString('zh-CN') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试" width="80">
          <template #default="{ row }">
            <span v-if="row.retryCount > 0" class="text-orange-500">{{ row.retryCount }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script lang="ts">
import { Refresh } from '@element-plus/icons-vue'
export default {
  components: { Refresh }
}
</script>

<style scoped>
.tasks-page {
  padding: 20px;
}
.cursor-pointer {
  cursor: pointer;
}
</style>
