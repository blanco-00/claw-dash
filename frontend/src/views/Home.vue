<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const stats = ref({
  gateway: 'Running',
  agents: 13,
  cronTasks: 5,
  pendingTasks: 3
})
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold mb-6">📊 {{ t('home.title') }}</h2>
    
    <el-row :gutter="20" class="mb-6">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="flex items-center justify-between">
              <span>{{ t('home.gatewayStatus') }}</span>
              <el-tag type="success">🟢</el-tag>
            </div>
          </template>
          <div class="text-3xl font-bold text-green-500">{{ t('overview.gateway.running') }}</div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span>{{ t('home.agentCount') }}</span>
          </template>
          <div class="text-3xl font-bold text-pink-500">{{ stats.agents }}</div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span>{{ t('home.cronTasks') }}</span>
          </template>
          <div class="text-3xl font-bold text-blue-500">{{ stats.cronTasks }}</div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span>{{ t('home.pendingTasks') }}</span>
          </template>
          <div class="text-3xl font-bold text-orange-500">{{ stats.pendingTasks }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
