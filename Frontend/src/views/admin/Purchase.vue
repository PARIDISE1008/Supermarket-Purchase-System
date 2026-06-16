<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center">
          <span>采购管理</span>
          <el-button type="primary" :disabled="cooling" @click="openCreate">
            {{ cooling ? `创建采购单 (${coolSeconds}秒)` : '创建采购单' }}
          </el-button>
        </div>
      </template>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="待处理" name="DRAFT" />
        <el-tab-pane label="已完成" name="HISTORY" />
        <el-tab-pane label="已删除" name="CANCELLED" />
      </el-tabs>
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="orderNo" label="采购单号" width="200" />
        <el-table-column prop="employeeName" label="采购员" width="100" />
        <el-table-column prop="totalQuantity" label="总数量" width="90" />
        <el-table-column prop="totalPrice" label="总金额" width="110" />
        <el-table-column prop="purchaseTime" label="采购时间" width="170" />
        <el-table-column prop="deadlineTime" label="截止时间" width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'DRAFT' && user.level === 2" type="success" size="small" @click="handleVerify(row)">核实</el-button>
            <el-button v-if="row.status === 'DRAFT'" type="warning" size="small" @click="handleCancel(row)">作废</el-button>
            <el-button v-if="row.status === 'CANCELLED'" type="success" size="small" @click="handleRestore(row)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" style="margin-top:16px; justify-content:flex-end" @current-change="fetchData" />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="采购单详情" width="700px">
      <el-descriptions v-if="currentMain" :column="2" border>
        <el-descriptions-item label="采购单号">{{ currentMain.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(currentMain.status) }}</el-descriptions-item>
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

    <!-- 创建采购单弹窗 -->
    <el-dialog v-model="createVisible" title="创建采购单" width="750px" @opened="initCreate" @closed="cleanupCreate">
      <!-- 订单号 -->
      <el-form label-width="100px">
        <el-form-item label="采购单号">
          <el-input v-model="orderNo" disabled style="width: 260px">
            <template #append><el-button @click="regenerateOrderNo" :disabled="orderNoGenerating">重新生成</el-button></template>
          </el-input>
        </el-form-item>
      </el-form>
      <!-- 明细表格 -->
      <div style="margin:12px 0; display:flex; gap:8px">
        <el-select v-model="selectedGoodsId" placeholder="搜索并选择商品" filterable remote :remote-method="searchGoodsRemote" :loading="searchGoodsLoading" clearable style="width:240px" @change="addGoodsItem">
          <el-option v-for="g in searchGoodsResult" :key="g.id" :label="g.name + ' (¥' + g.price + ')'" :value="g.id" />
        </el-select>
      </div>
      <el-table :data="purchaseItems" :key="purchaseItems.length" row-key="goodsId" border size="small" style="width:100%">
        <el-table-column prop="goodsName" label="商品" min-width="120" />
        <el-table-column prop="unitPrice" label="单价" width="90" />
        <el-table-column label="数量" width="160">
          <template #default="scope">
            <el-input-number v-model="scope.row.quantity" :min="1" size="small" @change="debouncedRecalc" />
          </template>
        </el-table-column>
        <el-table-column label="折扣(%)" width="100">
          <template #default="scope">
            <el-input-number v-model="scope.row.discount" :min="0" :max="100" size="small" @change="debouncedRecalc" />
          </template>
        </el-table-column>
        <el-table-column prop="subTotal" label="小计" width="100" />
        <el-table-column label="" width="70">
          <template #default="scope">
            <el-button type="danger" size="small" text @click="removeItem(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:12px; text-align:right">
        <span style="margin-right:20px">总数量：<b>{{ totalQuantity }}</b></span>
        <span style="margin-right:20px">小计：<b>¥{{ subtotalAmount }}</b></span>
        <span style="margin-right:20px">优惠：<b style="color:#67C23A">-¥{{ discountAmount }}</b></span>
        <span>实付：<b style="color:#f56c6c">¥{{ totalAmount }}</b></span>
      </div>

      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :disabled="submitting || purchaseItems.length === 0" :loading="submitting" @click="confirmSubmit">提交采购</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import purchaseApi from '../../api/purchase'
import goodsApi from '../../api/goods'

const user = JSON.parse(localStorage.getItem('user') || '{}')

const activeTab = ref('DRAFT')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

const detailVisible = ref(false)
const currentMain = ref(null)
const detailList = ref([])

const createVisible = ref(false)
const orderNo = ref('')
const preOrderNo = ref('')
const selectedGoodsId = ref(null)
const searchGoodsResult = ref([])
const searchGoodsLoading = ref(false)
const purchaseItems = ref([])
const cooling = ref(false)
const coolSeconds = ref(5)
const coolTimer = ref(null)
const submitting = ref(false)
const orderNoGenerating = ref(false)

const totalQuantity = computed(() => purchaseItems.value.reduce((s, i) => s + i.quantity, 0))
const totalAmount = computed(() => purchaseItems.value.reduce((s, i) => s + Number(calcItem(i)), 0).toFixed(2))
const subtotalAmount = computed(() => purchaseItems.value.reduce((s, i) => s + (Number(i.quantity) || 0) * (Number(i.unitPrice) || 0), 0).toFixed(2))
const discountAmount = computed(() => (Number(subtotalAmount.value) - Number(totalAmount.value)).toFixed(2))

function statusTag(s) {
  return { DRAFT: 'warning', HISTORY: 'success', CANCELLED: 'info' }[s] || ''
}
function statusLabel(s) {
  return { DRAFT: '草稿', HISTORY: '已完成', CANCELLED: '已作废' }[s] || s
}

async function fetchData() {
  loading.value = true
  try {
    const params = { status: activeTab.value, page: page.value, size: size.value }
    if (activeTab.value !== 'HISTORY') params.employeeId = user.id
    const res = await purchaseApi.listMyOrders(params)
    tableData.value = res.data || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function onTabChange() { page.value = 1; fetchData() }

async function showDetail(row) {
  try {
    const res1 = await purchaseApi.getDetail(row.id)
    currentMain.value = res1.data
    const res2 = await purchaseApi.getDetails(row.id)
    detailList.value = res2.data || []
    detailVisible.value = true
  } catch {}
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(`确定作废采购单「${row.orderNo}」？`, '确认作废', { type: 'warning' })
    await purchaseApi.cancel(row.id, user.id)
    ElMessage.success('已作废')
    fetchData()
  } catch {}
}

async function handleRestore(row) {
  try {
    await ElMessageBox.confirm(`确定恢复采购单「${row.orderNo}」？`, '确认恢复', { type: 'info' })
    await purchaseApi.restore(row.id, user.id)
    ElMessage.success('已恢复')
    fetchData()
  } catch {}
}

async function handleVerify(row) {
  try {
    await ElMessageBox.confirm(`确定核实采购单「${row.orderNo}」？核实后将提交订单`, '确认核实', { type: 'info' })
    await purchaseApi.verify(row.id, user.id)
    ElMessage.success('核实成功')
    fetchData()
  } catch {}
}

async function initCreate() {
  try {
    const res = await purchaseApi.generateOrderNo()
    preOrderNo.value = res.data
    orderNo.value = res.data
  } catch {
    ElMessage.error('获取订单号失败')
    createVisible.value = false
  }
}

async function regenerateOrderNo() {
  orderNoGenerating.value = true
  try {
    const res = await purchaseApi.generateOrderNo()
    preOrderNo.value = res.data
    orderNo.value = res.data
  } finally { orderNoGenerating.value = false }
}

async function searchGoodsRemote(query) {
  if (!query || query.length < 1) { searchGoodsResult.value = []; return }
  searchGoodsLoading.value = true
  try {
    const res = await goodsApi.list({ name: query, page: 1, size: 20 })
    searchGoodsResult.value = res.data || []
  } catch (err) {
    ElMessage.error(err?.msg || err?.message || '搜索商品失败')
  } finally { searchGoodsLoading.value = false }
}

async function addGoodsItem(goodsId) {
  const id = Number(goodsId)
  let goods = searchGoodsResult.value.find(g => g.id === id)
  if (!goods) {
    try {
      const res = await goodsApi.getById(id)
      goods = res.data
    } catch {}
  }
  if (!goods) return
  const exist = purchaseItems.value.find(i => i.goodsId === id)
  if (exist) {
    exist.quantity++
    debouncedRecalc()
    return
  }
  purchaseItems.value.push({
    goodsId: goods.id,
    goodsName: goods.name,
    unitPrice: goods.price,
    quantity: 1,
    discount: 0,
    subTotal: goods.price
  })
  selectedGoodsId.value = null
}

function calcItem(item) {
  const qty = Number(item.quantity) || 0
  const price = Number(item.unitPrice) || 0
  const discount = Math.min(100, Math.max(0, Number(item.discount) || 0))
  return (qty * price * (1 - discount / 100)).toFixed(2)
}

let recalcTimer = null
function debouncedRecalc() {
  clearTimeout(recalcTimer)
  recalcTimer = setTimeout(() => {
    purchaseItems.value.forEach(i => { i.subTotal = calcItem(i) })
  }, 200)
}

function removeItem(index) {
  purchaseItems.value.splice(index, 1)
}

function openCreate() {
  if (cooling.value) return
  createVisible.value = true
}

function confirmSubmit() {
  if (purchaseItems.value.length === 0) {
    ElMessage.warning('请添加采购商品')
    return
  }
  ElMessageBox.confirm(
    `确认提交采购单？共 ${totalQuantity.value} 件商品，总金额 ¥${totalAmount.value}`,
    '确认提交',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
  ).then(() => {
    doSubmit()
  }).catch(() => {})
}

function startMainCooldown() {
  clearInterval(coolTimer.value)
  cooling.value = true
  coolSeconds.value = 5
  coolTimer.value = setInterval(() => {
    coolSeconds.value--
    if (coolSeconds.value <= 0) {
      clearInterval(coolTimer.value)
      cooling.value = false
    }
  }, 1000)
}

async function doSubmit() {
  submitting.value = true
  try {
    await purchaseApi.submit({
      preOrderNo: preOrderNo.value,
      employeeId: user.id,
      details: purchaseItems.value.map(i => ({ goodsId: i.goodsId, quantity: i.quantity })),
      remark: ''
    })
    ElMessage.success('采购单已生成')
    createVisible.value = false
    activeTab.value = 'DRAFT'
    fetchData()
  } catch (err) {
    ElMessage.error(err?.msg || err?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function cleanupCreate() {
  clearTimeout(recalcTimer)
  purchaseItems.value = []
  startMainCooldown()
}

onMounted(fetchData)

onUnmounted(() => {
  clearTimeout(recalcTimer)
  clearInterval(coolTimer.value)
})
</script>
