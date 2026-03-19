<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useTheme } from '../composables/useTheme'
import { getGatewayStatus } from '../api/gateway'

const collapsed = ref(false)
const { theme, setTheme, isDark } = useTheme()
const locale = ref('CN')
const gateway = ref<any>({ status: 'unknown' })

async function fetchGatewayStatus() {
  try {
    gateway.value = await getGatewayStatus()
  } catch (e) {
    gateway.value = { status: 'unknown' }
  }
}

const themeOptions = [
  { value: 'light', label: '浅色', icon: '☀️' },
  { value: 'dark', label: '深色', icon: '🌙' },
  { value: 'system', label: '跟随系统', icon: '💻' }
]

const localeOptions = [
  { value: 'CN', label: '中文' },
  { value: 'EN', label: 'English' }
]

const themeOpen = ref(false)

const toggleThemeMenu = () => {
  themeOpen.value = !themeOpen.value
}
const handleThemeClick = (value: string) => {
  setTheme(value as 'light' | 'dark' | 'system')
  themeOpen.value = false
}

const menuItems = [
  { path: '/overview', icon: '📊', label: '总览' },
  { path: '/agents', icon: '🏛️', label: '组织架构' },
  { path: '/agent-graph', icon: '🕸️', label: 'Agent图谱' },
  { path: '/cron', icon: '⏰', label: '定时任务' },
  { path: '/tasks', icon: '📋', label: '任务队列' },
  { path: '/task-group', icon: '🔗', label: '任务组' },
  { path: '/tokens', icon: '💰', label: 'Tokens' },
  { path: '/failures', icon: '⚠️', label: '失败追踪' },
  { path: '/sessions', icon: '💬', label: '会话' }
]

const currentThemeOption = () => themeOptions.find(t => t.value === theme.value) || themeOptions[2]
const toggleLocale = () => {
  locale.value = locale.value === 'CN' ? 'EN' : 'CN'
}

onMounted(() => {
  fetchGatewayStatus()
  // 定时刷新
  setInterval(fetchGatewayStatus, 30000)
})
</script>

<template>
  <el-container class="h-screen">
    <el-aside :width="collapsed ? '64px' : '200px'" class="sidebar">
      <div class="sidebar-header">
        <h1 v-if="!collapsed" class="sidebar-title">🤖 ClawDash</h1>
        <span v-else class="sidebar-icon">🤖</span>
      </div>

      <el-menu :default-active="$route.path" :collapse="collapsed" router class="sidebar-menu">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <span>{{ item.icon }} {{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="glass-toolbar header">
        <div class="header-left">
          <el-button text @click="collapsed = !collapsed" class="header-btn">
            {{ collapsed ? '☰' : '✕' }}
          </el-button>
          <span class="header-title">ClawDash 监控系统</span>
        </div>

        <div class="header-right">
          <el-tag :type="gateway.status === 'running' ? 'success' : 'danger'" class="status-tag">
            Gateway:
            {{
              gateway.status === 'running'
                ? '运行中'
                : gateway.status === 'unknown'
                  ? '未知'
                  : '已停止'
            }}
          </el-tag>

          <div class="locale-toggle" @click="toggleLocale">
            <span class="locale-label">{{ locale }}</span>
          </div>

          <div class="theme-toggle" @click="toggleThemeMenu">
            <span class="theme-icon">{{ currentThemeOption().icon }}</span>
            <span class="theme-label">{{ currentThemeOption().label }}</span>
          </div>

          <el-button text class="settings-btn">⚙️</el-button>

          <transition name="fade">
            <div v-if="themeOpen" class="theme-dropdown">
              <div
                v-for="option in themeOptions"
                :key="option.value"
                class="theme-option"
                :class="{ active: theme === option.value }"
                @click.stop="handleThemeClick(option.value)"
              >
                <span class="theme-option-icon">{{ option.icon }}</span>
                <span>{{ option.label }}</span>
              </div>
            </div>
          </transition>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.sidebar {
  background: var(--card);
  border-right: 1px solid var(--border);
  transition:
    background-color 0.3s ease,
    border-color 0.3s ease;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--border);
}

.sidebar-title {
  font-size: 18px;
  font-weight: bold;
  color: var(--primary);
  margin: 0;
}

.sidebar-icon {
  font-size: 24px;
}

.sidebar-menu {
  border: none;
  background: transparent;
}

.header {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  transition: background-color 0.3s ease;
}

.dark .header {
  background: rgba(31, 41, 55, 0.6);
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
}

.header-btn {
  font-size: 18px;
}

.header-title {
  margin-left: 16px;
  color: var(--text-secondary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.status-tag {
  background: oklch(0.9 0.1 150) !important;
  border-color: oklch(0.8 0.15 150) !important;
}

.theme-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.dark .theme-toggle {
  background: rgba(255, 255, 255, 0.1);
}

.theme-toggle:hover {
  background: rgba(0, 0, 0, 0.1);
}

.dark .theme-toggle:hover {
  background: rgba(255, 255, 255, 0.15);
}

.theme-icon {
  font-size: 16px;
}

.theme-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.locale-toggle {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: background-color 0.2s ease;
  font-size: 12px;
  font-weight: 600;
  color: var(--primary);
}

.dark .locale-toggle {
  background: rgba(255, 255, 255, 0.1);
}

.locale-toggle:hover {
  background: rgba(114, 46, 209, 0.1);
}

.settings-btn {
  font-size: 18px;
  transition: transform 0.5s ease;
}

.settings-btn:hover {
  transform: rotate(90deg);
}

.theme-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 8px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 100;
  min-width: 120px;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.theme-option:hover {
  background: var(--bg-secondary);
}

.theme-option.active {
  color: var(--primary);
  background: oklch(0.95 0.05 280);
}

.theme-option-icon {
  font-size: 14px;
}

.main-content {
  background: var(--bg-secondary);
  padding: 20px;
  transition: background-color 0.3s ease;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
