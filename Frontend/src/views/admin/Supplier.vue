<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center">
          <span>供应商管理</span>
          <div>
            <el-button type="primary" @click="openAdd">新增供应商</el-button>
            <el-button @click="openBatch">批量导入</el-button>
          </div>
        </div>
      </template>
      <el-input v-model="searchName" placeholder="搜索供应商名称" clearable style="width:240px; margin-bottom:16px" @keyup.enter="fetchData" />
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="shortName" label="简称" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系人电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="150" />
        <el-table-column prop="address" label="地址" min-width="150" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" style="margin-top:16px; justify-content:flex-end" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑供应商' : '新增供应商'" width="560px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="100" placeholder="供应商名称" />
        </el-form-item>
        <el-form-item label="简称" prop="shortName">
          <el-input v-model="form.shortName" maxlength="50" placeholder="简称" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" maxlength="20" placeholder="如 010-12345678" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" maxlength="200" placeholder="地址" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" maxlength="100" placeholder="supplier@example.com" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" maxlength="50" placeholder="联系人姓名" />
        </el-form-item>
        <el-form-item label="联系人电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" maxlength="20" placeholder="如 13800001111" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" maxlength="500" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchVisible" title="批量导入供应商" width="600px">
      <el-input v-model="batchText" type="textarea" :rows="8" placeholder="每行一条，格式：名称,简称,地址,电话,邮箱,联系人,联系人电话,备注&#10;示例：华为科技,华为,深圳,010-123456,test@t.com,张三,1380001,备注" />
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
import supplierApi from '../../api/supplier'

const searchName = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const batchVisible = ref(false)
const batchText = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null, name: '', shortName: '', address: '', phone: '',
  email: '', contactPerson: '', contactPhone: '', remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' },
    { pattern: /^[0-9\-]{7,20}$/, message: '电话格式不正确', trigger: 'blur' }
  ]
}

function resetForm() {
  Object.keys(form).forEach(k => form[k] = k === 'id' ? null : '')
  formRef.value?.resetFields()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await supplierApi.list({ name: searchName.value, page: page.value, size: size.value })
    tableData.value = res.data || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function openAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id, name: row.name, shortName: row.shortName || '',
    address: row.address || '', phone: row.phone,
    email: row.email || '', contactPerson: row.contactPerson || '',
    contactPhone: row.contactPhone || '', remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await supplierApi.update(form)
      ElMessage.success('修改成功')
    } else {
      await supplierApi.add(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除供应商「${row.name}」？`, '确认删除', { type: 'warning' })
    await supplierApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

function openBatch() {
  batchText.value = ''
  batchVisible.value = true
}

async function handleBatchImport() {
  if (!batchText.value.trim()) {
    ElMessage.warning('请输入导入数据')
    return
  }
  const lines = batchText.value.trim().split('\n').filter(l => l.trim())
  const list = lines.map(line => {
    const parts = line.split(',')
    return {
      name: parts[0]?.trim() || '',
      shortName: parts[1]?.trim() || '',
      address: parts[2]?.trim() || '',
      phone: parts[3]?.trim() || '',
      email: parts[4]?.trim() || '',
      contactPerson: parts[5]?.trim() || '',
      contactPhone: parts[6]?.trim() || '',
      remark: parts[7]?.trim() || ''
    }
  })
  try {
    await supplierApi.batchImport(list)
    ElMessage.success(`成功导入 ${list.length} 条数据`)
    batchVisible.value = false
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>
