<template>
  <div class="agent-graph-canvas">
    <VueFlow
      v-model="graphData"
      :default-viewport="{ zoom: 1 }"
      :min-zoom="0.1"
      :max-zoom="2"
      fit-view-on-init
      class="vue-flow-basic"
      @node-click="handleNodeClick"
      @edge-click="handleEdgeClick"
      @pane-click="handlePaneClick"
    >
      <Background pattern-color="#aaa" :gap="16" />
      <Controls />
      <MiniMap />

      <template #node-custom="props">
        <AgentGraphNode :data="props.data" :selected="props.selected" />
      </template>
    </VueFlow>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { VueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import { MarkerType } from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'

import type { Node, Edge } from '@vue-flow/core'
import AgentGraphNode from './AgentGraphNode.vue'
import type {
  AgentNode as AgentNodeType,
  AgentEdge as AgentEdgeType,
  AgentGraph
} from '@/types/agentGraph'
import { loadGraph, saveGraph } from '@/lib/openclaw/agentApi'

interface Props {
  modelValue?: AgentGraph
}

interface Emits {
  (e: 'update:modelValue', value: AgentGraph): void
  (e: 'nodeClick', node: AgentNodeType): void
  (e: 'edgeClick', edge: AgentEdgeType): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({ version: '1.0', lastSync: '', nodes: [], edges: [] })
})

const emit = defineEmits<Emits>()

const internalGraph = ref<AgentGraph>(props.modelValue?.version ? props.modelValue : loadGraph())

const graphData = computed({
  get: () => ({
    nodes: internalGraph.value.nodes.map(node => ({
      id: node.id,
      type: 'custom',
      position: { x: Math.random() * 400, y: Math.random() * 400 },
      data: { ...node }
    })),
    edges: internalGraph.value.edges.map(edge => ({
      id: edge.id,
      source: edge.source,
      target: edge.target,
      type: edge.type === 'reports' ? 'step' : 'default',
      label: edge.type === 'assigns' ? 'assigns' : 'reports',
      markerEnd: { type: MarkerType.ArrowClosed },
      animated: edge.type === 'reports'
    }))
  }),
  set: (val: { nodes: Node[]; edges: Edge[] }) => {
    internalGraph.value = {
      version: internalGraph.value.version,
      lastSync: internalGraph.value.lastSync,
      nodes: val.nodes.map(n => n.data as AgentNodeType),
      edges: val.edges.map(e => ({
        id: e.id,
        source: e.source,
        target: e.target,
        type: e.type === 'step' ? 'reports' : 'assigns'
      })) as AgentEdgeType[]
    }
    emit('update:modelValue', internalGraph.value)
    saveGraph(internalGraph.value)
  }
})

function handleNodeClick(event: { node: Node }) {
  emit('nodeClick', event.node.data as AgentNodeType)
}

function handleEdgeClick(event: { edge: Edge }) {
  const edge = internalGraph.value.edges.find(e => e.id === event.edge.id)
  if (edge) {
    emit('edgeClick', edge)
  }
}

function handlePaneClick() {
  // Clear selection if needed
}

function addNode(node: AgentNodeType) {
  internalGraph.value.nodes.push(node)
  saveGraph(internalGraph.value)
}

function removeNode(nodeId: string) {
  internalGraph.value.nodes = internalGraph.value.nodes.filter(n => n.id !== nodeId)
  internalGraph.value.edges = internalGraph.value.edges.filter(
    e => e.source !== nodeId && e.target !== nodeId
  )
  saveGraph(internalGraph.value)
}

function addEdge(edge: AgentEdgeType) {
  internalGraph.value.edges.push(edge)
  saveGraph(internalGraph.value)
}

function removeEdge(edgeId: string) {
  internalGraph.value.edges = internalGraph.value.edges.filter(e => e.id !== edgeId)
  saveGraph(internalGraph.value)
}

defineExpose({
  addNode,
  removeNode,
  addEdge,
  removeEdge,
  graph: internalGraph
})
</script>

<style scoped>
.agent-graph-canvas {
  width: 100%;
  height: 100%;
  min-height: 400px;
}

.vue-flow-basic {
  background-color: #fafafa;
}
</style>
