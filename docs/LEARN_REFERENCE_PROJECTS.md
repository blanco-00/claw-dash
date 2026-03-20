# 学习参考项目记录

> 记录从优秀开源项目学习到的实践

## 2026-03-20

### 参考项目

| 项目 | 技术栈 | 主要学习点 |
|-----|-------|-----------|
| [OpenMAIC](https://github.com/DeepWisdom/OpenMAIC) | Next.js 16 + React 19 + LangGraph + Zustand | AI 聊天交互、流式输出、UI 组件 |
| [vue-pure-admin](https://github.com/pure-admin/vue-pure-admin) | Vue 3 + Element Plus + Pinia | 权限系统、布局、国际化、主题 |

---

## 待实现任务

### UI 美观优化 (高优先级)

| 任务 | 来源 | 状态 |
|-----|-----|-----|
| CSS 变量系统重构 (紫色主题) | OpenMAIC | TODO |
| 动画系统 (glow, shimmer, wave) | OpenMAIC | TODO |
| 毛玻璃效果 (Glassmorphism) | OpenMAIC | TODO |
| 增强暗色模式 | OpenMAIC | TODO |
| 现代布局 (悬浮 Header) | OpenMAIC | TODO |
| 组件样式优化 | OpenMAIC | TODO |

### AI 交互组件 (高优先级)

| 任务 | 来源 | 状态 |
|-----|-----|-----|
| PromptInput 输入组件 (支持文件上传) | OpenMAIC | TODO |
| Message 消息组件 (支持分支切换) | OpenMAIC | TODO |
| Tool 工具调用展示 | OpenMAIC | TODO |
| 流式文本 Hook | OpenMAIC | TODO |
| SSE 流式 API | OpenMAIC | TODO |
| 代码高亮组件 | OpenMAIC | TODO |
| Shimmer 骨架屏 | OpenMAIC | TODO |

### 通用后台能力 (中优先级)

| 任务 | 来源 | 状态 |
|-----|-----|-----|
| 权限组件 + 指令 | vue-pure-admin | TODO |
| Token 无感刷新 | vue-pure-admin | TODO |
| 路由权限守卫 | vue-pure-admin | TODO |
| Store 模块化 (Hook 导出) | vue-pure-admin | TODO |
| 多标签页系统 | vue-pure-admin | TODO |
| 主题切换 Hook | vue-pure-admin | TODO |
| 国际化配置 | vue-pure-admin | TODO |

---

## 关键文件参考

### OpenMAIC

```
components/
├── ai-elements/
│   ├── message.tsx              # 消息组件 (复合组件模式)
│   ├── prompt-input.tsx        # 输入组件 (1267行完整实现)
│   ├── tool.tsx                # 工具调用展示
│   ├── reasoning.tsx           # 推理过程展示
│   ├── code-block.tsx          # 代码高亮 (Shiki)
│   └── shimmer.tsx             # 骨架屏动画
│
lib/
├── store/settings.ts           # Zustand + persist
├── hooks/use-streaming-text.ts # 流式文本 Hook
├── utils/cn.ts                 # clsx + tailwind-merge
└── buffer/stream-buffer.ts    # 流缓冲层

app/
├── api/chat/route.ts           # SSE 流式 API
└── globals.css                # Tailwind 4 + CSS 变量
```

### vue-pure-admin

```
src/
├── components/ReAuth/          # 权限组件
├── directives/auth/            # 权限指令
├── utils/
│   ├── auth.ts                 # Token 管理
│   └── http/index.ts           # HTTP 封装 (Token 刷新)
├── store/modules/
│   ├── user.ts                 # 用户状态
│   └── multiTags.ts            # 标签页状态
├── layout/
│   ├── components/lay-tag/     # 标签页组件
│   └── hooks/useDataThemeChange.ts # 主题切换
├── plugins/i18n.ts            # 国际化配置
└── router/index.ts             # 路由守卫
```

---

## 详细学习笔记

### 1. CSS 变量系统

OpenMAIC 使用 Tailwind 4 的 CSS 变量系统：

```css
/* globals.css */
:root {
  --background: oklch(1 0 0);
  --foreground: oklch(0.145 0 0);
  --primary: #722ed1;  /* 紫色主题 */
  --radius: 0.625rem;
}

.dark {
  --background: oklch(0.145 0 0);
  --foreground: oklch(0.985 0 0);
  --primary: #8b47ea;
}
```

### 2. 流式文本 Hook

使用 requestAnimationFrame 实现流畅的打字机效果：

```typescript
// lib/hooks/use-streaming-text.ts
export function useStreamingText(options: StreamingTextOptions) {
  const [displayedText, setDisplayedText] = useState('');
  
  useEffect(() => {
    const animate = (timestamp: number) => {
      const elapsed = timestamp - startTimeRef.current;
      const targetIndex = Math.min(Math.floor((elapsed / 1000) * speed), text.length);
      setDisplayedText(text.slice(0, targetIndex));
      if (targetIndex < text.length) {
        frameRef.current = requestAnimationFrame(animate);
      }
    };
  }, [text, speed]);
  
  return { displayedText, skip, reset };
}
```

### 3. 权限组件模式

```tsx
// 组件方式
<Auth :value="['btn.add']">
  <el-button>有权限才显示</el-button>
</Auth>

// 指令方式
<el-button v-auth="['btn.add']">添加</el-button>
```

### 4. Token 无感刷新

HTTP 拦截器自动处理 Token 过期：

```typescript
// utils/http/index.ts
interceptors.request.use(async (config) => {
  const expired = parseInt(data.expires) - Date.now() <= 0;
  if (expired) {
    // 自动刷新 Token
    if (!PureHttp.isRefreshing) {
      PureHttp.isRefreshing = true;
      await useUserStoreHook().handRefreshToken({ refreshToken });
    }
  }
  return config;
});
```

---

---

## UI 美观实现详解 (来自 OpenMAIC)

### 1. CSS 变量系统 (紫色主题)

```css
/* OpenMAIC 使用 oklch 颜色系统 */
:root {
  --background: oklch(1 0 0);
  --foreground: oklch(0.145 0 0);
  --primary: #722ed1;        /* 紫色主题色 */
  --primary-foreground: oklch(0.985 0 0);
  --radius: 0.625rem;         /* 10px 圆角 */
  --border: oklch(0.922 0 0);
}

.dark {
  --background: oklch(0.145 0 0);
  --foreground: oklch(0.985 0 0);
  --primary: #8b47ea;        /* 暗色模式更亮 */
  --border: oklch(1 0 0 / 10%);  /* 半透明边框 */
}
```

### 2. 动画效果

#### Shimmer 骨架动画 (CSS Keyframe)
```css
@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
.animate-shimmer {
  animation: shimmer 2s infinite;
  background: linear-gradient(to right, transparent, rgba(255,255,255,0.4), transparent);
}
```

#### 脉冲光晕动画
```css
@keyframes setup-glow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(139, 92, 246, 0.4), 0 0 8px rgba(139, 92, 246, 0.15); }
  50% { box-shadow: 0 0 0 6px transparent, 0 0 16px rgba(59, 130, 246, 0.2); }
}
.animate-setup-glow { animation: setup-glow 2.5s ease-in-out infinite; }
```

#### Framer Motion 入场动画
```tsx
import { motion } from 'motion/react';

<motion.div
  initial={{ opacity: 0, y: 20 }}
  animate={{ opacity: 1, y: 0 }}
  transition={{ duration: 0.6, ease: 'easeOut' }}
/>
```

### 3. 毛玻璃效果

```tsx
// 导航栏毛玻璃
<div className="bg-white/60 dark:bg-gray-800/60 backdrop-blur-md rounded-full border border-gray-100/50">
  {/* 内容 */}
</div>

// 对话框遮罩
<div className="fixed inset-0 bg-black/10 backdrop-blur-xs" />

// 浮动卡片
<div className="bg-white/95 dark:bg-gray-800/95 backdrop-blur-sm rounded-2xl shadow-lg">
```

### 4. 渐变和光晕

```tsx
// 背景光晕装饰
<div className="absolute w-96 h-96 bg-purple-500/10 rounded-full blur-3xl animate-pulse" />
<div className="absolute w-96 h-96 bg-blue-500/10 rounded-full blur-3xl animate-pulse" style={{ animationDuration: '4s' }} />

// 渐变按钮
<button className="bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-500 hover:to-amber-600 text-white rounded-lg shadow-sm shadow-amber-200/50">
```

### 5. 暗色模式 Hook

```tsx
// lib/hooks/use-theme.tsx
'use client';
import { createContext, useContext, useEffect, useState } from 'react';

export function ThemeProvider({ children }) {
  const [theme, setThemeState] = useState<'light' | 'dark' | 'system'>('system');
  
  useEffect(() => {
    const root = document.documentElement;
    const resolved = theme === 'system' 
      ? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
      : theme;
    root.classList.toggle('dark', resolved === 'dark');
  }, [theme]);

  return <ThemeContext.Provider value={{ theme, setTheme: setThemeState }}>{children}</ThemeContext.Provider>;
}
```

### 6. 按钮组件样式 (cva 模式)

```tsx
import { cva } from 'class-variance-authority';

const buttonVariants = cva(
  "rounded-md border text-sm font-medium transition-all focus-visible:ring-[3px]",
  {
    variants: {
      variant: {
        default: 'bg-primary text-primary-foreground hover:bg-primary/80',
        outline: 'border-border bg-background hover:bg-muted dark:bg-input/30',
        ghost: 'hover:bg-muted hover:text-foreground',
        destructive: 'bg-destructive/10 hover:bg-destructive/20 text-destructive',
      },
      size: {
        default: 'h-9 px-2.5',
        sm: 'h-8 px-2.5',
        lg: 'h-10 px-3',
        icon: 'size-9',
      },
    },
  }
);
```

---

## 快速应用到 claw-dash 的步骤

1. **CSS 变量** → 复制到 `src/style.css` 的 `:root` 和 `.dark`
2. **安装 motion** → `pnpm add motion`
3. **主题切换** → 创建 `src/composables/useTheme.ts`
4. **毛玻璃** → 在 Header/Sidebar 组件添加 `backdrop-blur-*` 类
5. **背景光晕** → 在首页/Dashboard 添加装饰性 blur div

---

## 侧边栏菜单系统 (来自 vue-pure-admin)

### 当前 claw-dash 菜单问题

```typescript
// 当前简陋的菜单配置 (layout/index.vue)
const menuItems = [
  { path: '/overview', icon: '📊', label: '总览' },
  { path: '/agents', icon: '🏛️', label: '组织架构' },
  // 问题：扁平结构，没有分组，样式简陋
]
```

### 改进后的菜单数据结构

```typescript
// 参考 vue-pure-admin 的路由 meta 配置
interface MenuMeta {
  title: string;              // 菜单名称
  icon?: string;              // 图标 (如 "ep/home-filled")
  rank?: number;              // 排序号
  roles?: string[];           // 权限角色
  showLink?: boolean;          // 是否显示
  keepAlive?: boolean;        // 缓存
}

// 路由配置示例
const routes = [
  {
    path: '/dashboard',
    redirect: '/dashboard/overview',
    meta: { icon: 'ep/home-filled', title: '仪表盘', rank: 1 },
    children: [
      { path: '/dashboard/overview', meta: { title: '总览' } },
      { path: '/dashboard/analytics', meta: { title: '分析' } }
    ]
  },
  {
    path: '/agents',
    meta: { icon: 'ep/user', title: 'Agent管理', rank: 2 },
    children: [
      { path: '/agents/list', meta: { title: 'Agent列表' } },
      { path: '/agents/graph', meta: { title: 'Agent图谱' } }
    ]
  },
  {
    path: '/system',
    meta: { icon: 'ep/setting', title: '系统管理', rank: 10 },
    children: [
      { path: '/system/tokens', meta: { title: 'Token管理' } },
      { path: '/system/config', meta: { title: '系统配置' } }
    ]
  }
]
```

### 菜单分组建议

| 分组 | 菜单项 |
|-----|--------|
| **仪表盘** | 总览 |
| **Agent** | 组织架构、Agent图谱 |
| **任务** | 定时任务、任务队列、任务类型、任务组 |
| **监控** | 失败追踪、会话 |
| **系统** | Tokens、Docker、OpenClaw |

### 菜单组件实现要点

1. **SidebarItem 递归组件** - 支持多级嵌套
2. **el-menu 折叠** - `collapse` 属性控制
3. **active 状态** - `:default-active="$route.path"`
4. **图标支持** - 使用 Element Plus 图标 (`ep/*`)

```vue
<!-- 参考实现 -->
<el-scrollbar>
  <el-menu :collapse="collapsed" :default-active="activeMenu">
    <sidebar-item 
      v-for="route in menuData" 
      :key="route.path" 
      :item="route" 
    />
  </el-scrollbar>
</el-scrollbar>
```

### 待实现菜单任务

- [ ] 重构菜单数据结构 (添加分组、rank)
- [ ] 实现 SidebarItem 递归组件
- [ ] 添加菜单图标 (Element Plus)
- [ ] 优化菜单样式 (高亮、hover 效果)
- [ ] 添加折叠动画

---

## 更新日志

- 2026-03-20: 初始记录，基于 OpenMAIC 和 vue-pure-admin 分析
  - 添加 UI 美观实现详解
