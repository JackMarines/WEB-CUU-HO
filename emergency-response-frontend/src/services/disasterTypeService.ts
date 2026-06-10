import api from './api';
import { DisasterType } from '@/types';

export const disasterTypeService = {
  getAll: () =>
    api.get<DisasterType[]>('/disaster-types').then(r => r.data),
  getById: (id: number) =>
    api.get<DisasterType>(`/disaster-types/${id}`).then(r => r.data),
  create: (data: { name: string; slug: string; icon: string; baseUrgencyScore: number }) =>
    api.post<DisasterType>('/disaster-types', data).then(r => r.data),
  update: (id: number, data: { name: string; slug: string; icon: string; baseUrgencyScore: number }) =>
    api.put<DisasterType>(`/disaster-types/${id}`, data).then(r => r.data),
  delete: (id: number) =>
    api.delete(`/disaster-types/${id}`),
};
