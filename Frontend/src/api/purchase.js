import request from './request'

export default {
  generateOrderNo() { return request.get('/purchase/order-no') },
  submit(data) { return request.post('/purchase/submit', data) },
  listMyOrders(params) { return request.get('/purchase/my', { params }) },
  getDetail(id) { return request.get(`/purchase/${id}`) },
  getDetails(id) { return request.get(`/purchase/${id}/details`) },
  cancel(id, employeeId) { return request.put(`/purchase/cancel/${id}`, null, { params: { employeeId } }) },
  restore(id, employeeId) { return request.put(`/purchase/restore/${id}`, null, { params: { employeeId } }) },
  verify(id, adminId) { return request.put(`/purchase/verify/${id}`, null, { params: { adminId } }) }
}
