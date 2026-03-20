<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElDrawer, ElIcon } from 'element-plus'
import { Document, Folder, Close } from '@element-plus/icons-vue'
import axios from 'axios'

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
const loading = ref(false)

const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

watch(() => props.visible, async (val) => {
  if (val && props.agentName) {
    await loadFiles()
    if (files.value.length > 0 && !selectedFile.value) {
      selectFile(files.value[0].name)
    }
  }
})

watch(() => props.agentName, async (val) => {
  if (val && drawerVisible.value) {
    await loadFiles()
    if (files.value.length > 0) {
      selectFile(files.value[0].name)
    }
  }
})

async function loadFiles() {
  loading.value = true
  selectedFile.value = null
  fileContent.value = ''
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
  selectedFile.value = filename
  loading.value = true
  try {
    const res = await axios.get(`/api/openclaw/agents/${props.agentName}/files/${filename}`)
    fileContent.value = res.data.data?.content || ''
  } catch (err) {
    console.error('Failed to load file:', err)
    fileContent.value = ''
  } finally {
    loading.value = false
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
    size="70%"
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
            <span class="file-name">{{ file.name }}</span>
          </div>
          <div v-if="files.length === 0 && !loading" class="empty">
            No files found
          </div>
        </div>
      </div>
      
      <div class="file-content">
        <div v-if="selectedFile" class="content-header">
          <span class="content-filename">{{ selectedFile }}</span>
        </div>
        <div v-if="loading" class="loading">Loading...</div>
        <pre v-else class="content-pre">{{ fileContent }}</pre>
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
  width: 220px;
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
  align-items: center;
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

.file-name {
  font-size: 13px;
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
}

.content-filename {
  font-weight: 600;
  font-size: 14px;
}

.loading {
  padding: 20px;
  text-align: center;
  color: var(--text-secondary);
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
