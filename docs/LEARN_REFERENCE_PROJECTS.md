# 学习参考项目记录 / Reference Projects

> 记录从优秀开源项目学习到的实践

## 参考项目 / Reference Projects

| 项目 / Project | 技术栈 / Tech | 主要学习点 / Key Learnings |
|---------------|--------------|--------------------------|
| [OpenMAIC](https://github.com/DeepWisdom/OpenMAIC) | Next.js + React + LangGraph + Zustand | AI 聊天交互、流式输出、UI 组件 |
| [vue-pure-admin](https://github.com/pure-admin/vue-pure-admin) | Vue 3 + Element Plus + Pinia | 权限系统、布局、国际化、主题 |
| [Dify](https://github.com/langgenius/dify) | Python + Vue | 可视化工作流编排、拖拽式节点编辑器 |
| [CrewAI](https://github.com/crewAIInc/crewAI) | Python | Role-based agent teams、Flow 装饰器路由 |

---

## 已应用 / Applied

| 学习点 / Learning | 来源 / Source | 状态 / Status |
|------------------|--------------|--------------|
| CSS 紫色主题变量 | OpenMAIC | ✅ 已应用 |
| 动画系统 (shimmer) | OpenMAIC | ✅ 已应用 |
| 毛玻璃效果 (glassmorphism) | OpenMAIC | ✅ 已应用 |
| 暗色模式 | OpenMAIC | ✅ 已应用 |
| Token 无感刷新 | vue-pure-admin | ✅ 已应用 |
| 多标签页系统 | vue-pure-admin | ✅ 已应用 |
| Sidebar 折叠动画 | vue-pure-admin | ✅ 已应用 |

---

## 待应用 / To Apply

| 学习点 / Learning | 来源 / Source | 说明 |
|-------------------|--------------|------|
| PromptInput 文件上传组件 | OpenMAIC | Agent 消息支持附件 |
| 分支切换 Message 组件 | OpenMAIC | 对话分支可视化 |
| Shimmer 骨架屏 | OpenMAIC | 加载态优化 |
| n8n/Dify 式拖拽编排 | Dify | 可视化工作流构建器 |
| Flow 装饰器路由 | CrewAI | `@start/@listen/@router` 模式 |

---

## 关键文件参考 / Key File References

### OpenMAIC

```
components/
├── ai-elements/
│   ├── message.tsx              # 消息组件
│   ├── prompt-input.tsx         # 输入组件
│   ├── tool.tsx                 # 工具调用展示
│   ├── reasoning.tsx            # 推理过程展示
│   ├── code-block.tsx           # 代码高亮 (Shiki)
│   └── shimmer.tsx               # 骨架屏动画

lib/
├── store/settings.ts            # Zustand + persist
├── hooks/use-streaming-text.ts   # 流式文本 Hook
└── utils/cn.ts                  # clsx + tailwind-merge
```

### vue-pure-admin

```
src/
├── components/ReAuth/           # 权限组件
├── directives/auth/             # 权限指令
├── store/modules/user.ts        # 用户状态
├── layout/hooks/useDataThemeChange.ts  # 主题切换
└── router/index.ts              # 路由守卫
```

---

## 更新日志 / Changelog

- 2026-03-22: 重写文档，区分已应用/待应用状态
- 2026-03-20: 初始记录，基于 OpenMAIC 和 vue-pure-admin 分析
