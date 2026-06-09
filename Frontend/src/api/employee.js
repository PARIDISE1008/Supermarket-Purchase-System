import request from './request'

export default {
  add(data) { return request.post('/employee', data) },
  batchImport(data) { return request.post('/employee/batch', data) },
  list(params) { return request.get('/employee', { params }) },
  getById(id) { return request.get(`/employee/${id}`) },
  update(data) { return request.put('/employee', data) },
  delete(id) { return request.delete(`/employee/${id}`) },
  login(data) { return request.post('/employee/login', data) },
  register(data) { return request.post('/employee/register', data) },
  getPending(params) { return request.get('/employee/pending', { params }) },
  approve(id) { return request.put(`/employee/approve/${id}`) },
  reject(id) { return request.put(`/employee/reject/${id}`) }
}
