import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      children: [
        { path: '', redirect: '/overview' },
        { path: 'overview', component: () => import('@/views/overview/index.vue') },
        { path: 'agents', component: () => import('@/views/Agents.vue') },
        { path: 'agents-config', component: () => import('@/views/AgentsConfig.vue') },
        { path: 'agent-graph', component: () => import('@/views/AgentGraph.vue') },
        { path: 'cron', component: () => import('@/views/Cron.vue') },
        { path: 'tasks', component: () => import('@/views/TaskQueue.vue') },
        { path: 'task-types', component: () => import('@/views/TaskTypes.vue') },
        { path: 'task-group', component: () => import('@/views/TaskGroup.vue') },
        { path: 'tokens', component: () => import('@/views/Tokens.vue') },
        { path: 'failures', component: () => import('@/views/Failures.vue') },
        { path: 'sessions', component: () => import('@/views/Sessions.vue') },
        { path: 'openclaw', component: () => import('@/views/OpenClaw.vue') },
        { path: 'docker', component: () => import('@/views/Docker.vue') }
      ]
    },
    { path: '/:pathMatch(.*)*', component: () => import('@/views/404.vue') }
  ]
})

export default router
