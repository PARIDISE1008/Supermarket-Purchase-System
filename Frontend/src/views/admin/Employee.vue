<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center">
          <span>员工管理</span>
          <div>
            <el-button v-if="activeTab === 'approved'" type="primary" @click="openAdd">新增员工</el-button>
            <el-button v-if="activeTab === 'approved'" @click="openBatch">批量导入</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="已审批" name="approved" />
        <el-tab-pane label="待审批" name="pending" />
      </el-tabs>

      <!-- 已审批员工列表 -->
      <div v-if="activeTab === 'approved'">
        <div style="display:flex; gap:12px; margin-bottom:16px">
          <el-input v-model="searchName" placeholder="搜索姓名" clearable style="width:200px" @keyup.enter="fetchData" />
          <el-select v-model="searchLevel" placeholder="级别" clearable style="width:140px" @change="fetchData">
            <el-option label="普通员工" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
          <el-button @click="fetchData">查询</el-button>
        </div>
        <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
          <el-table-column prop="id" label="编号" width="70" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column label="级别" width="100">
            <template #default="{ row }">{{ row.level === 2 ? '管理员' : '普通员工' }}</template>
          </el-table-column>
          <el-table-column prop="phone" label="电话" width="130" />
          <el-table-column prop="salary" label="工资" width="100" />
          <el-table-column prop="remark" label="备注" min-width="120" />
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" style="margin-top:16px; justify-content:flex-end" @current-change="fetchData" />
      </div>

      <!-- 待审批员工列表 -->
      <div v-if="activeTab === 'pending'">
        <el-table :data="pendingData" border stripe v-loading="pendingLoading" style="width:100%">
          <el-table-column prop="id" label="编号" width="70" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="phone" label="电话" width="140" />
          <el-table-column prop="createTime" label="注册时间" width="170" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="pendingPage" :page-size="size" :total="pendingTotal" layout="total, prev, pager, next" style="margin-top:16px; justify-content:flex-end" @current-change="fetchPending" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑员工' : '新增员工'" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" maxlength="50" placeholder="员工姓名" />
        </el-form-item>
        <el-form-item label="级别" prop="level">
          <el-radio-group v-model="form.level">
            <el-radio :label="1">普通员工</el-radio>
            <el-radio :label="2">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="11位手机号" />
        </el-form-item>
        <el-form-item label="工资" prop="salary">
          <el-input-number v-model="form.salary" :min="0" :precision="2" style="width:100%" placeholder="0.00" />
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

    <el-dialog v-model="batchVisible" title="批量导入员工" width="600px">
      <el-input v-model="batchText" type="textarea" :rows="8" placeholder="每行一条，格式：姓名,级别(1普通/2管理),电话,工资,备注&#10;示例：张三,1,13800001111,5000,备注" />
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
import employeeApi from '../../api/employee'

const activeTab = ref('approved')
const searchName = ref('')
const searchLevel = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

const pendingPage = ref(1)
const pendingTotal = ref(0)
const pendingData = ref([])
const pendingLoading = ref(false)

const dialogVisible = ref(false)
const batchVisible = ref(false)
const batchText = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null, name: '', level: 1, phone: '', salary: null, remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

function resetForm() {
  form.id = null; form.name = ''; form.level = 1; form.phone = ''; form.salary = null; form.remark = ''
  formRef.value?.resetFields()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await employeeApi.list({ name: searchName.value, level: searchLevel.value, page: page.value, size: size.value })
    tableData.value = res.data || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

async function fetchPending() {
  pendingLoading.value = true
  try {
    const res = await employeeApi.getPending({ page: pendingPage.value, size: size.value })
    pendingData.value = res.data || []
    pendingTotal.value = res.total || 0
  } finally { pendingLoading.value = false }
}

function onTabChange(tab) {
  if (tab === 'approved') fetchData()
  else fetchPending()
}

function openAdd() { isEdit.value = false; resetForm(); dialogVisible.value = true }

function openEdit(row) {
  isEdit.value = true
  form.id = row.id; form.name = row.name; form.level = row.level
  form.phone = row.phone || ''; form.salary = row.salary; form.remark = row.remark || ''
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await employeeApi.update(form)
      ElMessage.success('修改成功')
    } else {
      await employeeApi.add(form)
      ElMessage.success('新增成功，默认密码为 123456')
    }
    dialogVisible.value = false
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除员工「${row.name}」？`, '确认删除', { type: 'warning' })
    await employeeApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确定通过「${row.name}」的注册申请？`, '审批通过', { type: 'info' })
    await employeeApi.approve(row.id)
    ElMessage.success('审批通过')
    fetchPending()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(err?.msg || err?.message || '操作失败')
  }
}

async function handleReject(row) {
  try {
    await ElMessageBox.confirm(`确定拒绝「${row.name}」的注册申请？该操作不可恢复`, '拒绝申请', { type: 'warning' })
    await employeeApi.reject(row.id)
    ElMessage.success('已拒绝')
    fetchPending()
  } catch (err) {
    if (err !== 'cancel') ElMessage.error(err?.msg || err?.message || '操作失败')
  }
}

function openBatch() { batchText.value = ''; batchVisible.value = true }

async function handleBatchImport() {
  if (!batchText.value.trim()) { ElMessage.warning('请输入导入数据'); return }
  const lines = batchText.value.trim().split('\n').filter(l => l.trim())
  const list = lines.map(line => {
    const parts = line.split(',')
    return { name: parts[0]?.trim() || '', level: Number(parts[1]) || 1, phone: parts[2]?.trim() || '', salary: Number(parts[3]) || 0, remark: parts[4]?.trim() || '' }
  })
  try {
    await employeeApi.batchImport(list)
    ElMessage.success(`成功导入 ${list.length} 条`)
    batchVisible.value = false
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>
