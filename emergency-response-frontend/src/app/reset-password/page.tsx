'use client';

import { useState } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { authService } from '@/services/authService';

export default function ResetPasswordPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [token] = useState(searchParams.get('token') || '');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setMessage('');

    if (newPassword.length < 6) {
      setError('Mật khẩu phải có ít nhất 6 ký tự');
      return;
    }
    if (newPassword !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp');
      return;
    }

    setLoading(true);
    try {
      await authService.resetPassword(token, newPassword);
      setMessage('Mật khẩu đã được đặt lại thành công!');
      setTimeout(() => router.push('/login'), 2000);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Đặt lại mật khẩu thất bại');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-surface-medium px-4">
      <div className="w-full max-w-sm rounded-section border border-border-default bg-surface-elevated p-8">
        <h1 className="text-xl font-bold text-text-primary text-center mb-6">Đặt lại mật khẩu</h1>
        {error && <p className="text-status-high text-sm mb-4">{error}</p>}
        {message && <p className="text-status-low text-sm mb-4">{message}</p>}
        {!message && (
          <form onSubmit={handleSubmit} className="space-y-4">
            {!token && (
              <div>
                <label className="block text-xs text-text-muted mb-1">Mã token</label>
                <input
                  type="text"
                  placeholder="Nhập token từ email"
                  value={token}
                  readOnly
                  className="w-full px-4 py-2.5 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors opacity-70"
                />
              </div>
            )}
            <div>
              <label className="block text-xs text-text-muted mb-1">Mật khẩu mới</label>
              <input
                type="password"
                placeholder="Ít nhất 6 ký tự"
                value={newPassword}
                onChange={e => setNewPassword(e.target.value)}
                required
                className="w-full px-4 py-2.5 rounded-pill bg-surface-default border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
              />
            </div>
            <div>
              <label className="block text-xs text-text-muted mb-1">Xác nhận mật khẩu</label>
              <input
                type="password"
                placeholder="Nhập lại mật khẩu mới"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                required
                className="w-full px-4 py-2.5 rounded-pill bg-surface-default border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 transition-opacity disabled:opacity-50 cursor-pointer"
            >
              {loading ? 'Đang xử lý...' : 'Đặt lại mật khẩu'}
            </button>
          </form>
        )}
        <p className="text-center text-xs text-text-muted mt-6">
          <Link href="/login" className="text-primary hover:underline">Quay lại đăng nhập</Link>
        </p>
      </div>
    </div>
  );
}
