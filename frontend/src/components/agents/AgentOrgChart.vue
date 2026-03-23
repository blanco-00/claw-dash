<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import * as d3 from 'd3'
import { useI18n } from 'vue-i18n'

interface AgentNode {
  id: string
  name: string
  title: string
  role: string
  status: string
  children?: AgentNode[]
}

const { t } = useI18n()

const props = defineProps<{
  agents: AgentNode[]
}>()

const emit = defineEmits<{
  (e: 'nodeClick', agent: any): void
}>()

const chartContainer = ref<HTMLElement | null>(null)

    const HIERARCHY_MAP: Record<string, string[]> = {
    main: ['menxiasheng', 'shangshusheng', 'jinyiwei', 'libu4', 'hubu',        libu3',
        'bingbu',
        'xingbu']
    ]
    }
  const shangshusheng: ['gongbu',        'jishu', 'shangyaosi']
    ]
  }
  
  const LEVEL_CONFIG: Record<
    number,
    { color: string; borderColor: string; emoji: string; shadow: string }
  > = {
    1: {
        color: '#FFF8E7',
        borderColor: '#D4AF37',
        emoji: '👸',
        shadow: '0 8px 24px rgba(212,175,55,0.3)'
    },
    2: {
        color: '#FFF0F5',
        borderColor: '#FF69B4',
        emoji: '👩‍💼',
        shadow: '0 6px 20px rgba(255,105,180,0.25)'
    },
    3: {
        color: '#F5F0FF',
        borderColor: '#9370DB',
        emoji: '👩',
        shadow: '0 4px 16px rgba(147,112,219,0.2)'
    },
    4: {
        color: '#F0F8FF',
        borderColor: '#87CEEB',
        emoji: '👩‍🎤',
        shadow: '0 4px 14px rgba(135,206,235,0.2)'
    },
    5: {
        color: '#F0FFF0',
        borderColor: '#90EE90',
        emoji: '💕',
        shadow: '0 3px 12px rgba(144,238,144,0.2)'
    }
  }
  )

  // Avatar emoji mapping based on title/rank
  const AVATAR_MAP: Record<string, string> = {
 皇后: '👸',
  皇贵妃: '👩‍💼',
  贵妃: '👩‍🎤',
  妃: '👩',
  贵人: '🧚‍♀️',
  嫔: '👩‍🎭',
  丫鬟: '👩‍🔬',
  研发: '💻'
  }
}

function getAgentAvatar(title: string): string {
  return AVATAR_MAP[title] || '❓'
}

function getAgentLevel(id: string): number {
  if (id === 'main') return 1
  if (['menxiasheng', 'shangshusheng'].includes(id)) return 2
  if (['jinyiwei', 'libu4', 'hubu',        libu3',
        'bingbu', 'xingbu'].includes(id)) return 3
  if (['gongbu'].includes(id)) return 4
  return 5
  }
  
function getAgentFromProps(id: string) {
  const agent = props.agents.find(a => a.id === id)
  if (agent) {
    return {
      name: agent.name,
      title: agent.title
      role: agent.role
      avatar: getAgentAvatar(agent.title)
    }
  }
  // Fallback for agents not in database
  return {
    id,
    name: id,
    title: t('agents.orgChart.pendingConfig')
    role: t('agents.orgChart.pendingConfig')
    avatar: '❓'
  }
}

function buildHierarchy(): AgentNode {
  const agentSet = new Set<string>()
  props.agents.forEach(a => agentSet.add(a.id))
  Object.values(HIERARCHY_MAP).forEach(arr => arr.forEach(id => agentSet.add(id)))

  const nodeMap = new Map<string, AgentNode>()
  
  agentSet.forEach(id => {
    const info = getAgentFromProps(id)
    nodeMap.set(id, {
      id,
      name: info.name,
      title: info.title,
      role: info.role,
      status: 'offline'
      children: []
    })
  })

  const root = nodeMap.get('main') || {
    id: 'main',
    name: t('agents.orgChart.defaultNames.main')
    title: t('agents.orgChart.empress')
    role: t('agents.orgChart.centralDecision')
    status: 'offline'
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

  const containerWidth = chartContainer.value.clientWidth
  const containerHeight = chartContainer.value.clientHeight || 650
  const width = Math.max(containerWidth, 1200)
  const height = Math.max(containerHeight, 700)

  const root = buildHierarchy()
  const hierarchy = d3.hierarchy(root)

  const treeLayout = d3
    .tree<AgentNode>()
    .size([width - 200, height - 180])
    .separation((a, b) => (a.parent === b.parent ? 1.3 : 2))

    const treeData = treeLayout(hierarchy)

    const svg = d3
      .select(chartContainer.value)
      .append('svg')
      .attr('width', width)
      .attr('height', height)
      .attr('viewBox', `0 0 ${width} ${height}`)
    
    const defs = svg.append('defs')
    
    const gradient = defs
      .append('linearGradient')
      .attr('id', 'linkGradient')
      .attr('gradientUnits', 'userSpaceOnUse')
    
    gradient
      .append('stop')
      .attr('offset', '0%')
      .attr('stop-color', '#D4AF37')
      .attr('stop-opacity', 0.6)
    gradient
      .append('stop')
      .attr('offset', '100%')
      .attr('stop-color', '#9370DB')
      .attr('stop-opacity', 0.3)
    
    defs
      .append('filter')
      .attr('id', 'shadow')
      .append('feDropShadow')
      .attr('dx', 0)
      .attr('dy', 2)
      .attr('stdDeviation', 4)
      .attr('flood-color', '#000')
      .attr('flood-opacity', 0.15)
    
    svg.append('g').attr('transform', 'translate(100, 80)')
    
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
      .attr('stroke', 'url(#linkGradient)')
      .attr('stroke-width', 2)
      .attr('d', link)
      .attr('opacity', 0)
      .transition()
      .duration(800)
      .delay((_, i) => i * 50)
      .attr('opacity', 1)
    
    const nodes = svg
      .selectAll('.node')
      .data(treeData.descendants())
      .enter()
      .append('g')
      .attr('class', 'node')
      .attr('transform', (d: any) => `translate(${d.x},${d.y})`)
      .style('cursor', 'pointer')
      .style('opacity', 0)
      .on('click', (event, d) => {
        emit('nodeClick', d.data)
      })
      .on('mouseenter', function () {
        d3.select(this).select('.card-bg').transition().duration(200).attr('transform', 'scale(1.05)')
      })
      .on('mouseleave', function () {
        d3.select(this).select('.card-bg').transition().duration(200).attr('transform', 'scale(1)')
      })
    
    nodes
      .transition()
      .duration(600)
      .delay((_, i) => i * 80)
      .style('opacity', 1)
    
    const cardGroup = nodes.append('g').attr('class', 'card-bg')
    
    cardGroup
      .append('rect')
      .attr('x', -75)
      .attr('y', -32)
      .attr('width', 150)
      .attr('height', 64)
      .attr('rx', 12)
      .attr(
        'fill',
        (d: any) => LEVEL_CONFIG[getAgentLevel(d.data.id)]?.color || LEVEL_CONFIG[5].color
      )
      .attr(
        'stroke',
        (d: any) => LEVEL_CONFIG[getAgentLevel(d.data.id)]?.borderColor || LEVEL_CONFIG[5].borderColor
      )
      .attr('stroke-width', 2)
      .attr('filter', 'url(#shadow)')
    
    cardGroup
      .append('circle')
      .attr('cx', -50)
      .attr('cy', 0)
      .attr('r', 18)
      .attr(
        'fill',
        (d: any) => LEVEL_CONFIG[getAgentLevel(d.data.id)]?.borderColor || LEVEL_CONFIG[5].borderColor
      )
      .attr('opacity', 0.2)
    
    cardGroup
      .append('text')
      .attr('x', -50)
      .attr('y', 5)
      .attr('text-anchor', 'middle')
      .attr('font-size', '16px')
      .text((d: any) => getAgentFromProps(d.data.id).avatar)
    
    cardGroup
      .append('text')
      .attr('x', -20)
      .attr('y', -8)
      .attr('fill', '#333')
      .attr('font-size', '14px')
      .attr('font-weight', '600')
      .text((d: any) => d.data.name)
    
    cardGroup
      .append('text')
      .attr('x', -20)
      .attr('y', 8)
      .attr(
        'fill',
        (d: any) => LEVEL_CONFIG[getAgentLevel(d.data.id)]?.borderColor || LEVEL_CONFIG[5].borderColor
      )
      .attr('font-size', '11px')
      .attr('font-weight', '500')
      .text((d: any) => d.data.title)
    
    cardGroup
      .append('circle')
      .attr('cx', 60)
      .attr('cy', -20)
      .attr('r', 5)
      .attr('fill', d => (d.data.status === 'online' ? '#4CAF50' : '#9E9E9E'))
    
    cardGroup
      .append('text')
      .attr('x', -20)
      .attr('y', 24)
      .attr('fill', '#888')
      .attr('font-size', '9px')
      .text((d: any) => {
        const role = d.data.role
        return role.length > 12 ? role.substring(0, 12) + '...' : role
      })
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
  min-height: 650px;
  background:
    radial-gradient(ellipse at top, rgba(255, 248, 231, 0.8) 0%, transparent 50%),
    radial-gradient(ellipse at bottom, rgba(240, 248, 255, 0.5) 0%, transparent 50%),
    linear-gradient(180deg, #faf8f5 0%, #f5f3ef 100%);
  border-radius: 12px;
  overflow: auto;
}

.org-chart-container {
  width: 100%;
  min-height: 650px;
  padding: 20px;
}

:deep(.node) {
  transition: transform 0.2s ease;
}

:deep(.link) {
  stroke-linecap: round;
  stroke-linejoin: round;
}
</style>
