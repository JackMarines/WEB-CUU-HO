import api from './api';
import { RescueCenter } from '@/types';

export const centerService = {
  getAll: (type?: string) =>
    api.get<RescueCenter[]>('/centers', { params: { type } }).then(r => r.data),
  getById: (id: number) =>
    api.get<RescueCenter>(`/centers/${id}`).then(r => r.data),
  create: (data: any) =>
    api.post<RescueCenter>('/centers', data).then(r => r.data),
  update: (id: number, data: any) =>
    api.put<RescueCenter>(`/centers/${id}`, data).then(r => r.data),
  delete: (id: number) =>
    api.delete(`/centers/${id}`),
};
