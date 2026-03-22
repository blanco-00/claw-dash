<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useTheme } from '../composables/useTheme'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const collapsed = ref(false)
const { theme, setTheme, isDark } = useTheme()
const { locale } = useI18n()

const localeLabel = computed(() => locale.value === 'zh' ? 'CN' : 'EN')

const pageNameMap: Record<string, string> = {
  '/overview': '系统概览',
  '/agents': 'Agent 组织架构',
  '/agent-graph': 'Agent 图谱',
  '/agents-config': 'Agent 配置',
  '/agents-config-graph': 'Config Graph',
  '/edge-types': '边类型管理',
  '/cron': '定时任务',
  '/tasks': '任务队列',
  '/task-types': '任务类型',
  '/task-group': '任务组',
  '/failures': '失败追踪',
  '/sessions': '会话',
  '/tokens': 'Tokens',
  '/openclaw': 'OpenClaw',
  '/docker': 'Docker'
}

const currentPageName = computed(() => {
  const path = route.path
  return pageNameMap[path] || pageNameMap[path.split('/').slice(0, -1).join('/')] || '监控系统'
})

const fetchGatewayStatus = () => {}

const themeOptions = [
  { value: 'light', label: '浅色', icon: 'Sunny' },
  { value: 'dark', label: '深色', icon: 'Moon' },
  { value: 'system', label: '跟随系统', icon: 'Monitor' }
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

interface MenuItem {
  path: string
  icon: string
  label: string
}

interface MenuGroup {
  title: string
  icon: string
  items: MenuItem[]
}

const menuGroups: MenuGroup[] = [
  {
    title: '仪表盘',
    icon: 'HomeFilled',
    items: [
      { path: '/overview', icon: 'Odometer', label: '总览' }
    ]
  },
  {
    title: 'Agent',
    icon: 'UserFilled',
    items: [
      { path: '/agents-list', icon: 'List', label: 'Agent列表' },
      { path: '/agents-config-graph', icon: 'Connection', label: 'Config Graph' },
      { path: '/edge-types', icon: 'Guide', label: '边类型管理' }
    ]
  },
  {
    title: '任务',
    icon: 'List',
    items: [
      { path: '/tasks', icon: 'Tickets', label: '任务队列' },
      { path: '/task-types', icon: 'Grid', label: '任务类型' }
    ]
  },
  {
    title: '监控',
    icon: 'Bell',
    items: [
      { path: '/failures', icon: 'WarningFilled', label: '失败追踪' }
    ]
  },
  {
    title: '系统',
    icon: 'Tools',
    items: [
      { path: '/tokens', icon: 'Coin', label: 'Tokens' },
      { path: '/openclaw', icon: 'Box', label: 'OpenClaw' },
      { path: '/docker', icon: 'Cpu', label: 'Docker' }
    ]
  }
]

const currentThemeOption = () => themeOptions.find(t => t.value === theme.value) || themeOptions[2]
const toggleLocale = () => {
  const newLocale = locale.value === 'zh' ? 'en' : 'zh'
  locale.value = newLocale
  localStorage.setItem('locale', newLocale)
}

onMounted(() => {
  fetchGatewayStatus()
  setInterval(fetchGatewayStatus, 30000)
})
</script>

<template>
  <el-container class="h-screen">
    <el-aside :width="collapsed ? '64px' : '220px'" class="sidebar">
      <div class="sidebar-header">
        <div v-if="!collapsed" class="sidebar-logo">
          <el-icon class="logo-icon"><Monitor /></el-icon>
          <h1 class="sidebar-title">ClawDash</h1>
        </div>
        <el-icon v-else class="sidebar-icon-mini"><Monitor /></el-icon>
      </div>

      <el-scrollbar>
        <el-menu 
          :default-active="$route.path" 
          :collapse="collapsed" 
          router 
          class="sidebar-menu"
          :collapse-transition="false"
        >
          <template v-for="group in menuGroups" :key="group.title">
            <el-sub-menu :index="group.title" v-if="group.items.length > 1 && !collapsed">
              <template #title>
                <el-icon><component :is="group.icon" /></el-icon>
                <span>{{ group.title }}</span>
              </template>
              <el-menu-item 
                v-for="item in group.items" 
                :key="item.path" 
                :index="item.path"
              >
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </el-menu-item>
            </el-sub-menu>

            <template v-else-if="collapsed">
              <el-sub-menu :index="group.title">
                <template #title>
                  <el-icon><component :is="group.icon" /></el-icon>
                </template>
                <el-menu-item 
                  v-for="item in group.items" 
                  :key="item.path" 
                  :index="item.path"
                >
                  <el-icon><component :is="item.icon" /></el-icon>
                  <span>{{ item.label }}</span>
                </el-menu-item>
              </el-sub-menu>
            </template>

            <el-menu-item 
              v-if="group.items.length === 1 && !collapsed"
              :index="group.items[0].path"
            >
              <el-icon><component :is="group.items[0].icon" /></el-icon>
              <span>{{ group.items[0].label }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="glass-toolbar header">
        <div class="header-left">
          <el-button text @click="collapsed = !collapsed" class="header-btn">
            <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          </el-button>
          <span class="header-title">{{ currentPageName }}</span>
        </div>

        <div class="header-right">
          <div class="locale-toggle" @click="toggleLocale">
            <span class="locale-label">{{ localeLabel }}</span>
          </div>

          <div class="theme-toggle" @click="toggleThemeMenu">
            <el-icon><component :is="currentThemeOption().icon" /></el-icon>
            <span class="theme-label">{{ currentThemeOption().label }}</span>
          </div>

          <el-button text class="settings-btn"><el-icon><Setting /></el-icon></el-button>

          <transition name="fade">
            <div v-if="themeOpen" class="theme-dropdown">
              <div
                v-for="option in themeOptions"
                :key="option.value"
                class="theme-option"
                :class="{ active: theme === option.value }"
                @click.stop="handleThemeClick(option.value)"
              >
                <el-icon><component :is="option.icon" /></el-icon>
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
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--border);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  font-size: 28px;
  color: var(--primary);
}

.sidebar-title {
  font-size: 18px;
  font-weight: bold;
  color: var(--primary);
  margin: 0;
}

.sidebar-icon-mini {
  font-size: 24px;
  color: var(--primary);
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
  position: relative;
  z-index: 101;
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

.theme-toggle .el-icon {
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
}

.settings-btn:hover {
  color: var(--primary);
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

.theme-option .el-icon {
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
