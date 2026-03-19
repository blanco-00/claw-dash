## 1. CSS Variables System

- [x] 1.1 Rewrite `src/style.css` with OpenMAIC-inspired CSS variables (purple #722ed1 primary)
- [x] 1.2 Add light mode color tokens (background, foreground, primary, secondary, muted, border, destructive)
- [x] 1.3 Add dark mode color tokens with `.dark` class support
- [x] 1.4 Add Element Plus theme overrides (--el-color-primary, --el-color-primary-light-3)
- [x] 1.5 Add radius and spacing tokens

## 2. Animation System

- [x] 2.1 Add `animate-setup-glow` keyframes (pulsing purple glow)
- [x] 2.2 Add `animate-shimmer` keyframes (loading skeleton sweep)
- [x] 2.3 Add `animate-wave` keyframes (audio visualizer)
- [x] 2.4 Add `animate-setup-border` keyframes (breathe border effect)
- [x] 2.5 Add fade transition utilities for Vue router
- [x] 2.6 Add scrollbar styling utilities

## 3. Glassmorphism

- [x] 3.1 Add backdrop blur utility classes
- [x] 3.2 Add translucent background utilities (white 60%, dark 60%)
- [x] 3.3 Add semi-transparent border utilities
- [x] 3.4 Add shadow utilities

## 4. Theme Management

- [x] 4.1 Create `src/composables/useTheme.ts` with theme store
- [x] 4.2 Implement localStorage persistence for theme preference
- [x] 4.3 Add system preference detection (prefers-color-scheme)
- [x] 4.4 Add real-time theme switching without reload
- [x] 4.5 Update `src/App.vue` to provide theme context

## 5. Layout Enhancement

- [x] 5.1 Update `src/layout/index.vue` with floating header
- [x] 5.2 Add theme toggle dropdown component
- [x] 5.3 Add language selector (CN/EN) component
- [x] 5.4 Add settings button with hover rotation animation
- [x] 5.5 Apply glassmorphism to header and toolbars

## 6. Component Styling

- [x] 6.1 Customize Element Plus button styles (purple primary)
- [x] 6.2 Customize Element Plus card styles (rounded, shadow)
- [x] 6.3 Customize Element Plus input focus states
- [x] 6.4 Add hover transitions to interactive elements
- [x] 6.5 Style scrollbars to match theme

## 7. Verification

- [x] 7.1 Test light mode rendering
- [x] 7.2 Test dark mode rendering and toggle
- [x] 7.3 Verify animations play smoothly
- [x] 7.4 Verify glassmorphism effects on supported browsers
- [x] 7.5 Check Element Plus components match new theme
