<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElDrawer, ElIcon, ElMessage } from 'element-plus'
import { Document, Folder, Close, Edit, Check, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const FILE_DESCRIPTIONS: Record<string, { desc: string; tips: string }> = {
  'IDENTITY.md': {
    desc: 'Agent 身份设定 - 定义你是谁',
    tips: '设置 Name(名称), Creature(角色), Vibe(风格), Emoji, 封号/官职/职责'
  },
  'SOUL.md': {
    desc: 'Agent 灵魂/性格 - 定义你的行为方式',
    tips: '定义性格特点、说话风格、服务范围'
  },
  'MEMORY.md': {
    desc: 'Agent 记忆 - 定义你记住什么',
    tips: '配置记忆策略、记忆持久化规则'
  },
  'TOOLS.md': {
    desc: 'Agent 工具 - 定义你能做什么',
    tips: '配置可使用的工具和权限'
  },
  'AGENTS.md': {
    desc: 'Agent 关联 - 定义你和其他 Agent 的关系',
    tips: '配置与其他 agents 的 A2A 关系'
  },
  'BOOTSTRAP.md': {
    desc: '启动配置 - 定义初始化行为',
    tips: '配置启动时的初始化流程'
  },
  'HEARTBEAT.md': {
    desc: '心跳配置 - 定义健康检查',
    tips: '配置心跳间隔和健康检查逻辑'
  },
  'USER.md': {
    desc: '用户信息 - 定义你对用户的了解',
    tips: '记录用户名称、偏好、上下文'
  }
}

const props = defineProps<{
  visible: boolean
  agentName: string
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const files = ref<Array<{name: string, path: string, size: number, modified: number}>>([])
const selectedFile = ref<string | null>(null)
const fileContent = ref('')
const editingContent = ref('')
const isEditing = ref(false)
const loading = ref(false)
const saving = ref(false)
const hasChanges = ref(false)

const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const currentFileInfo = computed(() => {
  if (!selectedFile.value) return null
  return FILE_DESCRIPTIONS[selectedFile.value] || { desc: '', tips: '' }
})

watch(() => props.visible, async (val) => {
  if (val && props.agentName) {
    isEditing.value = false
    hasChanges.value = false
    await loadFiles()
    if (files.value.length > 0 && !selectedFile.value) {
      selectFile(files.value[0].name)
    }
  }
})

watch(() => props.agentName, async (val) => {
  if (val && drawerVisible.value) {
    isEditing.value = false
    hasChanges.value = false
    await loadFiles()
    if (files.value.length > 0) {
      selectFile(files.value[0].name)
    }
  }
})

watch(fileContent, (val) => {
  if (isEditing.value) {
    hasChanges.value = val !== editingContent.value
  }
})

async function loadFiles() {
  loading.value = true
  selectedFile.value = null
  fileContent.value = ''
  editingContent.value = ''
  try {
    const res = await axios.get(`/api/openclaw/agents/${props.agentName}/files`)
    files.value = res.data.data || []
  } catch (err) {
    console.error('Failed to load files:', err)
    files.value = []
  } finally {
    loading.value = false
  }
}

async function selectFile(filename: string) {
  if (hasChanges.value && !confirm('有未保存的修改，确定要切换文件吗？')) {
    return
  }
  
  selectedFile.value = filename
  loading.value = true
  isEditing.value = false
  hasChanges.value = false
  try {
    const res = await axios.get(`/api/openclaw/agents/${props.agentName}/files/${filename}`)
    fileContent.value = res.data.data?.content || ''
    editingContent.value = fileContent.value
  } catch (err) {
    console.error('Failed to load file:', err)
    fileContent.value = ''
    editingContent.value = ''
  } finally {
    loading.value = false
  }
}

function startEdit() {
  editingContent.value = fileContent.value
  isEditing.value = true
  hasChanges.value = false
}

function cancelEdit() {
  editingContent.value = fileContent.value
  isEditing.value = false
  hasChanges.value = false
}

async function saveContent() {
  if (!selectedFile.value) return
  
  saving.value = true
  try {
    await axios.patch(
      `/api/openclaw/agents/${props.agentName}/files/${selectedFile.value}`,
      { content: editingContent.value }
    )
    fileContent.value = editingContent.value
    isEditing.value = false
    hasChanges.value = false
    ElMessage.success('保存成功')
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function getFileIcon(filename: string) {
  if (filename.endsWith('.md')) return Document
  return Folder
}

function formatSize(bytes: number) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}
</script>

<template>
  <el-drawer
    v-model="drawerVisible"
    :title="agentName"
    size="80%"
    direction="rtl"
    :before-close="() => emit('update:visible', false)"
  >
    <div class="detail-panel">
      <div class="file-sidebar">
        <div class="sidebar-header">
          <span>配置文件</span>
          <a :href="'http://localhost:18789'" target="_blank" class="open-btn">打开 →</a>
        </div>
        <div class="file-list">
          <div
            v-for="file in files"
            :key="file.name"
            class="file-item"
            :class="{ active: selectedFile === file.name }"
            @click="selectFile(file.name)"
          >
            <el-icon><component :is="getFileIcon(file.name)" /></el-icon>
            <div class="file-info">
              <span class="file-name">{{ file.name }}</span>
              <span v-if="FILE_DESCRIPTIONS[file.name]" class="file-desc">
                {{ FILE_DESCRIPTIONS[file.name].desc }}
              </span>
            </div>
          </div>
          <div v-if="files.length === 0 && !loading" class="empty">
            No files found
          </div>
        </div>
      </div>
      
      <div class="file-content">
        <div v-if="selectedFile" class="content-header">
          <div class="content-title">
            <span class="content-filename">{{ selectedFile }}</span>
            <span v-if="currentFileInfo?.desc" class="content-desc">{{ currentFileInfo.desc }}</span>
          </div>
          <div class="content-actions">
            <template v-if="isEditing">
              <el-button type="primary" size="small" :loading="saving" @click="saveContent">
                <el-icon><Check /></el-icon>
                保存
              </el-button>
              <el-button size="small" @click="cancelEdit">取消</el-button>
            </template>
            <template v-else>
              <el-button size="small" @click="startEdit">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
            </template>
          </div>
        </div>
        
        <div v-if="currentFileInfo?.tips" class="content-tips">
          <strong>💡 提示：</strong>{{ currentFileInfo.tips }}
        </div>
        
        <div v-if="loading" class="loading">Loading...</div>
        <template v-else-if="selectedFile">
          <textarea
            v-if="isEditing"
            v-model="editingContent"
            class="content-editor"
            spellcheck="false"
          ></textarea>
          <pre v-else class="content-pre">{{ fileContent }}</pre>
        </template>
      </div>
    </div>
  </el-drawer>
</template>

<style scoped>
.detail-panel {
  display: flex;
  height: 100%;
  margin: -20px;
}

.file-sidebar {
  width: 280px;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
}

.sidebar-header {
  padding: 12px 16px;
  font-weight: 600;
  font-size: 13px;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header .open-btn {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-primary, #409eff);
  text-decoration: none;
}

.sidebar-header .open-btn:hover {
  text-decoration: underline;
}

.file-list {
  flex: 1;
  overflow-y: auto;
}

.file-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.file-item:hover {
  background: var(--bg-hover);
}

.file-item.active {
  background: var(--bg-active, #e8f4ff);
  color: var(--color-primary, #409eff);
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.file-name {
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-desc {
  font-size: 11px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-secondary);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.content-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.content-filename {
  font-weight: 600;
  font-size: 14px;
}

.content-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.content-actions {
  display: flex;
  gap: 8px;
}

.content-tips {
  padding: 10px 16px;
  background: #fffbeb;
  border-bottom: 1px solid #fcd34d;
  font-size: 12px;
  color: #92400e;
}

.loading {
  padding: 20px;
  text-align: center;
  color: var(--text-secondary);
}

.content-editor {
  flex: 1;
  margin: 0;
  padding: 16px;
  border: none;
  resize: none;
  font-size: 13px;
  line-height: 1.6;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: var(--bg-card);
}

.content-editor:focus {
  outline: none;
}

.content-pre {
  flex: 1;
  margin: 0;
  padding: 16px;
  overflow: auto;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.empty {
  padding: 20px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
}
</style>
