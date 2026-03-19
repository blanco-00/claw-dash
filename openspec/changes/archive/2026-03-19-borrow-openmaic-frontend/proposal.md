## Why

The current claw-dash frontend uses basic CSS with Element Plus components. The user loves OpenMAIC's modern, polished frontend design featuring purple theme, smooth animations, glassmorphism effects, and professional UI components. We should borrow these frontend patterns to elevate claw-dash's visual experience while maintaining the Vue + Element Plus tech stack.

## What Changes

1. **CSS Variables Overhaul**: Adopt OpenMAIC's CSS variable system with purple primary color (#722ed1), proper dark mode tokens, and consistent theming
2. **Animation System**: Add OpenMAIC-inspired animations (glow effects, shimmer loading, wave visualizer, breathe transitions)
3. **Glassmorphism**: Implement backdrop blur and translucent backgrounds for cards and headers
4. **Enhanced Dark Mode**: Replace basic media query with proper dark mode toggle and CSS variable system
5. **Layout Improvements**: Add floating header with language/theme toggles, improved sidebar styling
6. **Component Enhancements**: Style improvements for cards, buttons, inputs to match modern design

## Capabilities

### New Capabilities

- **css-variables-overhaul**: Complete CSS variable system inspired by OpenMAIC's Tailwind theme
- **animation-system**: Animations library including glow, shimmer, wave, breathe effects
- **glassmorphism**: Backdrop blur and translucent design patterns
- **enhanced-dark-mode**: Proper dark mode with toggle switch and persistent theme
- **modern-layout**: Floating header with theme/language toggles and improved styling

### Modified Capabilities

- None - this is purely a frontend visual enhancement

## Impact

- `src/style.css` - Complete rewrite with CSS variables and animations
- `src/layout/index.vue` - Enhanced header with toggles
- `src/App.vue` - Theme provider setup
- `src/components/` - New styled components as needed
- No backend changes required
- Element Plus theme customization for consistency
