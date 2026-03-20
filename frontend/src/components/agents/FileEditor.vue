<template>
  <div class="file-editor">
    <div class="editor-toolbar">
      <div class="toolbar-left">
        <span class="file-name">{{ fileName }}</span>
        <el-tag v-if="isModified" type="warning" size="small">Modified</el-tag>
      </div>
      <div class="toolbar-right">
        <el-button size="small" @click="openExternal">
          <el-icon><Launch /></el-icon>
          Open External
        </el-button>
        <el-button size="small" type="primary" :disabled="!isModified" @click="save">
          <el-icon><Document /></el-icon>
          Save
        </el-button>
      </div>
    </div>
    
    <div class="editor-container">
      <textarea
        ref="editorRef"
        v-model="content"
        class="editor-textarea"
        :placeholder="placeholder"
        spellcheck="false"
        @input="onInput"
        @keydown="handleKeydown"
      />
    </div>
    
    <div class="editor-statusbar">
      <span class="status-item">{{ lineCount }} lines</span>
      <span class="status-item">{{ charCount }} characters</span>
      <span v-if="lastSaved" class="status-item">Last saved: {{ lastSaved }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Launch } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: string
  fileName: string
  placeholder?: string
  readOnly?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'save', content: string): void
  (e: 'openExternal'): void
}>()

const editorRef = ref<HTMLTextAreaElement | null>(null)
const content = ref(props.modelValue)
const isModified = ref(false)
const lastSaved = ref<string | null>(null)

const lineCount = computed(() => content.value.split('\n').length)
const charCount = computed(() => content.value.length)

watch(() => props.modelValue, (newVal) => {
  if (newVal !== content.value) {
    content.value = newVal
    isModified.value = false
  }
})

function onInput() {
  isModified.value = content.value !== props.modelValue
  emit('update:modelValue', content.value)
}

function handleKeydown(e: KeyboardEvent) {
  // Ctrl/Cmd + S to save
  if ((e.ctrlKey || e.metaKey) && e.key === 's') {
    e.preventDefault()
    if (isModified.value) {
      save()
    }
    return
  }
  
  // Tab key - insert spaces
  if (e.key === 'Tab') {
    e.preventDefault()
    const start = (e.target as HTMLTextAreaElement).selectionStart
    const end = (e.target as HTMLTextAreaElement).selectionEnd
    const spaces = '  '
    content.value = content.value.substring(0, start) + spaces + content.value.substring(end)
    nextTick(() => {
      if (editorRef.value) {
        editorRef.value.selectionStart = start + spaces.length
        editorRef.value.selectionEnd = start + spaces.length
      }
    })
  }
}

function save() {
  emit('save', content.value)
  lastSaved.value = new Date().toLocaleTimeString()
  isModified.value = false
  ElMessage.success('File saved')
}

function openExternal() {
  emit('openExternal')
}

defineExpose({
  focus: () => editorRef.value?.focus(),
  getContent: () => content.value,
  setContent: (val: string) => { content.value = val }
})
</script>

<style scoped>
.file-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 200px;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-name {
  font-family: monospace;
  font-weight: 600;
  color: var(--text-primary);
}

.editor-container {
  flex: 1;
  overflow: hidden;
}

.editor-textarea {
  width: 100%;
  height: 100%;
  padding: 12px;
  border: none;
  outline: none;
  resize: none;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
  color: var(--text-primary);
  background: var(--bg-primary);
  tab-size: 2;
}

.editor-textarea::placeholder {
  color: var(--text-secondary);
}

.editor-statusbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 6px 12px;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
  font-size: 12px;
  color: var(--text-secondary);
}

.status-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
