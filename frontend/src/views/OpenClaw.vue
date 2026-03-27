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
          <el-button type="primary" size="small" @click="showIntegrationDialog = true">
            {{ t('openclaw.integration.switchMethod') }}
          </el-button>
        </div>
      </template>

      <el-alert
        v-if="integrationType === 'skill'"
        type="success"
        :closable="false"
      >
        <template #title>
          <span>{{ t('openclaw.integration.skillApi.name') }}（{{ t('openclaw.integration.currentMethod') }}）</span>
        </template>
        {{ t('openclaw.integration.skillApi.description') }}
      </el-alert>

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

      <el-alert type="warning" :closable="false" style="margin-top: 20px">
        <template #title>
          <span>{{ t('openclaw.integration.mcpServer.description') }}</span>
        </template>
        {{ t('openclaw.integration.mcpServer.description') }}
      </el-alert>

      <template #footer>
        <el-button @click="showIntegrationDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <el-card class="plugins-card">
      <template #header>
        <span>{{ t('openclaw.plugins.title') }}</span>
      </template>

      <el-table :data="pluginList" style="width: 100%">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
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
const detectDialogVisible = ref(false)
const detectResult = ref<AutoDetectResult | null>(null)
const showConfigDialog = ref(false)
const configPath = ref(localStorage.getItem('openclawConfigPath') || '~/.openclaw')
const showIntegrationDialog = ref(false)
const integrationType = ref<'skill' | 'mcp'>('skill')
const detectStatus = ref<'idle' | 'loading' | 'success' | 'failed'>('idle')

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
      pluginList.value = (res.data.available || []).map((name: string) => ({
        name,
        enabled: (res.data.enabled || []).includes(name)
      }))
    }
  } catch (e: any) {
    ElMessage.error(e?.message || t('openclaw.message.fetchPluginsFailed'))
  }
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
</style>
