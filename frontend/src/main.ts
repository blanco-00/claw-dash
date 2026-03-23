import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createI18n } from 'vue-i18n'
import App from './App.vue'
import router from './router'
import enMessages from './locales/en.json'
import zhMessages from './locales/zh.json'
import './style.css'

const currentLocale = localStorage.getItem('locale') || 'zh'
const elementLocale = currentLocale === 'zh' ? zhCn : en

const i18n = createI18n({
  legacy: false,
  locale: currentLocale,
  fallbackLocale: 'en',
  messages: {
    en: enMessages,
    zh: zhMessages
  }
})

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus, { locale: elementLocale })
app.use(router)
app.use(i18n)
app.mount('#app')
