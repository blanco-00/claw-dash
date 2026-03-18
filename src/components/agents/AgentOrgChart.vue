<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import * as d3 from 'd3'

interface AgentNode {
  id: string
  name: string
  title: string
  role: string
  status: string
  children?: AgentNode[]
}

const props = defineProps<{
  agents: AgentNode[]
}>()

const emit = defineEmits<{
  (e: 'nodeClick', agent: any): void
}>()

const chartContainer = ref<HTMLElement | null>(null)

const HIERARCHY_MAP: Record<string, string[]> = {
  main: ['menxiasheng', 'shangshusheng', 'jinyiwei', 'libu4', 'hubu', 'libu3', 'bingbu', 'xingbu'],
  shangshusheng: ['gongbu', 'jishu', 'shangshiju', 'shangyaosi']
}

const LEVEL_COLORS: Record<number, string> = {
  1: '#FFD700',
  2: '#FF69B4',
  3: '#9370DB',
  4: '#87CEEB',
  5: '#90EE90'
}

function getAgentLevel(id: string): number {
  if (id === 'main') return 1
  if (['menxiasheng', 'shangshusheng'].includes(id)) return 2
  if (['jinyiwei', 'libu4', 'hubu', 'libu3', 'bingbu', 'xingbu'].includes(id)) return 3
  if (['gongbu'].includes(id)) return 4
  return 5
}

const AGENT_TEMPLATES: Record<string, { name: string; title: string; role: string }> = {
  main: { name: '瑾儿', title: '皇后', role: '中书省决策' },
  menxiasheng: { name: '卿酒', title: '皇贵妃', role: '门下省审核' },
  shangshusheng: { name: '红袖', title: '贵妃', role: '尚书省分发' },
  jinyiwei: { name: '灵鸢', title: '贵人', role: '锦衣卫督查' },
  libu4: { name: '珊瑚', title: '妃', role: '吏部人事' },
  hubu: { name: '琉璃', title: '妃', role: '户部财务' },
  libu3: { name: '书瑶', title: '妃', role: '礼部外交' },
  bingbu: { name: '魅羽', title: '妃', role: '兵部安全' },
  xingbu: { name: '如意', title: '嫔', role: '刑部法务' },
  gongbu: { name: '灵犀', title: '嫔', role: '工部技术' },
  jishu: { name: '青岚', title: '丫鬟', role: '工部研发' },
  shangshiju: { name: '婉儿', title: '丫鬟', role: '尚食局' },
  shangyaosi: { name: '允贤', title: '丫鬟', role: '尚药司' },
  yanfa: { name: '研发动态', title: '研发', role: '技术研发' }
}

function getAgentInfo(id: string) {
  return AGENT_TEMPLATES[id] || { name: id, title: '待配置', role: '待配置' }
}

function buildHierarchy(): AgentNode {
  const agentSet = new Set<string>()
  props.agents.forEach(a => agentSet.add(a.id))
  Object.values(HIERARCHY_MAP).forEach(arr => arr.forEach(id => agentSet.add(id)))

  const nodeMap = new Map<string, AgentNode>()

  agentSet.forEach(id => {
    const info = getAgentInfo(id)
    nodeMap.set(id, {
      id,
      name: info.name,
      title: info.title,
      role: info.role,
      status: 'offline',
      children: []
    })
  })

  const root = nodeMap.get('main') || {
    id: 'main',
    name: '瑾儿',
    title: '皇后',
    role: '中书省决策',
    status: 'offline',
    children: []
  }

  Object.entries(HIERARCHY_MAP).forEach(([parentId, childIds]) => {
    const parent = nodeMap.get(parentId)
    if (parent) {
      childIds.forEach(childId => {
        const child = nodeMap.get(childId)
        if (child) {
          parent.children = parent.children || []
          parent.children.push(child)
        }
      })
    }
  })

  return root
}

function renderChart() {
  if (!chartContainer.value) return

  d3.select(chartContainer.value).selectAll('*').remove()

  const width = chartContainer.value.clientWidth
  const height = chartContainer.value.clientHeight || 600

  const root = buildHierarchy()
  const hierarchy = d3.hierarchy(root)

  const treeLayout = d3
    .tree<AgentNode>()
    .size([width - 100, height - 100])
    .separation((a, b) => (a.parent === b.parent ? 1 : 1.5))

  const treeData = treeLayout(hierarchy)

  const svg = d3
    .select(chartContainer.value)
    .append('svg')
    .attr('width', width)
    .attr('height', height)
    .append('g')
    .attr('transform', 'translate(50, 50)')

  const link = d3
    .linkVertical<any, any>()
    .x((d: any) => d.x)
    .y((d: any) => d.y)

  svg
    .selectAll('.link')
    .data(treeData.links())
    .enter()
    .append('path')
    .attr('class', 'link')
    .attr('fill', 'none')
    .attr('stroke', '#ccc')
    .attr('stroke-width', 2)
    .attr('d', link)

  const nodes = svg
    .selectAll('.node')
    .data(treeData.descendants())
    .enter()
    .append('g')
    .attr('class', 'node')
    .attr('transform', (d: any) => `translate(${d.x},${d.y})`)
    .style('cursor', 'pointer')
    .on('click', (event, d) => {
      emit('nodeClick', d.data)
    })

  nodes
    .append('rect')
    .attr('x', -60)
    .attr('y', -20)
    .attr('width', 120)
    .attr('height', 40)
    .attr('rx', 8)
    .attr('fill', (d: any) => LEVEL_COLORS[getAgentLevel(d.data.id)] || LEVEL_COLORS[5])
    .attr('stroke', '#fff')
    .attr('stroke-width', 2)

  nodes
    .append('text')
    .attr('text-anchor', 'middle')
    .attr('dy', -5)
    .attr('fill', '#333')
    .attr('font-size', '12px')
    .attr('font-weight', 'bold')
    .text((d: any) => d.data.name)

  nodes
    .append('text')
    .attr('text-anchor', 'middle')
    .attr('dy', 10)
    .attr('fill', '#666')
    .attr('font-size', '10px')
    .text((d: any) => d.data.title)
}

watch(
  () => props.agents,
  () => {
    nextTick(() => renderChart())
  },
  { deep: true }
)

onMounted(() => {
  nextTick(() => renderChart())
  window.addEventListener('resize', renderChart)
})
</script>

<template>
  <div class="org-chart-wrapper">
    <div ref="chartContainer" class="org-chart-container"></div>
  </div>
</template>

<style scoped>
.org-chart-wrapper {
  width: 100%;
  min-height: 600px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  border-radius: 8px;
  overflow: auto;
}

.org-chart-container {
  width: 100%;
  min-height: 600px;
}

:deep(.node:hover rect) {
  filter: brightness(1.1);
}
</style>
