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
        {
          path: 'overview',
          name: 'overview',
          component: () => import('@/views/overview/index.vue')
        },
        {
          path: 'agents',
          name: 'agents',
          component: () => import('@/views/Agents.vue')
        },
        {
          path: 'cron',
          name: 'cron',
          component: () => import('@/views/Cron.vue')
        },
        {
          path: 'tasks',
          name: 'tasks',
          component: () => import('@/views/Tasks.vue')
        },
        {
          path: 'sessions',
          name: 'sessions',
          component: () => import('@/views/Sessions.vue')
        }
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
