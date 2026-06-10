'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import { authService } from '@/services/authService';

export default function ProfilePage() {
  const { user, loading, setUser } = useAuth();
  const router = useRouter();
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [changingPw, setChangingPw] = useState(false);
  const [pwError, setPwError] = useState('');
  const [pwSuccess, setPwSuccess] = useState('');

  useEffect(() => {
    if (!loading && !user) router.push('/login');
    if (user) {
      setName(user.name);
      setPhone(user.phone || '');
    }
  }, [user, loading, router]);

  if (loading || !user) return null;

  async function handleProfileUpdate(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const updated = await authService.updateProfile(name, phone);
      const updatedUser = { ...user, ...updated };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      setUser(updatedUser);
      setSuccess('Cập nhật thành công');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Cập nhật thất bại');
    } finally {
      setSaving(false);
    }
  }

  async function handlePasswordChange(e: React.FormEvent) {
    e.preventDefault();
    if (!currentPassword || !newPassword) return;
    setChangingPw(true);
    setPwError('');
    setPwSuccess('');
    try {
      await authService.changePassword(currentPassword, newPassword);
      setPwSuccess('Đổi mật khẩu thành công');
      setCurrentPassword('');
      setNewPassword('');
    } catch (err: any) {
      setPwError(err.response?.data?.error || 'Đổi mật khẩu thất bại');
    } finally {
      setChangingPw(false);
    }
  }

  return (
    <div className="max-w-2xl mx-auto p-6 space-y-8">
      <h1 className="text-xl font-bold text-text-primary">Thông tin tài khoản</h1>

      <div className="rounded-section border border-border-default bg-surface-elevated p-6">
        <div className="flex items-center gap-4 mb-6">
          {user.avatarUrl ? (
            <img src={user.avatarUrl} alt="" className="w-14 h-14 rounded-full object-cover" />
          ) : (
            <div className="w-14 h-14 rounded-full bg-primary-subtle flex items-center justify-center text-primary font-bold text-lg">
              {user.name.charAt(0).toUpperCase()}
            </div>
          )}
          <div>
            <p className="text-text-body font-medium">{user.name}</p>
            <p className="text-text-muted text-xs">{user.email}</p>
          </div>
        </div>

        {error && <p className="text-status-high text-sm mb-4">{error}</p>}
        {success && <p className="text-[#4CAF50] text-sm mb-4">{success}</p>}

        <form onSubmit={handleProfileUpdate} className="space-y-4">
          <div>
            <label className="block text-xs text-text-muted mb-1 font-semibold uppercase tracking-wider">
              Họ và tên
            </label>
            <input
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              required
              className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
            />
          </div>
          <div>
            <label className="block text-xs text-text-muted mb-1 font-semibold uppercase tracking-wider">
              Email
            </label>
            <input
              type="email"
              value={user.email}
              disabled
              className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-muted text-sm cursor-not-allowed"
            />
          </div>
          <div>
            <label className="block text-xs text-text-muted mb-1 font-semibold uppercase tracking-wider">
              Số điện thoại
            </label>
            <input
              type="tel"
              value={phone}
              onChange={e => setPhone(e.target.value)}
              className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
            />
          </div>
          <button
            type="submit"
            disabled={saving}
            className="px-4 py-2 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 disabled:opacity-50 transition-opacity cursor-pointer"
          >
            {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
          </button>
        </form>
      </div>

      {user.provider === 'local' && (
        <div className="rounded-section border border-border-default bg-surface-elevated p-6">
          <h2 className="text-lg font-bold text-text-primary mb-4">Đổi mật khẩu</h2>
          {pwError && <p className="text-status-high text-sm mb-4">{pwError}</p>}
          {pwSuccess && <p className="text-[#4CAF50] text-sm mb-4">{pwSuccess}</p>}
          <form onSubmit={handlePasswordChange} className="space-y-4">
            <div>
              <label className="block text-xs text-text-muted mb-1 font-semibold uppercase tracking-wider">
                Mật khẩu hiện tại
              </label>
              <input
                type="password"
                value={currentPassword}
                onChange={e => setCurrentPassword(e.target.value)}
                required
                className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
              />
            </div>
            <div>
              <label className="block text-xs text-text-muted mb-1 font-semibold uppercase tracking-wider">
                Mật khẩu mới
              </label>
              <input
                type="password"
                value={newPassword}
                onChange={e => setNewPassword(e.target.value)}
                required
                minLength={6}
                className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
              />
            </div>
            <button
              type="submit"
              disabled={changingPw}
              className="px-4 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm font-medium hover:border-primary transition-colors disabled:opacity-50 cursor-pointer"
            >
              {changingPw ? 'Đang đổi...' : 'Đổi mật khẩu'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
