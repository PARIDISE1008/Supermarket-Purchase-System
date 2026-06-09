import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/admin',
    component: () => import('../views/admin/Layout.vue'),
    meta: { requiresAuth: true, role: 2 },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'supplier', name: 'Supplier', component: () => import('../views/admin/Supplier.vue') },
      { path: 'goods', name: 'Goods', component: () => import('../views/admin/Goods.vue') },
      { path: 'employee', name: 'EmployeeMgmt', component: () => import('../views/admin/Employee.vue') },
      { path: 'member', name: 'MemberMgmt', component: () => import('../views/admin/Member.vue') },
      { path: 'purchase', name: 'Purchase', component: () => import('../views/admin/Purchase.vue') }
    ]
  },
  {
    path: '/user',
    component: () => import('../views/user/Layout.vue'),
    meta: { requiresAuth: true, role: 1 },
    children: [
      { path: 'info', name: 'UserInfo', component: () => import('../views/user/UserInfo.vue') },
      { path: 'goods', name: 'UserGoods', component: () => import('../views/user/UserGoods.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (to.meta.requiresAuth) {
    if (!user) {
      ElMessage.warning('请先登录')
      return next('/login')
    }
    if (to.meta.role && user.level !== to.meta.role && to.meta.role === 2 && user.level !== 2) {
      ElMessage.warning('无权限访问')
      return next(false)
    }
  }
  next()
})

export default router
