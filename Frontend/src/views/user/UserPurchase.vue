<template>
  <el-card>
    <template #header><span>历史采购订单</span></template>
    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
      <el-table-column prop="orderNo" label="采购单号" width="200" />
      <el-table-column prop="employeeName" label="采购员" width="100" />
      <el-table-column prop="totalQuantity" label="总数量" width="90" />
      <el-table-column prop="totalPrice" label="总金额" width="110" />
      <el-table-column prop="purchaseTime" label="采购时间" width="170" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="{ DRAFT: 'warning', HISTORY: 'success', CANCELLED: 'info' }[row.status] || ''">
            {{ { DRAFT: '草稿', HISTORY: '已完成', CANCELLED: '已作废' }[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" style="margin-top:16px; justify-content:flex-end" @current-change="fetchData" />

    <el-dialog v-model="detailVisible" title="采购单详情" width="700px">
      <el-descriptions v-if="currentMain" :column="2" border>
        <el-descriptions-item label="采购单号">{{ currentMain.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ { DRAFT: '草稿', HISTORY: '已完成', CANCELLED: '已作废' }[currentMain.status] }}</el-descriptions-item>
        <el-descriptions-item label="采购员">{{ currentMain.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ currentMain.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="总金额">{{ currentMain.totalPrice }}</el-descriptions-item>
        <el-descriptions-item label="采购时间">{{ currentMain.purchaseTime }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ currentMain.deadlineTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentMain.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <h4 style="margin-top:16px">采购明细</h4>
      <el-table :data="detailList" border size="small" style="width:100%">
        <el-table-column prop="goodsName" label="商品" min-width="120" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="unitPrice" label="单价" width="90" />
        <el-table-column prop="totalPrice" label="金额" width="100" />
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import userApi from '../../api/user'

const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentMain = ref(null)
const detailList = ref([])

async function fetchData() {
  loading.value = true
  try {
    const res = await userApi.queryPurchase({ page: page.value, size: size.value })
    tableData.value = res.data || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

async function showDetail(row) {
  try {
    const res1 = await userApi.getPurchaseDetail(row.id)
    currentMain.value = res1.data
    const res2 = await purchaseApi.getDetails(row.id)
    detailList.value = res2.data || []
    detailVisible.value = true
  } catch {}
}

import purchaseApi from '../../api/purchase'
onMounted(fetchData)
</script>
