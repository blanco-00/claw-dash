<script setup lang="ts">
import { useTheme } from './composables/useTheme'

// Theme is initialized at module load time in useTheme.ts
// This import ensures the theme is applied before first render
useTheme()
</script>

<template>
  <router-view />
</template>