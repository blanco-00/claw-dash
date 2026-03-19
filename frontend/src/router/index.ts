import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '@/views/Dashboard.vue'
import Tasks from '@/views/Tasks.vue'
import Agents from '@/views/Agents.vue'
import Finance from '@/views/Finance.vue'
import Settings from '@/views/Settings.vue'
import AgentGraph from '@/views/AgentGraph.vue'
import TaskQueue from '@/views/TaskQueue.vue'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/tasks',
    name: 'Tasks',
    component: Tasks
  },
  {
    path: '/agents',
    name: 'Agents',
    component: Agents
  },
  {
    path: '/agent-graph',
    name: 'AgentGraph',
    component: AgentGraph
  },
  {
    path: '/task-queue',
    name: 'TaskQueue',
    component: TaskQueue
  },
  {
    path: '/finance',
    name: 'Finance',
    component: Finance
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
