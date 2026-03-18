<template>
  <div class="finance-page">
    <div class="header">
      <h2>财务管理</h2>
      <el-button type="primary" @click="showDialog = true">添加记录</el-button>
    </div>

    <el-row :gutter="20" class="summary-row">
      <el-col :span="8">
        <el-card>
          <div class="summary-item">
            <span>收入</span>
            <span class="income">¥{{ summary.income || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div class="summary-item">
            <span>支出</span>
            <span class="expense">¥{{ summary.expense || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div class="summary-item">
            <span>结余</span>
            <span :class="summary.balance >= 0 ? 'income' : 'expense'"
              >¥{{ summary.balance || 0 }}</span
            >
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table :data="records" style="width: 100%">
      <el-table-column prop="recordId" label="记录ID" width="180" />
      <el-table-column prop="type" label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.type === 'INCOME' ? 'success' : 'danger'">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="120">
        <template #default="{ row }"> ¥{{ row.amount }} </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="recordDate" label="日期" width="120" />
    </el-table>

    <el-dialog v-model="showDialog" title="添加记录" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="类型">
          <el-select v-model="form.type">
            <el-option label="收入" value="INCOME" />
            <el-option label="支出" value="EXPENSE" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.amount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRecord">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const records = ref([])
const summary = ref({ income: 0, expense: 0, balance: 0 })
const showDialog = ref(false)
const form = ref({
  type: 'EXPENSE',
  amount: 0,
  category: '',
  description: '',
  recordDate: new Date().toISOString().split('T')[0]
})

const loadData = async () => {
  try {
    const now = new Date()
    const year = now.getFullYear()
    const month = now.getMonth() + 1

    const [recordsRes, summaryRes] = await Promise.all([
      request.get('/finance/records', { params: { year, month } }),
      request.get('/finance/summary', { params: { year, month } })
    ])

    records.value = recordsRes.data
    summary.value = summaryRes.data
  } catch (error) {
    console.error('Failed to load data:', error)
  }
}

const submitRecord = async () => {
  try {
    await request.post('/finance/record', {
      ...form.value,
      amount: form.value.amount.toString()
    })
    ElMessage.success('添加成功')
    showDialog.value = false
    loadData()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.finance-page {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.summary-row {
  margin-bottom: 20px;
}
.summary-item {
  display: flex;
  justify-content: space-between;
  font-size: 18px;
}
.income {
  color: #67c23a;
}
.expense {
  color: #f56c6c;
}
</style>
