<template>
  <div>
    <h3>欢迎使用超市进销存管理系统</h3>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card" style="background:#409EFF;color:#fff" @click="router.push('/admin/supplier')">
          <div class="stat-title">供应商总数</div>
          <div class="stat-num">{{ stats.supplier }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card" style="background:#67C23A;color:#fff" @click="router.push('/admin/goods')">
          <div class="stat-title">商品总数</div>
          <div class="stat-num">{{ stats.goods }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card" style="background:#E6A23C;color:#fff" @click="router.push('/admin/employee')">
          <div class="stat-title">员工总数</div>
          <div class="stat-num">{{ stats.employee }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card" style="background:#F56C6C;color:#fff" @click="router.push('/admin/member')">
          <div class="stat-title">会员总数</div>
          <div class="stat-num">{{ stats.member }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card" style="background:#909399;color:#fff" @click="router.push('/admin/purchase')">
          <div class="stat-title">采购管理</div>
          <div class="stat-num">{{ stats.purchase }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import supplierApi from '../../api/supplier'
import goodsApi from '../../api/goods'
import employeeApi from '../../api/employee'
import memberApi from '../../api/member'
import purchaseApi from '../../api/purchase'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const stats = reactive({ supplier: 0, goods: 0, employee: 0, member: 0, purchase: 0 })

onMounted(async () => {
  const results = await Promise.allSettled([
    supplierApi.list({ page: 1, size: 1 }),
    goodsApi.list({ page: 1, size: 1 }),
    employeeApi.list({ page: 1, size: 1 }),
    memberApi.list({ page: 1, size: 1 }),
    purchaseApi.listMyOrders({ employeeId: user.id, status: 'DRAFT', page: 1, size: 1 })
  ])
  if (results[0].status === 'fulfilled') stats.supplier = results[0].value.total
  if (results[1].status === 'fulfilled') stats.goods = results[1].value.total
  if (results[2].status === 'fulfilled') stats.employee = results[2].value.total
  if (results[3].status === 'fulfilled') stats.member = results[3].value.total
  if (results[4].status === 'fulfilled') stats.purchase = results[4].value.total
})
</script>

<style scoped>
.stat-card { text-align: center; border: none; cursor: pointer; transition: transform 0.2s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-title { font-size: 14px; margin-bottom: 10px; }
.stat-num { font-size: 32px; font-weight: bold; }
</style>
