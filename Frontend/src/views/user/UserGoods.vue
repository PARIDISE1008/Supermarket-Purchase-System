<template>
  <el-card>
    <template #header><span>商品信息查询</span></template>
    <div style="display:flex; gap:12px; margin-bottom:16px">
      <el-input v-model="searchName" placeholder="搜索商品名称" clearable style="width:240px" @keyup.enter="fetchData" />
      <el-button @click="fetchData">查询</el-button>
    </div>
    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="price" label="单价" width="100" />
      <el-table-column prop="supplierName" label="供应商" width="140" />
      <el-table-column prop="description" label="简介" min-width="180" />
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" style="margin-top:16px; justify-content:flex-end" @current-change="fetchData" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import userApi from '../../api/user'

const searchName = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const res = await userApi.queryGoods({ name: searchName.value, page: page.value, size: size.value })
    tableData.value = res.data || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>
