import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

// Contracts
export const fetchContracts = () => apiClient.get('/api/v1/contracts').then(r => r.data);
export const fetchContract = (id) => apiClient.get(`/api/v1/contracts/${id}`).then(r => r.data);
export const createContract = (data) => apiClient.post('/api/v1/contracts', data).then(r => r.data);
export const executeContract = (id, facts) => apiClient.post(`/api/v1/contracts/${id}/execute`, facts || {}).then(r => r.data);
export const cancelContract = (id) => apiClient.post(`/api/v1/contracts/${id}/cancel`).then(r => r.data);
export const fetchContractEvents = (id) => apiClient.get(`/api/v1/contracts/${id}/events`).then(r => r.data);
export const fetchExecutionLogs = (id) => apiClient.get(`/api/v1/contracts/${id}/execution-logs`).then(r => r.data);

// Rules
export const createRule = (data) => apiClient.post('/api/v1/rules', data).then(r => r.data);
export const fetchRule = (id) => apiClient.get(`/api/v1/rules/${id}`).then(r => r.data);

// Admin
export const replayContract = (id) => apiClient.post(`/admin/replay/${id}`).then(r => r.data);
export const fetchDeadLetters = () => apiClient.get('/admin/dead-letters').then(r => r.data);

// Health / Actuator
export const fetchHealth = () => apiClient.get('/health').then(r => r.data);
export const fetchActuatorHealth = () => apiClient.get('/actuator/health').then(r => r.data);
export const fetchActuatorInfo = () => apiClient.get('/actuator/info').then(r => r.data);
export const fetchPrometheus = () => apiClient.get('/actuator/prometheus').then(r => r.data);

export default apiClient;
