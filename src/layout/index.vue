<script setup lang="ts">
import { ref } from 'vue'

const collapsed = ref(false)
const menuItems = [
  { path: '/overview', icon: '📊', label: '总览' },
  { path: '/agents-config', icon: '🏯', label: '女儿国' },
  { path: '/agents', icon: '👩‍💼', label: 'Agent' },
  { path: '/cron', icon: '⏰', label: '定时任务' },
  { path: '/tasks', icon: '📋', label: '任务队列' },
  { path: '/task-group', icon: '🔗', label: '任务组' },
  { path: '/tokens', icon: '💰', label: 'Tokens' },
  { path: '/failures', icon: '⚠️', label: '失败追踪' },
  { path: '/sessions', icon: '💬', label: '会话' }
]
</script>

<template>
  <el-container class="h-screen">
    <!-- 侧边栏 -->
    <el-aside :width="collapsed ? '64px' : '200px'" style="background: white; border-right: 1px solid #e5e7eb;">
      <div style="height: 64px; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #e5e7eb;">
        <h1 v-if="!collapsed" style="font-size: 18px; font-weight: bold; color: #ec4899; margin: 0;">🏰 女儿国</h1>
        <span v-else style="font-size: 24px;">🏰</span>
      </div>
      
      <el-menu :default-active="$route.path" :collapse="collapsed" router style="border: none;">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <span>{{ item.icon }} {{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 头部 -->
      <el-header style="background: white; border-bottom: 1px solid #e5e7eb; display: flex; align-items: center; justify-content: space-between; padding: 0 16px;">
        <div style="display: flex; align-items: center;">
          <el-button text @click="collapsed = !collapsed">
            {{ collapsed ? '☰' : '✕' }}
          </el-button>
          <span style="margin-left: 16px; color: #6b7280;">ClawDash 监控系统</span>
        </div>
        <el-tag type="success">Gateway: 运行中</el-tag>
      </el-header>
      
      <!-- 主体 -->
      <el-main style="background: #f9fafb; padding: 20px;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
