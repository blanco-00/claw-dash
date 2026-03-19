<script setup lang="ts">
import { ref } from 'vue'

const stats = ref({
  gateway: '运行中',
  agents: 13,
  cronTasks: 5,
  pendingTasks: 3
})
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold mb-6">📊 系统总览</h2>
    
    <el-row :gutter="20" class="mb-6">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="flex items-center justify-between">
              <span>Gateway状态</span>
              <el-tag type="success">🟢</el-tag>
            </div>
          </template>
          <div class="text-3xl font-bold text-green-500">{{ stats.gateway }}</div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span>Agent数量</span>
          </template>
          <div class="text-3xl font-bold text-pink-500">{{ stats.agents }}</div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span>Cron任务</span>
          </template>
          <div class="text-3xl font-bold text-blue-500">{{ stats.cronTasks }}</div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span>待处理任务</span>
          </template>
          <div class="text-3xl font-bold text-orange-500">{{ stats.pendingTasks }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
