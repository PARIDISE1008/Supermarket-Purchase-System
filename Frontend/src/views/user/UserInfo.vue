<template>
  <el-card v-loading="loading">
    <template #header><span>个人信息</span></template>
    <el-descriptions v-if="info" :column="2" border>
      <el-descriptions-item label="编号">{{ info.id }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ info.name }}</el-descriptions-item>
      <el-descriptions-item label="级别">{{ info.level === 2 ? '管理员' : '普通员工' }}</el-descriptions-item>
      <el-descriptions-item label="电话">{{ info.phone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="工资">{{ info.salary || '-' }}</el-descriptions-item>
      <el-descriptions-item label="入职时间">{{ info.createTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">{{ info.remark || '-' }}</el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import userApi from '../../api/user'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const info = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await userApi.getUserInfo(user.id)
    info.value = res.data
  } finally { loading.value = false }
})
</script>
