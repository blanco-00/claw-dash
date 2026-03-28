<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useTheme } from '../composables/useTheme'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const collapsed = ref(false)
const { theme, setTheme, isDark } = useTheme()
const { t, locale } = useI18n()

const localeLabel = computed(() => locale.value === 'zh' ? 'CN' : 'EN')

const pageNameMap: Record<string, string> = {
  '/overview': 'overview.title',
  '/agents': 'agents.title',
  '/agent-graph': 'agentGraph.title',
  '/agents-config': 'agentsConfig.title',
  '/agents-config-graph': 'menu.configGraph',
  '/edge-types': 'edgeTypes.title',
  '/cron': 'cron.title',
  '/tasks': 'taskQueue.title',
  '/task-group': 'taskGroup.title',
  '/task-types': 'taskType.title',
  '/task-group': 'taskGroup.title',
  '/sessions': 'sessions.title',
  '/openclaw': 'openclaw.title',
  '/docker': 'docker.title'
}

const currentPageName = computed(() => {
  const path = route.path
  const key = pageNameMap[path] || pageNameMap[path.split('/').slice(0, -1).join('/')]
  return key ? t(key) : t('menu.dashboard')
})

const fetchGatewayStatus = () => {}

const themeOptions = [
  { value: 'light', labelKey: 'theme.light', icon: 'Sunny' },
  { value: 'dark', labelKey: 'theme.dark', icon: 'Moon' },
  { value: 'system', labelKey: 'theme.system', icon: 'Monitor' }
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
  labelKey: string
}

interface MenuGroup {
  titleKey: string
  icon: string
  items: MenuItem[]
}

const menuGroups = computed<MenuGroup[]>(() => [
  {
    titleKey: 'menu.dashboard',
    icon: 'HomeFilled',
    items: [
      { path: '/overview', icon: 'Odometer', labelKey: 'menu.overview' }
    ]
  },
  {
    titleKey: 'menu.agents',
    icon: 'UserFilled',
    items: [
      { path: '/agents-list', icon: 'List', labelKey: 'menu.agentList' },
      { path: '/agents-config-graph', icon: 'Connection', labelKey: 'menu.configGraph' },
      { path: '/edge-types', icon: 'Guide', labelKey: 'menu.edgeTypes' }
    ]
  },
  {
    titleKey: 'menu.tasks',
    icon: 'List',
    items: [
      { path: '/tasks', icon: 'Tickets', labelKey: 'menu.taskQueue' },
      { path: '/task-group', icon: 'Link', labelKey: 'menu.taskGroup' },
      { path: '/task-types', icon: 'Grid', labelKey: 'menu.taskTypes' }
    ]
  },
  {
    titleKey: 'menu.system',
    icon: 'Tools',
    items: [
      { path: '/openclaw', icon: 'Box', labelKey: 'menu.openclaw' },
      { path: '/docker', icon: 'Cpu', labelKey: 'menu.docker' }
    ]
  }
])

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
            <template v-for="group in menuGroups" :key="group.titleKey">
            <el-sub-menu :index="group.titleKey" v-if="group.items.length > 1 && !collapsed">
              <template #title>
                <el-icon><component :is="group.icon" /></el-icon>
                <span>{{ t(group.titleKey) }}</span>
              </template>
              <el-menu-item 
                v-for="item in group.items" 
                :key="item.path" 
                :index="item.path"
              >
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ t(item.labelKey) }}</span>
              </el-menu-item>
            </el-sub-menu>

            <template v-else-if="collapsed">
              <el-sub-menu :index="group.titleKey">
                <template #title>
                  <el-icon><component :is="group.icon" /></el-icon>
                </template>
                <el-menu-item 
                  v-for="item in group.items" 
                  :key="item.path" 
                  :index="item.path"
                >
                  <el-icon><component :is="item.icon" /></el-icon>
                  <span>{{ t(item.labelKey) }}</span>
                </el-menu-item>
              </el-sub-menu>
            </template>

            <el-menu-item 
              v-if="group.items.length === 1 && !collapsed"
              :index="group.items[0].path"
            >
              <el-icon><component :is="group.items[0].icon" /></el-icon>
              <span>{{ t(group.items[0].labelKey) }}</span>
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
            <span class="theme-label">{{ t(currentThemeOption().labelKey) }}</span>
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
                <span>{{ t(option.labelKey) }}</span>
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
  background: var(--sidebar);
  border-right: 1px solid var(--sidebar-border);
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
  border-bottom: 1px solid var(--sidebar-border);
  background: var(--sidebar);
}

.sidebar-title {
  color: var(--sidebar-foreground);
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
  position: relative;
  z-index: 100;
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
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  z-index: 9999;
  min-width: 120px;
}

.dark .theme-dropdown {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.4), 0 4px 6px -2px rgba(0, 0, 0, 0.2);
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
  height: calc(100vh - 64px);
  overflow: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Dark mode sidebar */
.dark .sidebar {
  background: var(--sidebar);
  border-right-color: var(--sidebar-border);
}

.dark .sidebar-header {
  border-bottom-color: var(--sidebar-border);
}

.dark .sidebar-title {
  color: var(--sidebar-foreground);
}
</style>

<style>
/* Global dark mode overrides for Element Plus - must be unscoped to work with 3rd party components */
.dark .sidebar-menu {
  background: transparent !important;
  border: none;
}

/* Dark mode el-menu overrides */
.dark .sidebar-menu:not(.el-menu--collapse) {
  background: transparent !important;
}

.dark .sidebar-menu .el-menu-item,
.dark .sidebar-menu .el-sub-menu__title {
  background: transparent !important;
  color: var(--sidebar-foreground) !important;
}

.dark .sidebar-menu .el-menu-item:hover,
.dark .sidebar-menu .el-sub-menu__title:hover {
  background: var(--sidebar-accent) !important;
  color: var(--sidebar-primary) !important;
}

.dark .sidebar-menu .el-menu-item.is-active {
  background: var(--sidebar-accent) !important;
  color: var(--sidebar-primary) !important;
}

.dark .sidebar-menu .el-sub-menu .el-menu-item {
  background: transparent !important;
}

.dark .sidebar-menu .el-sub-menu .el-menu-item:hover {
  background: var(--sidebar-accent) !important;
}

.dark .sidebar-menu .el-sub-menu .el-menu-item.is-active {
  background: var(--sidebar-accent) !important;
  color: var(--sidebar-primary) !important;
}

/* Collapse icon colors in dark mode */
.dark .sidebar-menu .el-icon {
  color: var(--sidebar-foreground);
}

.dark .sidebar-menu .el-menu-item.is-active .el-icon,
.dark .sidebar-menu .el-sub-menu.is-active .el-sub-menu__title .el-icon {
  color: var(--sidebar-primary);
}

/* Submenu popup dark mode - these styles must override Element Plus defaults */
.dark .el-menu--popup {
  background: var(--sidebar) !important;
  border: 1px solid var(--sidebar-border) !important;
}

.dark .el-menu--popup .el-menu-item {
  background: var(--sidebar) !important;
  color: var(--sidebar-foreground) !important;
}

.dark .el-menu--popup .el-menu-item:hover {
  background: var(--sidebar-accent) !important;
  color: var(--sidebar-primary) !important;
}

.dark .el-menu--popup .el-menu-item.is-active {
  background: var(--sidebar-accent) !important;
  color: var(--sidebar-primary) !important;
}

/* Inline submenu dark mode (used in sidebar when not collapsed) */
.dark .el-menu--inline {
  background: var(--sidebar) !important;
}

.dark .sidebar-menu .el-menu--inline .el-menu-item {
  background: transparent !important;
  color: var(--sidebar-foreground) !important;
}

.dark .sidebar-menu .el-menu--inline .el-menu-item:hover {
  background: var(--sidebar-accent) !important;
  color: var(--sidebar-primary) !important;
}

.dark .sidebar-menu .el-menu--inline .el-menu-item.is-active {
  background: var(--sidebar-accent) !important;
  color: var(--sidebar-primary) !important;
}

.dark .sidebar-menu .el-menu--inline .el-sub-menu__title {
  color: var(--sidebar-foreground) !important;
}

.dark .sidebar-menu .el-menu--inline .el-sub-menu__title:hover {
  background: var(--sidebar-accent) !important;
  color: var(--sidebar-primary) !important;
}
</style>
