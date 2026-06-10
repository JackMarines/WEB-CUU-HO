import api from './api';
import { DistressCall, DashboardStats } from '@/types';

export const callService = {
  getAll: (type?: string, status?: string, q?: string, dateFrom?: string, dateTo?: string) =>
    api.get<DistressCall[]>('/calls', { params: { type, status, q, dateFrom, dateTo } }).then(r => r.data),
  getById: (id: number) =>
    api.get<DistressCall>(`/calls/${id}`).then(r => r.data),
  getMine: () =>
    api.get<DistressCall[]>('/calls/mine').then(r => r.data),
  create: (data: any) =>
    api.post<DistressCall>('/calls', data).then(r => r.data),
  updateStatus: (id: number, status: string) =>
    api.put<DistressCall>(`/calls/${id}/status`, { status }).then(r => r.data),
  getResponses: (callId: number) =>
    api.get<any[]>(`/calls/${callId}/responses`).then(r => r.data),
  assignResponse: (callId: number, rescueCenterId: number, note?: string) =>
    api.post<any>(`/calls/${callId}/responses`, { rescueCenterId, note }).then(r => r.data),
  updateResponseStatus: (id: number, status: string) =>
    api.put(`/calls/responses/${id}/status`, { status }),
  submitFeedback: (callId: number, rating: number, feedback?: string) =>
    api.post(`/calls/${callId}/feedback`, { rating, feedback }).then(r => r.data),
  getStats: () =>
    api.get<DashboardStats>('/calls/stats').then(r => r.data),
};
