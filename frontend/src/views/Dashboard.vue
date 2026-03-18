<template>
  <div class="dashboard">
    <h1>ClawDash 控制台</h1>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>任务统计</template>
          <div class="stat-item">
            <div class="stat-label">待执行</div>
            <div class="stat-value">{{ stats.PENDING || 0 }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">执行中</div>
            <div class="stat-value">{{ stats.RUNNING || 0 }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">已完成</div>
            <div class="stat-value">{{ stats.COMPLETED || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>Agent状态</template>
          <div class="stat-item">
            <div class="stat-label">在线</div>
            <div class="stat-value">{{ agentStats.active || 0 }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">总计</div>
            <div class="stat-value">{{ agentStats.total || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>本月收支</template>
          <div class="stat-item">
            <div class="stat-label">收入</div>
            <div class="stat-value income">¥{{ financeStats.income || 0 }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">支出</div>
            <div class="stat-value expense">¥{{ financeStats.expense || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>系统状态</template>
          <div class="status-indicator">
            <el-tag type="success">运行正常</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const stats = ref({})
const agentStats = ref({ active: 0, total: 0 })
const financeStats = ref({ income: 0, expense: 0 })

const loadStats = async () => {
  try {
    const taskRes = await request.get('/tasks/stats')
    stats.value = taskRes.data

    const agentRes = await request.get('/agents')
    const agents = agentRes.data
    agentStats.value = {
      active: agents.filter((a: any) => a.status === 'ACTIVE').length,
      total: agents.length
    }

    const now = new Date()
    const financeRes = await request.get('/finance/summary', {
      params: { year: now.getFullYear(), month: now.getMonth() + 1 }
    })
    financeStats.value = financeRes.data
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}
.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}
.stat-item:last-child {
  border-bottom: none;
}
.stat-label {
  color: #666;
}
.stat-value {
  font-size: 20px;
  font-weight: bold;
}
.stat-value.income {
  color: #67c23a;
}
.stat-value.expense {
  color: #f56c6c;
}
.status-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
}
</style>
