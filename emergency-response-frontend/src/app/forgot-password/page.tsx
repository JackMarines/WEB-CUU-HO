'use client';

import { useState } from 'react';
import Link from 'next/link';
import { authService } from '@/services/authService';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);
    try {
      const res = await authService.forgotPassword(email);
      setMessage(res.message);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Gửi yêu cầu thất bại');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-surface-medium px-4">
      <div className="w-full max-w-sm rounded-section border border-border-default bg-surface-elevated p-8">
        <h1 className="text-xl font-bold text-text-primary text-center mb-6">Quên mật khẩu</h1>
        <p className="text-text-muted text-sm text-center mb-6">
          Nhập email của bạn để nhận liên kết đặt lại mật khẩu
        </p>
        {error && <p className="text-status-high text-sm mb-4">{error}</p>}
        {message && <p className="text-status-low text-sm mb-4">{message}</p>}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <input
              type="email"
              placeholder="Email của bạn"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
              className="w-full px-4 py-2.5 rounded-pill bg-surface-default border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 transition-opacity disabled:opacity-50 cursor-pointer"
          >
            {loading ? 'Đang gửi...' : 'Gửi yêu cầu'}
          </button>
        </form>
        <p className="text-center text-xs text-text-muted mt-6">
          <Link href="/login" className="text-primary hover:underline">Quay lại đăng nhập</Link>
        </p>
      </div>
    </div>
  );
}
