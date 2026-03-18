<script setup lang="ts">
defineProps<{
  title: string
  value: number | string
  icon: string
  color?: string
  loading?: boolean
}>()
</script>

<template>
  <el-card shadow="hover">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-bold">{{ title }}</span>
        <span class="text-2xl">{{ icon }}</span>
      </div>
    </template>
    
    <div v-if="loading" class="h-10 flex items-center justify-center">
      <el-skeleton :rows="1" animated />
    </div>
    
    <div v-else class="text-center py-2">
      <div 
        class="text-3xl font-bold"
        :class="{
          'text-pink-500': color === 'pink',
          'text-blue-500': color === 'blue',
          'text-green-500': color === 'green',
          'text-orange-500': color === 'orange',
          'text-purple-500': color === 'purple'
        }"
      >
        {{ value }}
      </div>
    </div>
  </el-card>
</template>
