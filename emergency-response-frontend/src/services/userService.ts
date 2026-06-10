import api from './api';
import { User } from '@/types';

export const userService = {
  getAll: () =>
    api.get<User[]>('/admin/users').then(r => r.data),
  create: (name: string, email: string, password: string) =>
    api.post<User>('/admin/users', { name, email, password }).then(r => r.data),
  delete: (id: number) =>
    api.delete(`/admin/users/${id}`),
};
