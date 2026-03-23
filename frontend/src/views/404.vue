<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
</script>

<template>
  <div class="h-screen flex items-center justify-center bg-gray-50">
    <div class="text-center">
      <h1 class="text-9xl font-bold text-pink-500">{{ t('notFound.title') }}</h1>
      <p class="text-xl text-gray-600 mt-4">{{ t('notFound.message') }}</p>
      <el-button type="primary" class="mt-6" @click="router.push('/')">
        {{ t('notFound.backHome') }}
      </el-button>
    </div>
  </div>
</template>
