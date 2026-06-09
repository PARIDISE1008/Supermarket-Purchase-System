<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center">
          <span>会员管理</span>
          <div>
            <el-button type="primary" @click="openAdd">新增会员</el-button>
            <el-button @click="openBatch">批量导入</el-button>
          </div>
        </div>
      </template>
      <div style="display:flex; gap:12px; margin-bottom:16px">
        <el-input v-model="searchName" placeholder="搜索姓名" clearable style="width:200px" @keyup.enter="fetchData" />
        <el-select v-model="searchLevel" placeholder="等级" clearable style="width:140px" @change="fetchData">
          <el-option v-for="lv in [1,2,3,4]" :key="lv" :label="lv + '级'" :value="lv" />
        </el-select>
        <el-button @click="fetchData">查询</el-button>
      </div>
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="150" />
        <el-table-column prop="points" label="积分" width="80" />
        <el-table-column label="等级" width="80">
          <template #default="{ row }">
            <el-tag :type="['','info','warning','','danger'][row.level]">{{ ['','普通','银卡','金卡','钻石'][row.level] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" style="margin-top:16px; justify-content:flex-end" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑会员' : '新增会员'" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" maxlength="50" placeholder="会员姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="11位手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" maxlength="100" placeholder="email@example.com" />
        </el-form-item>
        <el-form-item label="积分" prop="points">
          <el-input-number v-model="form.points" :min="0" style="width:100%" placeholder="0" />
        </el-form-item>
        <el-form-item label="等级" prop="level">
          <el-select v-model="form.level" placeholder="选择等级" style="width:100%">
            <el-option label="1 - 普通会员" :value="1" />
            <el-option label="2 - 银卡会员" :value="2" />
            <el-option label="3 - 金卡会员" :value="3" />
            <el-option label="4 - 钻石会员" :value="4" />
          </el-select>
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

    <el-dialog v-model="batchVisible" title="批量导入会员" width="600px">
      <el-input v-model="batchText" type="textarea" :rows="8" placeholder="每行一条，格式：姓名,电话,邮箱,积分,等级,备注&#10;示例：李四,13800002222,test@t.com,100,2,备注" />
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
import memberApi from '../../api/member'

const searchName = ref('')
const searchLevel = ref(null)
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
  id: null, name: '', phone: '', email: '', points: 0, level: 1, remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入会员姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  points: [{ required: true, message: '请输入积分', trigger: 'blur' }]
}

function resetForm() {
  form.id = null; form.name = ''; form.phone = ''; form.email = ''; form.points = 0; form.level = 1; form.remark = ''
  formRef.value?.resetFields()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await memberApi.list({ name: searchName.value, level: searchLevel.value, page: page.value, size: size.value })
    tableData.value = res.data || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function openAdd() { isEdit.value = false; resetForm(); dialogVisible.value = true }

function openEdit(row) {
  isEdit.value = true
  form.id = row.id; form.name = row.name; form.phone = row.phone
  form.email = row.email || ''; form.points = row.points; form.level = row.level; form.remark = row.remark || ''
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await memberApi.update(form)
      ElMessage.success('修改成功')
    } else {
      await memberApi.add(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除会员「${row.name}」？`, '确认删除', { type: 'warning' })
    await memberApi.delete(row.id)
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
    return { name: parts[0]?.trim() || '', phone: parts[1]?.trim() || '', email: parts[2]?.trim() || '', points: Number(parts[3]) || 0, level: Number(parts[4]) || 1, remark: parts[5]?.trim() || '' }
  })
  try {
    await memberApi.batchImport(list)
    ElMessage.success(`成功导入 ${list.length} 条`)
    batchVisible.value = false
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>
