<template>
  <div class="openclaw">
    <el-card class="status-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('openclaw.title') }}</span>
          <el-button type="primary" @click="refreshStatus">{{ t('openclaw.refresh') }}</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item :label="t('openclaw.status.title')">
          <el-tag :type="status.running ? 'success' : 'danger'">
            {{ status.running ? t('openclaw.status.running') : t('openclaw.status.notRunning') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('openclaw.status.dashboard')">
          <a v-if="status.apiUrl" :href="status.apiUrl" target="_blank" class="api-link">{{ t('openclaw.status.open') }}</a>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item :label="t('openclaw.status.token')">
          <span v-if="status.token" class="token">{{ status.token }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item :label="t('openclaw.status.lastUpdated')">{{ displayTime }}</el-descriptions-item>
        <el-descriptions-item :label="t('openclaw.status.error')">{{ status.error || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="actions">
        <el-button 
          :type="detectStatus === 'success' ? 'success' : detectStatus === 'failed' ? 'danger' : 'default'"
          :loading="detectStatus === 'loading'"
          @click="handleDetect"
        >
          <el-icon v-if="detectStatus === 'success'" class="el-icon--left"><Check /></el-icon>
          {{ detectButtonText }}
        </el-button>
        <el-button @click="showConfigDialog = true">{{ t('openclaw.actions.configure') }}</el-button>
      </div>

    <el-dialog v-model="showConfigDialog" :title="t('openclaw.config.title')" width="400px">
      <el-form label-width="100px">
        <el-form-item :label="t('openclaw.config.configPath')">
          <el-input v-model="configPath" :placeholder="t('openclaw.config.configPathPlaceholder')" @change="handleConfigPathChange" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showConfigDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>
    </el-card>

    <el-card class="mcp-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('openclaw.integration.title') }}</span>
          <el-button type="default" size="small" @click="showIntegrationDialog = true">
            {{ t('openclaw.integration.switchMethod') }}
          </el-button>
        </div>
      </template>

      <div v-if="integrationType === 'skill'" class="clawdash-skill-detail">
        <div class="skill-header">
          <span class="skill-title">🤖 ClawDash Skill</span>
          <el-tag type="success" size="small">已配置</el-tag>
          <el-button type="primary" size="small" :loading="syncingSkill" @click="confirmSyncSkill" style="margin-left: auto">
            安装/更新 Skill
          </el-button>
        </div>
        <p class="skill-desc">{{ skillContent?.description || '加载中...' }}</p>
        
        <div class="skill-info">
          <div class="info-row">
            <span class="info-label">API 地址:</span>
            <span class="info-value">{{ skillContent?.apiUrl || '-' }}</span>
          </div>
        </div>

        <el-collapse v-model="showApiEndpoints" class="api-endpoints">
          <el-collapse-item title="📡 API 端点" name="endpoints">
            <div class="endpoint-list" v-if="skillContent?.endpoints?.length">
              <div v-for="ep in skillContent.endpoints" :key="ep.path + ep.method" class="endpoint-item">
                <el-tag :type="ep.method === 'POST' ? 'success' : ep.method === 'PUT' ? 'warning' : 'primary'" size="small">{{ ep.method }}</el-tag>
                <span class="endpoint-path">{{ ep.path }}</span>
                <span class="endpoint-desc">{{ ep.description }}</span>
              </div>
            </div>
            <div v-else class="endpoint-list">
              <div class="endpoint-item">暂无 API 端点</div>
            </div>
          </el-collapse-item>
        </el-collapse>

        <div class="skill-meta">
          <div class="meta-item">
            <span class="meta-label">任务类型:</span>
            <template v-if="skillContent?.taskTypes?.length">
              <el-tag v-for="tt in skillContent.taskTypes" :key="tt.name" size="small" style="margin-right: 4px">{{ tt.name }}</el-tag>
            </template>
            <span v-else>-</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">优先级:</span>
            <span>1-10 (数字越大优先级越高)</span>
          </div>
        </div>

        <div class="skill-footer">
          <el-button type="info" plain size="small" @click="handleShowInstalled">
            <el-icon class="el-icon--left"><Document /></el-icon>
            查看已安装
          </el-button>
        </div>
      </div>

      <el-alert
        v-else
        type="warning"
        :closable="false"
      >
        <template #title>
          <span>{{ t('openclaw.integration.mcpServer.name') }}（{{ t('openclaw.integration.mcpServer.status') }}）</span>
        </template>
        {{ t('openclaw.integration.mcpServer.description') }}
      </el-alert>
    </el-card>

    <!-- 接入方式选择对话框 -->
    <el-dialog v-model="showIntegrationDialog" :title="t('openclaw.integration.title')" width="700px">
      <el-alert type="info" :closable="false" style="margin-bottom: 20px">
        {{ t('openclaw.integration.switchWarning') }}
      </el-alert>

      <el-table :data="integrationOptions" style="width: 100%">
        <el-table-column prop="name" :label="t('openclaw.integration.skillApi.name')" width="160" />
        <el-table-column prop="status" :label="t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.statusType" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="t('common.description')" />
        <el-table-column :label="t('common.actions')" width="140">
          <template #default="{ row }">
            <el-button
              v-if="row.type === integrationType"
              type="success"
              size="small"
              disabled
            >
              {{ t('openclaw.integration.currentMethod') }}
            </el-button>
            <el-button
              v-else
              type="primary"
              size="small"
              :disabled="row.status === t('openclaw.integration.mcpServer.status')"
              @click="handleIntegrate(row.type)"
            >
              {{ row.status === t('openclaw.integration.mcpServer.status') ? t('openclaw.integration.mcpServer.status') : t('openclaw.integration.switchMethod') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-divider />

      <h4>{{ t('openclaw.integration.comparison.title') }}</h4>
      <el-table :data="comparisonData" style="width: 100%" size="small">
        <el-table-column prop="feature" :label="t('openclaw.integration.comparison.feature')" width="150" />
        <el-table-column prop="skill" :label="t('openclaw.integration.comparison.skill')" />
        <el-table-column prop="mcp" :label="t('openclaw.integration.comparison.mcp')" />
      </el-table>

      <template #footer>
        <el-button @click="showIntegrationDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <el-card class="plugins-card">
      <template #header>
        <span>{{ t('openclaw.plugins.title') }}</span>
      </template>

      <el-table :data="paginatedPlugins" style="width: 100%">
        <el-table-column prop="name" :label="t('common.name')" />
        <el-table-column :label="t('common.status')">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? t('common.enabled') : t('common.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.actions')">
          <template #default="{ row }">
            <el-button size="small" @click="handleTogglePlugin(row.name)">
              {{ row.enabled ? t('common.disable') : t('common.enable') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="pluginPagination.page"
          :page-size="pluginPagination.pageSize"
          :total="pluginPagination.total"
          layout="prev, pager, next"
          small
          @current-change="handlePluginPageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detectDialogVisible" :title="t('openclaw.autoDetect.title')" width="500px">
      <div v-if="detectResult">
        <el-alert :type="detectResult.running ? 'success' : 'warning'" :title="detectResult.running ? t('openclaw.autoDetect.running') : t('openclaw.autoDetect.notRunning')" :closable="false" style="margin-bottom: 20px" />
        
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="t('openclaw.autoDetect.apiAddress')">{{ detectResult.apiUrl }}</el-descriptions-item>
          <el-descriptions-item v-if="detectResult.error" :label="t('openclaw.autoDetect.errorState')">{{ detectResult.error }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="detectResult.running && detectResult.plugins" style="margin-top: 20px">
          <h4>{{ t('openclaw.autoDetect.enabledPlugins') }}</h4>
          <el-tag v-for="(info, name) in detectResult.plugins" :key="name" :type="info.enabled ? 'success' : 'info'" style="margin-right: 8px; margin-bottom: 8px">
            {{ name }} {{ info.enabled ? '✅' : '❌' }}
          </el-tag>
        </div>

        <div v-if="detectResult.running && detectResult.workspaces && detectResult.workspaces.length" style="margin-top: 20px">
          <h4>{{ t('openclaw.autoDetect.workspaces') }}</h4>
          <el-tag v-for="ws in detectResult.workspaces" :key="ws" style="margin-right: 8px; margin-bottom: 8px">
            {{ ws }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="detectDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button v-if="detectResult?.running" type="primary" @click="handleConfirmConnect">{{ t('openclaw.autoDetect.confirmConnect') }}</el-button>
        <el-button v-else type="primary" @click="detectDialogVisible = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showInstalledDialog" title="已安装的 Skill" width="800px">
      <el-empty v-if="!installedSkillContent" description="Skill 未安装或文件不存在">
        <el-button type="primary" @click="handleSyncSkill">立即安装</el-button>
      </el-empty>
      <pre v-else class="skill-content-pre">{{ installedSkillContent }}</pre>
      <template #footer>
        <el-button @click="showInstalledDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Document } from '@element-plus/icons-vue'
import {
  getOpenClawStatus,
  installOpenClaw,
  uninstallOpenClaw,
  getOpenClawPlugins,
  togglePlugin,
  autoDetectOpenClaw,
  confirmConnect,
  installClawdashSkill,
  uninstallClawdashSkill,
  type OpenClawStatus,
  type AutoDetectResult
} from '@/api/openclaw'

const { t, locale } = useI18n()

const formatTime = (timestamp: string) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString(locale.value === 'zh' ? 'zh-CN' : 'en-US')
}

const displayTime = computed(() => formatTime(status.value.timestamp))

const status = ref<OpenClawStatus>({
  running: false,
  apiUrl: '',
  timestamp: '',
  error: ''
})

const pluginList = ref<{ name: string; enabled: boolean }[]>([])
const pluginPagination = ref({ page: 1, pageSize: 20, total: 0 })
const detectDialogVisible = ref(false)
const detectResult = ref<AutoDetectResult | null>(null)
const showConfigDialog = ref(false)
const configPath = ref(localStorage.getItem('openclawConfigPath') || '~/.openclaw')
const showIntegrationDialog = ref(false)
const showApiEndpoints = ref(['endpoints'])
const integrationType = ref<'skill' | 'mcp'>('skill')
const detectStatus = ref<'idle' | 'loading' | 'success' | 'failed'>('idle')
const installedSkills = ref<{ name: string; description: string }[]>([])
const syncingSkill = ref(false)
const skillContent = ref<{ description: string; apiUrl: string; endpoints: any[]; taskTypes: any[] } | null>(null)
const showInstalledDialog = ref(false)
const installedSkillContent = ref('')

const detectButtonText = computed(() => {
  switch (detectStatus.value) {
    case 'loading': return t('openclaw.actions.detecting')
    case 'success': return t('openclaw.actions.connected')
    case 'failed': return t('openclaw.actions.detectFailed')
    default: return t('openclaw.actions.detect')
  }
})

const integrationOptions = computed(() => [
  {
    type: 'skill',
    name: t('openclaw.integration.skillApi.name'),
    status: t('openclaw.integration.skillApi.status'),
    statusType: 'success',
    description: t('openclaw.integration.skillApi.description')
  },
  {
    type: 'mcp',
    name: t('openclaw.integration.mcpServer.name'),
    status: t('openclaw.integration.mcpServer.status'),
    statusType: 'warning',
    description: t('openclaw.integration.mcpServer.description')
  }
])

const comparisonData = computed(() => [
  { feature: t('openclaw.integration.complexity'), skill: t('openclaw.integration.simple'), mcp: t('openclaw.integration.requiresMcpConfig') },
  { feature: t('openclaw.integration.tokenUsage'), skill: t('openclaw.integration.higher'), mcp: t('openclaw.integration.lower') },
  { feature: t('openclaw.integration.standardization'), skill: t('openclaw.integration.nonStandard'), mcp: t('openclaw.integration.mcpStandard') },
  { feature: t('openclaw.integration.toolDiscovery'), skill: t('openclaw.integration.noDiscovery'), mcp: t('openclaw.integration.autoDiscovery') },
  { feature: t('openclaw.integration.stability'), skill: t('openclaw.integration.stable'), mcp: t('openclaw.integration.waiting') }
])

const handleIntegrate = async (type: string) => {
  if (type === 'skill') {
    if (integrationType.value === 'mcp') {
      ElMessage.info(t('openclaw.message.switchToSkill'))
    }
    await handleSkillIntegration()
  } else if (type === 'mcp') {
    ElMessage.info(t('openclaw.message.mcpWaiting'))
  }
}

const handleSkillIntegration = async () => {
  try {
    const backendUrl = status.value.apiUrl 
      ? status.value.apiUrl.replace('3000', '5178')
      : 'http://localhost:5178'
    
    const res = await installClawdashSkill(`${backendUrl}`) as any
    if (res.code === 200) {
      ElMessage.success(t('openclaw.message.configSuccess'))
      showIntegrationDialog.value = false
      integrationType.value = 'skill'
    } else {
      ElMessage.error(res.message || t('openclaw.message.configFailed'))
    }
  } catch (e: any) {
    ElMessage.error(e?.message || t('openclaw.message.configFailed'))
  }
}

const refreshStatus = async () => {
  try {
    const res = await getOpenClawStatus() as any
    if (res.code === 200 && res.data) {
      status.value.running = res.data.running
      // 替换 Docker 内部地址为浏览器可访问的地址
      status.value.apiUrl = (res.data.apiUrl || '')
        .replace(/host\.docker\.internal/g, 'localhost')
        .replace(/172\.17\.0\.1/g, 'localhost')
      status.value.token = res.data.token
      status.value.timestamp = res.data.timestamp
      status.value.error = res.data.error
    }
  } catch (e: any) {
    ElMessage.error(e?.message || t('openclaw.message.fetchStatusFailed'))
  }
}

const loadPlugins = async () => {
  try {
    const res = await getOpenClawPlugins() as any
    if (res.code === 200 && res.data) {
      const sorted = (res.data.available || []).map((name: string) => ({
        name,
        enabled: (res.data.enabled || []).includes(name)
      })).sort((a, b) => {
        if (a.enabled !== b.enabled) return b.enabled ? 1 : -1
        return a.name.localeCompare(b.name)
      })
      pluginList.value = sorted
      pluginPagination.value.total = sorted.length
    }
  } catch (e: any) {
    ElMessage.error(e?.message || t('openclaw.message.fetchPluginsFailed'))
  }
}

const loadSkillContent = async () => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_BASE || 'http://localhost:5178'}/api/openclaw/skill/content`)
    const result = await res.json()
    if (result.code === 200 && result.data) {
      skillContent.value = result.data
    }
  } catch (e: any) {
    console.error('Failed to load skill content:', e)
  }
}

const loadInstalledSkill = async () => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_BASE || 'http://localhost:5178'}/api/openclaw/skill/installed`)
    const result = await res.json()
    if (result.code === 200 && result.data) {
      installedSkillContent.value = result.data.content || ''
    }
  } catch (e: any) {
    console.error('Failed to load installed skill:', e)
  }
}

const handleShowInstalled = async () => {
  await loadInstalledSkill()
  showInstalledDialog.value = true
}

const paginatedPlugins = computed(() => {
  const start = (pluginPagination.value.page - 1) * pluginPagination.value.pageSize
  return pluginList.value.slice(start, start + pluginPagination.value.pageSize)
})

const handlePluginPageChange = (page: number) => {
  pluginPagination.value.page = page
}

const handleDetect = async () => {
  detectStatus.value = 'loading'
  try {
    const res = await autoDetectOpenClaw(configPath.value) as any
    if (res.code === 200 && res.data) {
      detectResult.value = res.data as AutoDetectResult
      
      if (res.data.running) {
        // Auto-save on success
        localStorage.setItem('openclawConfigPath', configPath.value)
        const confirmRes = await confirmConnect(res.data.apiUrl, res.data.token || '', configPath.value) as any
        if (confirmRes.code === 200) {
          detectStatus.value = 'success'
          refreshStatus()
        } else {
          detectStatus.value = 'failed'
        }
      } else {
        detectStatus.value = 'failed'
        ElMessage.warning(t('openclaw.message.notRunningWarning'))
      }
    } else {
      detectStatus.value = 'failed'
      ElMessage.error(res.message || t('openclaw.message.detectFailed'))
    }
  } catch (e: any) {
    detectStatus.value = 'failed'
    ElMessage.error(e.message || t('openclaw.message.detectFailed'))
  }
}

const handleConfigPathChange = () => {
  localStorage.setItem('openclawConfigPath', configPath.value)
  handleDetect()
}

const handleConfirmConnect = async () => {
  if (!detectResult.value) return
  try {
    const res = await confirmConnect(detectResult.value.apiUrl, detectResult.value.token || '', configPath.value) as any
    if (res.code === 200) {
      ElMessage.success(t('openclaw.message.connectSuccess'))
      detectDialogVisible.value = false
      detectStatus.value = 'success'
      refreshStatus()
    } else {
      ElMessage.error(res.message || t('openclaw.message.connectFailed'))
    }
  } catch (e: any) {
    ElMessage.error(e.message || t('openclaw.message.connectFailed'))
  }
}

const confirmSyncSkill = () => {
  ElMessageBox.confirm(
    '将同步 ClawDash 任务类型到 OpenClaw Skill，确认继续？',
    '同步 Skill',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    handleSyncSkill()
  }).catch(() => {})
}

const handleSyncSkill = async () => {
  syncingSkill.value = true
  try {
    const res = await fetch(`${import.meta.env.VITE_API_BASE || 'http://localhost:5178'}/api/openclaw/skill/sync`, {
      method: 'POST'
    })
    const result = await res.json()
    if (result.code === 200) {
      ElMessage.success('Skill 已同步')
    } else {
      ElMessage.error(result.message || '同步失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '同步失败')
  } finally {
    syncingSkill.value = false
  }
}

const handleTogglePlugin = async (name: string) => {
  try {
    await togglePlugin(name)
    ElMessage.success(t('openclaw.message.operationSuccess'))
    loadPlugins()
  } catch (e) {
    ElMessage.error(t('openclaw.message.operationFailed'))
  }
}

onMounted(() => {
  handleDetect()
  loadPlugins()
  loadSkillContent()
})
</script>

<style scoped>
.openclaw {
  padding: 20px;
}

.status-card {
  margin-bottom: 20px;
}

.mcp-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.api-link {
  color: var(--color-primary, #409eff);
  text-decoration: none;
}

.api-link:hover {
  text-decoration: underline;
}

.token {
  font-family: monospace;
  font-size: 12px;
  color: var(--text-secondary);
  word-break: break-all;
}

.clawdash-skill-detail {
  padding: 8px 0;
}

.skill-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.skill-title {
  font-size: 16px;
  font-weight: 600;
}

.skill-desc {
  color: var(--text-secondary);
  margin-bottom: 16px;
  line-height: 1.6;
}

.skill-info {
  background: var(--bg-color-light, #f5f7fa);
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  gap: 8px;
}

.info-label {
  font-weight: 500;
  color: var(--text-primary);
}

.info-value {
  font-family: monospace;
  color: var(--color-primary);
}

.api-endpoints {
  margin-bottom: 16px;
}

.endpoint-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.endpoint-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.endpoint-path {
  font-family: monospace;
  font-size: 13px;
  color: var(--text-primary);
  min-width: 220px;
}

.endpoint-desc {
  color: var(--text-secondary);
  font-size: 13px;
}

.skill-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color-light, #ebeef5);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-label {
  font-weight: 500;
  color: var(--text-primary);
}

.skill-footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color-light, #ebeef5);
}

.skill-content-pre {
  background: var(--bg-color-light, #f5f7fa);
  padding: 16px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 400px;
  overflow-y: auto;
}
</style>
