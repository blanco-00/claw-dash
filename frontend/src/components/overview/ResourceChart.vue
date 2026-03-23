<script setup lang="ts">
const props = defineProps<{
  loading?: boolean
  cpuUsage?: number
  memoryUsage?: number
  memoryUsed?: number
  memoryTotal?: number
}>()

function formatMemory(mb?: number): string {
  if (!mb) return '0MB'
  if (mb >= 1024) {
    return (mb / 1024).toFixed(1) + 'GB'
  }
  return mb + 'MB'
}
</script>

<template>
  <el-card shadow="hover" class="resource-card">
    <template #header>
      <div class="card-header">
        <span class="card-title">📊 系统资源</span>
      </div>
    </template>
    
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading text-2xl"><Loading /></el-icon>
    </div>
    
    <div v-else class="resource-grid">
      <!-- CPU Card -->
      <div class="resource-item">
        <div class="resource-item-header">
          <div class="resource-icon cpu-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
              <rect x="9" y="9" width="6" height="6" stroke="currentColor" stroke-width="2"/>
              <path d="M9 1V4M15 1V4M9 20V23M15 20V23M1 9H4M1 15H4M20 9H23M20 15H23" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <span class="resource-label">CPU 使用率</span>
        </div>
        
        <div class="resource-value-row">
          <span class="resource-value cpu-value">
            {{ cpuUsage !== undefined && cpuUsage >= 0 ? cpuUsage : '--' }}
          </span>
          <span v-if="cpuUsage !== undefined && cpuUsage >= 0" class="resource-unit">%</span>
          <span v-else class="resource-unit">不支持</span>
        </div>
        
        <div v-if="cpuUsage !== undefined && cpuUsage >= 0" class="progress-bar">
          <div class="progress-fill cpu-fill" :style="{ width: cpuUsage + '%' }"></div>
        </div>
        <div v-else class="progress-bar progress-bar-disabled">
          <div class="progress-fill" style="width: 0%"></div>
        </div>
      </div>
      
      <!-- Memory Card -->
      <div class="resource-item">
        <div class="resource-item-header">
          <div class="resource-icon memory-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="2" y="6" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
              <path d="M6 6V18M10 6V18M14 6V18M18 6V18" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <span class="resource-label">内存使用</span>
        </div>
        
        <div class="resource-value-row">
          <span class="resource-value memory-value">{{ memoryUsage || 0 }}</span>
          <span class="resource-unit">%</span>
        </div>
        
        <div class="progress-bar">
          <div class="progress-fill memory-fill" :style="{ width: (memoryUsage || 0) + '%' }"></div>
        </div>
        
        <div class="resource-detail">
          {{ formatMemory(memoryUsed) }} / {{ formatMemory(memoryTotal) }}
        </div>
      </div>
    </div>
  </el-card>
</template>

<script lang="ts">
import { Loading } from '@element-plus/icons-vue'
export default {
  components: { Loading }
}
</script>

<style scoped>
.resource-card {
  background-color: var(--card);
  border-color: var(--border);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.loading-container {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.resource-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.resource-item {
  padding: 14px 16px;
  background: var(--bg-secondary);
  border-radius: 10px;
  transition: all 0.2s ease;
}

.resource-item:hover {
  background: var(--hover-bg, rgba(0, 0, 0, 0.03));
}

.dark .resource-item:hover {
  background: rgba(255, 255, 255, 0.03);
}

.resource-item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.resource-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.cpu-icon {
  background: rgba(236, 72, 153, 0.12);
  color: #ec4899;
}

.memory-icon {
  background: rgba(139, 92, 246, 0.12);
  color: #8b5cf6;
}

.dark .cpu-icon {
  background: rgba(236, 72, 153, 0.2);
}

.dark .memory-icon {
  background: rgba(139, 92, 246, 0.2);
}

.resource-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.resource-value-row {
  display: flex;
  align-items: baseline;
  gap: 2px;
  margin-bottom: 10px;
}

.resource-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.cpu-value {
  color: #ec4899;
}

.memory-value {
  color: #8b5cf6;
}

.resource-unit {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

.progress-bar {
  height: 6px;
  background: var(--border);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar-disabled {
  background: var(--border);
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s ease;
}

.cpu-fill {
  background: linear-gradient(90deg, #ec4899, #f472b6);
}

.memory-fill {
  background: linear-gradient(90deg, #8b5cf6, #a78bfa);
}

.dark .progress-bar {
  background: rgba(255, 255, 255, 0.1);
}

.resource-detail {
  margin-top: 6px;
  font-size: 11px;
  color: var(--text-secondary);
  font-family: 'SF Mono', Monaco, 'Courier New', monospace;
}
</style>
