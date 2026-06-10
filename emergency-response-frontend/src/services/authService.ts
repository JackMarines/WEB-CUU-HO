import api from './api';
import { User, AuthResponse } from '@/types';

export const authService = {
  async login(email: string, password: string): Promise<AuthResponse> {
    const res = await api.post('/auth/login', { email, password });
    return res.data;
  },
  async getMe(): Promise<User> {
    const res = await api.get('/auth/me');
    return res.data;
  },
  async updateProfile(name: string, phone?: string): Promise<User> {
    const res = await api.put('/auth/profile', { name, phone });
    return res.data;
  },
  async changePassword(currentPassword: string, newPassword: string): Promise<void> {
    await api.put('/auth/password', { currentPassword, newPassword });
  },
  async forgotPassword(email: string): Promise<{ message: string; token?: string }> {
    const res = await api.post('/auth/forgot-password', { email });
    return res.data;
  },
  async resetPassword(token: string, newPassword: string): Promise<void> {
    await api.post('/auth/reset-password', { token, newPassword });
  },
};
