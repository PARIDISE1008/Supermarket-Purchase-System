<template>
  <el-container style="height:100vh">
    <el-aside width="200px" style="background:#304156">
      <div class="logo">超市管理系统</div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/user/info">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
        <el-menu-item index="/user/goods">
          <el-icon><Goods /></el-icon>
          <span>商品信息</span>
        </el-menu-item>
        <el-menu-item index="/user/purchase">
          <el-icon><ShoppingCart /></el-icon>
          <span>采购信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="border-bottom:1px solid #dcdfe6; display:flex; align-items:center; justify-content:space-between">
        <span style="font-size:16px; font-weight:bold">员工：{{ user.name }}</span>
        <el-button text @click="logout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Goods, ShoppingCart } from '@element-plus/icons-vue'

const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('user') || '{}'))

function logout() {
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.el-menu { border-right: none; }
</style>
