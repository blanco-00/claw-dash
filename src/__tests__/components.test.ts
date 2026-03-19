import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { createRouter, createWebHistory } from 'vue-router'

const vuetify = createVuetify({
  components,
  directives
})

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/overview' },
    { path: '/overview', component: { template: '<div>Overview</div>' } },
    { path: '/tasks', component: { template: '<div>Tasks</div>' } },
    { path: '/agents', component: { template: '<div>Agents</div>' } }
  ]
})

describe('Router', () => {
  it('should redirect root to overview', async () => {
    router.push('/')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/overview')
  })

  it('should navigate to tasks', async () => {
    router.push('/tasks')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/tasks')
  })

  it('should navigate to agents', async () => {
    router.push('/agents')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/agents')
  })
})

describe('Utils', () => {
  it('should format date correctly', () => {
    const formatDate = (date: Date) => {
      return date.toLocaleDateString('zh-CN')
    }
    expect(formatDate(new Date('2024-01-01'))).toBe('2024/1/1')
  })

  it('should validate email', () => {
    const isValidEmail = (email: string) => {
      return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
    }
    expect(isValidEmail('test@example.com')).toBe(true)
    expect(isValidEmail('invalid')).toBe(false)
  })
})
