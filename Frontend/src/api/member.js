import request from './request'

export default {
  add(data) { return request.post('/member', data) },
  batchImport(data) { return request.post('/member/batch', data) },
  list(params) { return request.get('/member', { params }) },
  getById(id) { return request.get(`/member/${id}`) },
  update(data) { return request.put('/member', data) },
  delete(id) { return request.delete(`/member/${id}`) }
}
