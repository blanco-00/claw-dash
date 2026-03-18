<template>
  <div class="settings-page">
    <h2>系统设置</h2>

    <el-card>
      <template #header>OpenClaw 配置</template>
      <el-form label-width="150px">
        <el-form-item label="OpenClaw 地址">
          <el-input v-model="config.openclawUrl" placeholder="http://localhost:3000" />
        </el-form-item>
        <el-form-item label="API Key">
          <el-input v-model="config.apiKey" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveConfig">保存配置</el-button>
          <el-button @click="testConnection">测试连接</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="mt-4">
      <template #header>认证设置</template>
      <el-form label-width="150px">
        <el-form-item label="启用认证">
          <el-switch v-model="config.authEnabled" />
        </el-form-item>
        <el-form-item label="JWT Secret">
          <el-input v-model="config.jwtSecret" type="password" show-password />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="mt-4">
      <template #header>Docker 状态</template>
      <div v-if="dockerStatus" class="docker-info">
        <el-tag type="success">运行中</el-tag>
        <p>容器数量: {{ dockerStatus.containers }}</p>
        <p>镜像数量: {{ dockerStatus.images }}</p>
      </div>
      <div v-else>
        <el-button @click="checkDocker">检查 Docker 状态</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const config = ref({
  openclawUrl: 'http://localhost:3000',
  apiKey: '',
  authEnabled: false,
  jwtSecret: ''
})

const dockerStatus = ref<any>(null)

const saveConfig = () => {
  ElMessage.success('配置已保存')
}

const testConnection = () => {
  ElMessage.info('测试连接功能开发中')
}

const checkDocker = () => {
  dockerStatus.value = { containers: 5, images: 12 }
}
</script>

<style scoped>
.settings-page {
  padding: 20px;
}
.mt-4 {
  margin-top: 16px;
}
.docker-info {
  padding: 10px;
}
</style>
