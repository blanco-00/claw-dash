<script setup lang="ts">
import { onMounted } from 'vue'
import { useTheme } from './composables/useTheme'

const { theme } = useTheme()

onMounted(() => {
  console.log('Theme:', theme.value)
})
</script>

<template>
  <router-view />
</template>
