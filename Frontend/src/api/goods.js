import request from './request'

export default {
  add(data) { return request.post('/goods', data) },
  batchImport(data) { return request.post('/goods/batch', data) },
  list(params) { return request.get('/goods', { params }) },
  getById(id) { return request.get(`/goods/${id}`) },
  update(data) { return request.put('/goods', data) },
  delete(id) { return request.delete(`/goods/${id}`) }
}
