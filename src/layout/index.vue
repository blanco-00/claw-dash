<script setup lang="ts">
import { ref } from 'vue'
import { PureContainer } from '@pureadmin/components'

const collapsed = ref(false)
</script>

<template>
  <PureContainer>
    <el-container class="h-screen">
      <el-aside :width="collapsed ? '64px' : '220px'" class="bg-white border-r">
        <div class="h-16 flex items-center justify-center border-b">
          <h1 v-if="!collapsed" class="text-lg font-bold text-pink-500">🏰 女儿国</h1>
          <span v-else class="text-pink-500 text-2xl">🏰</span>
        </div>
        
        <el-menu :default-active="$route.path" :collapse="collapsed" router class="border-none">
          <el-menu-item index="/overview"><span>📊 总览</span></el-menu-item>
          <el-menu-item index="/agents-config"><span>🏯 女儿国</span></el-menu-item>
          <el-menu-item index="/agents"><span>👩‍💼 Agent</span></el-menu-item>
          <el-menu-item index="/cron"><span>⏰ 定时任务</span></el-menu-item>
          <el-menu-item index="/tasks"><span>📋 任务队列</span></el-menu-item>
          <el-menu-item index="/task-group"><span>🔗 任务组</span></el-menu-item>
          <el-menu-item index="/tokens"><span>💰 Tokens</span></el-menu-item>
          <el-menu-item index="/failures"><span>⚠️ 失败追踪</span></el-menu-item>
          <el-menu-item index="/sessions"><span>💬 会话</span></el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="bg-white border-b flex items-center justify-between px-4">
          <div class="flex items-center">
            <el-button text @click="collapsed = !collapsed">
              <span class="text-lg">{{ collapsed ? '☰' : '✕' }}</span>
            </el-button>
            <span class="ml-4 text-gray-600 font-medium">ClawDash 监控系统</span>
          </div>
          <div class="flex items-center gap-4">
            <el-tag type="success">Gateway: 运行中</el-tag>
          </div>
        </el-header>
        <el-main class="bg-gray-50">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </PureContainer>
</template>

<style scoped>
.el-aside { transition: width 0.3s; }
</style>
