import request from './request'

export default {
  getUserInfo(employeeId) { return request.get('/user/info', { params: { employeeId } }) },
  queryGoods(params) { return request.get('/user/goods', { params }) },
  getGoodsDetail(id) { return request.get(`/user/goods/${id}`) },
  queryPurchase(params) { return request.get('/user/purchase', { params }) },
  getPurchaseDetail(id) { return request.get(`/user/purchase/${id}`) }
}
