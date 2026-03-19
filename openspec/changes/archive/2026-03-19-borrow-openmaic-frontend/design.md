## Context

The claw-dash project is a Vue 3 + Element Plus dashboard for monitoring OpenClaw agents. Currently uses basic pink-themed CSS with simple dark mode via media query. The user loves OpenMAIC's modern purple-themed design with smooth animations, glassmorphism, and professional UI. This design document outlines how to bring OpenMAIC's visual patterns to claw-dash.

## Goals / Non-Goals

**Goals:**

1. Adopt purple primary color (#722ed1) matching OpenMAIC's aesthetic
2. Implement CSS variable system for consistent theming
3. Add animations library (glow, shimmer, wave, breathe)
4. Implement glassmorphism with backdrop blur
5. Create proper dark mode with toggle switch
6. Enhance header with theme/language toggles
7. Maintain Vue + Element Plus compatibility

**Non-Goals:**

- Replace Element Plus components (keep them, just customize theme)
- Migrate from Vue to React/Next.js
- Change backend or API
- Add new functionality - purely visual enhancement

## Decisions

### 1. CSS Variable System

**Decision:** Use OpenMAIC's CSS variable pattern with oklch color space
**Rationale:** Modern color management, easy dark mode switching, matches OpenMAIC's design language
**Alternative:** Keep current CSS variables - insufficient for proper theming

### 2. Animation Implementation

**Decision:** Add animation keyframes and utility classes inspired by OpenMAIC
**Components:**

- `animate-glow` - Setup glow effect
- `animate-shimmer` - Loading skeleton sweep
- `animate-wave` - Audio visualizer
- `animate-breathe` - Subtle border breathing
  **Rationale:** These animations add polish without significant overhead

### 3. Dark Mode

**Decision:** Implement theme store with localStorage persistence and toggle
**Rationale:** OpenMAIC uses proper dark/light/system toggle, not just media query
**Alternative:** Keep media query - insufficient control, user can't override

### 4. Glassmorphism

**Decision:** Add backdrop-blur and translucent background utilities
**Rationale:** Modern UI trend used in OpenMAIC's floating headers

### 5. Element Plus Integration

**Decision:** Override Element Plus CSS variables via `--el-*` custom properties
**Rationale:** Maintain component functionality while matching design

## Risks / Trade-offs

[Risk] Element Plus version compatibility → Mitigation: Test with current version, use CSS variable overrides
[Risk] Animation performance → Mitigation: Use CSS transforms, avoid JS animations
[Risk] Theme persistence → Mitigation: Use localStorage with system preference fallback

## Migration Plan

1. Update `src/style.css` with new CSS variables and animations
2. Create `src/composables/useTheme.ts` for theme management
3. Update `src/App.vue` to provide theme context
4. Update `src/layout/index.vue` with floating header
5. Update Element Plus theme variables
6. Test in both light and dark modes

## Open Questions

- Should we keep the "女儿国" (Women's Kingdom) theme identity or fully adopt OpenMAIC's purple?
- Should icons be updated from Element Plus icons to Lucide (used in OpenMAIC)?
