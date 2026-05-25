import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
}); 

export const sessionApi = {
  getAll: () => api.get('/sessions'),
  get: (id) => api.get(`/sessions/${id}`),
  create: (data) => api.post('/sessions', data),
  start: (id) => api.post(`/sessions/${id}/start`),
  end: (id) => api.post(`/sessions/${id}/end`),
  getDashboard: (id) => api.get(`/sessions/${id}/dashboard`),
  getStudents: (id) => api.get(`/sessions/${id}/students`),
};

export const studentApi = {
  add: (data) => api.post('/students', data),
  markAttendance: (id, status) => api.put(`/students/${id}/attendance?status=${status}`),
  raiseHand: (id) => api.put(`/students/${id}/raise-hand`),
  updateEngagement: (data) => api.put('/students/engagement', data),
};

export default api;
