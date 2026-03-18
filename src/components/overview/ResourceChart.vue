<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'

// 注册ECharts组件
use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const props = defineProps<{
  loading?: boolean
}>()

// 模拟数据 - 实际应该从系统获取
const cpuData = ref<number[]>([30, 35, 28, 32, 40, 38, 35])
const memoryData = ref<number[]>([45, 48, 46, 50, 52, 49, 47])
const timeLabels = ref<string[]>(['10:00', '10:10', '10:20', '10:30', '10:40', '10:50', '11:00'])

const chartOption = computed(() => ({
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['CPU (%)', '内存 (%)'],
    bottom: 0
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '15%',
    top: '10%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: timeLabels.value
  },
  yAxis: {
    type: 'value',
    min: 0,
    max: 100,
    axisLabel: {
      formatter: '{value}%'
    }
  },
  series: [
    {
      name: 'CPU (%)',
      type: 'line',
      smooth: true,
      data: cpuData.value,
      itemStyle: { color: '#ec4899' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(236, 72, 153, 0.3)' },
            { offset: 1, color: 'rgba(236, 72, 153, 0.05)' }
          ]
        }
      }
    },
    {
      name: '内存 (%)',
      type: 'line',
      smooth: true,
      data: memoryData.value,
      itemStyle: { color: '#8b5cf6' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(139, 92, 246, 0.3)' },
            { offset: 1, color: 'rgba(139, 92, 246, 0.05)' }
          ]
        }
      }
    }
  ]
}))

onMounted(() => {
  // 定时更新数据
  setInterval(() => {
    const now = new Date()
    const time = `${now.getHours()}:${String(now.getMinutes()).padStart(2, '0')}`
    timeLabels.value = [...timeLabels.value.slice(1), time]
    cpuData.value = [...cpuData.value.slice(1), Math.floor(Math.random() * 30 + 20)]
    memoryData.value = [...memoryData.value.slice(1), Math.floor(Math.random() * 20 + 40)]
  }, 10000)
})
</script>

<template>
  <el-card shadow="hover">
    <template #header>
      <span class="font-bold">系统资源</span>
    </template>
    
    <div v-if="loading" class="h-64 flex items-center justify-center">
      <el-icon class="is-loading text-2xl text-gray-400"><Loading /></el-icon>
    </div>
    
    <v-chart v-else class="h-64" :option="chartOption" autoresize />
  </el-card>
</template>

<script lang="ts">
import { Loading } from '@element-plus/icons-vue'
export default {
  components: { Loading, VChart }
}
</script>
