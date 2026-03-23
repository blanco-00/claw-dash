import { ref, watch } from 'vue'

type Theme = 'light' | 'dark' | 'system'

const THEME_KEY = 'clawdash-theme'

const theme = ref<Theme>('system')
const isDark = ref(false)

function getSystemTheme(): 'light' | 'dark' {
  if (typeof window !== 'undefined') {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }
  return 'light'
}

function applyTheme(dark: boolean) {
  isDark.value = dark
  if (dark) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

function updateTheme() {
  if (theme.value === 'system') {
    applyTheme(getSystemTheme() === 'dark')
  } else {
    applyTheme(theme.value === 'dark')
  }
}

// Initialize theme immediately on module load - before any component renders
function initTheme() {
  const stored = localStorage.getItem(THEME_KEY) as Theme | null
  if (stored && ['light', 'dark', 'system'].includes(stored)) {
    theme.value = stored
  }
  updateTheme()

  // Listen for system theme changes
  if (typeof window !== 'undefined') {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', () => {
      if (theme.value === 'system') {
        updateTheme()
      }
    })
  }
}

// Apply initial theme BEFORE any components render
initTheme()

export function useTheme() {
  watch(theme, () => {
    localStorage.setItem(THEME_KEY, theme.value)
    updateTheme()
  })

  const setTheme = (newTheme: Theme) => {
    theme.value = newTheme
  }

  const toggleTheme = () => {
    if (theme.value === 'light') {
      theme.value = 'dark'
    } else if (theme.value === 'dark') {
      theme.value = 'system'
    } else {
      theme.value = 'light'
    }
  }

  return {
    theme,
    isDark,
    setTheme,
    toggleTheme
  }
}