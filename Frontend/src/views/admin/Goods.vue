<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center">
          <span>商品管理</span>
          <div>
            <el-button type="primary" @click="openAdd">新增商品</el-button>
            <el-button @click="openBatch">批量导入</el-button>
          </div>
        </div>
      </template>
      <div style="display:flex; gap:12px; margin-bottom:16px">
        <el-input v-model="searchName" placeholder="搜索商品名称" clearable style="width:200px" @keyup.enter="fetchData" />
        <el-select v-model="searchSupplierId" placeholder="选择供应商" clearable filterable style="width:200px" @change="fetchData">
          <el-option v-for="s in supplierList" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-button @click="fetchData">查询</el-button>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="price" label="单价" width="100" />
        <el-table-column prop="supplierName" label="供应商" width="140" />
        <el-table-column prop="description" label="简介" min-width="150" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" style="margin-top:16px; justify-content:flex-end" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="100" placeholder="商品名称" />
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" placeholder="0.00" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="form.supplierId" placeholder="请选择供应商" filterable style="width:100%">
            <el-option v-for="s in supplierList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" maxlength="500" placeholder="商品简介" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" maxlength="500" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchVisible" title="批量导入商品" width="600px">
      <el-input v-model="batchText" type="textarea" :rows="8" placeholder="每行一条，格式：名称,单价,供应商编号,简介,备注&#10;示例：苹果,5.50,1,新鲜红富士,备注" />
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import goodsApi from '../../api/goods'
import supplierApi from '../../api/supplier'

const searchName = ref('')
const searchSupplierId = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const supplierList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const batchVisible = ref(false)
const batchText = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null, name: '', price: 0, supplierId: null, description: '', remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur', type: 'number', min: 0 }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }]
}

function resetForm() {
  form.id = null; form.name = ''; form.price = 0; form.supplierId = null; form.description = ''; form.remark = ''
  formRef.value?.resetFields()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await goodsApi.list({ name: searchName.value, supplierId: searchSupplierId.value, page: page.value, size: size.value })
    tableData.value = res.data || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

async function loadSuppliers() {
  try {
    const res = await supplierApi.list({ page: 1, size: 999 })
    supplierList.value = res.data || []
  } catch {}
}

function openAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.id = row.id; form.name = row.name; form.price = row.price
  form.supplierId = row.supplierId; form.description = row.description || ''
  form.remark = row.remark || ''
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await goodsApi.update(form)
      ElMessage.success('修改成功')
    } else {
      await goodsApi.add(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除商品「${row.name}」？`, '确认删除', { type: 'warning' })
    await goodsApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

function openBatch() { batchText.value = ''; batchVisible.value = true }

async function handleBatchImport() {
  if (!batchText.value.trim()) { ElMessage.warning('请输入导入数据'); return }
  const lines = batchText.value.trim().split('\n').filter(l => l.trim())
  const list = lines.map(line => {
    const parts = line.split(',')
    return { name: parts[0]?.trim() || '', price: Number(parts[1]) || 0, supplierId: Number(parts[2]) || null, description: parts[3]?.trim() || '', remark: parts[4]?.trim() || '' }
  })
  try {
    await goodsApi.batchImport(list)
    ElMessage.success(`成功导入 ${list.length} 条数据`)
    batchVisible.value = false
    fetchData()
  } catch {}
}

onMounted(() => { fetchData(); loadSuppliers() })
</script>
