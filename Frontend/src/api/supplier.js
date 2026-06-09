import request from './request'

export default {
  add(data) { return request.post('/supplier', data) },
  batchImport(data) { return request.post('/supplier/batch', data) },
  list(params) { return request.get('/supplier', { params }) },
  getById(id) { return request.get(`/supplier/${id}`) },
  update(data) { return request.put('/supplier', data) },
  delete(id) { return request.delete(`/supplier/${id}`) }
}
