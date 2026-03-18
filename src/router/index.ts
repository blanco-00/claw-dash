import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/overview'
    },
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      children: [
        { path: 'overview', name: 'overview', component: () => import('@/views/overview/index.vue') },
        { path: 'agents', name: 'agents', component: () => import('@/views/Agents.vue') },
        { path: 'agents-config', name: 'agents-config', component: () => import('@/views/AgentsConfig.vue') },
        { path: 'cron', name: 'cron', component: () => import('@/views/Cron.vue') },
        { path: 'tasks', name: 'tasks', component: () => import('@/views/Tasks.vue') },
        { path: 'task-group', name: 'task-group', component: () => import('@/views/TaskGroup.vue') },
        { path: 'tokens', name: 'tokens', component: () => import('@/views/Tokens.vue') },
        { path: 'failures', name: 'failures', component: () => import('@/views/Failures.vue') },
        { path: 'sessions', name: 'sessions', component: () => import('@/views/Sessions.vue') }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: '404',
      component: () => import('@/views/404.vue')
    }
  ]
})

export default router
