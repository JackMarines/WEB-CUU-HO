'use client';

import { useState, useEffect } from 'react';
import { userService } from '@/services/userService';
import { useAuth } from '@/contexts/AuthContext';
import { User } from '@/types';

export default function AdminUsersPage() {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showCreate, setShowCreate] = useState(false);
  const [createName, setCreateName] = useState('');
  const [createEmail, setCreateEmail] = useState('');
  const [createPassword, setCreatePassword] = useState('');
  const [creating, setCreating] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<User | null>(null);

  useEffect(() => {
    userService.getAll()
      .then(setUsers)
      .catch(() => setError('Không thể tải danh sách người dùng'))
      .finally(() => setLoading(false));
  }, []);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    setCreating(true);
    setError('');
    try {
      const created = await userService.create(createName, createEmail, createPassword);
      setUsers(prev => [created, ...prev]);
      setShowCreate(false);
      setCreateName('');
      setCreateEmail('');
      setCreatePassword('');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Tạo admin thất bại');
    } finally {
      setCreating(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await userService.delete(deleteTarget.id);
      setUsers(prev => prev.filter(u => u.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Xóa người dùng thất bại');
      setDeleteTarget(null);
    }
  }

  function canDelete(target: User) {
    if (!currentUser) return false;
    if (currentUser.id === target.id) return false;
    if (target.role === 'superadmin') return false;
    if (currentUser.role === 'admin') return target.role === 'user';
    return true;
  }

  if (loading) return <p className="text-text-muted">Đang tải...</p>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-bold text-text-primary">Người dùng</h1>
        {currentUser?.role === 'superadmin' && (
          <button
            onClick={() => setShowCreate(true)}
            className="px-4 py-2 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 transition-opacity cursor-pointer"
          >
            + Tạo Admin
          </button>
        )}
      </div>

      {error && <p className="text-status-high text-sm mb-4">{error}</p>}

      <><div className="sm:hidden space-y-3">
        {users.map(u => (
          <div key={u.id} className={`rounded-section border border-border-default bg-surface-elevated p-4 ${currentUser?.id === u.id ? 'border-primary' : ''}`}>
            <div className="flex items-center justify-between mb-2">
              <span className="text-text-body font-medium text-sm">{u.name}{currentUser?.id === u.id && <span className="text-xs text-text-muted ml-1">(bạn)</span>}</span>
              <button
                onClick={() => setDeleteTarget(u)}
                disabled={!canDelete(u)}
                className={`text-xs px-2.5 py-1 rounded-pill transition-colors cursor-pointer min-w-[44px] min-h-[44px] flex items-center justify-center ${
                  canDelete(u)
                    ? 'bg-[rgba(248,110,100,0.15)] text-status-high'
                    : 'bg-surface-card text-text-subtle opacity-40 cursor-not-allowed'
                }`}
              >
                Xóa
              </button>
            </div>
            <p className="text-text-muted text-xs mb-1">{u.email}</p>
            <div className="flex items-center gap-2">
              <span className={`px-2 py-0.5 rounded-tag text-xs font-medium ${
                u.role === 'superadmin' ? 'bg-[rgba(179,157,219,0.15)] text-[#B39DDB]' :
                u.role === 'admin' ? 'bg-primary-subtle text-status-high' :
                'bg-surface-card text-text-muted'
              }`}>
                {u.role}
              </span>
              <span className={`px-2 py-0.5 rounded-tag text-xs font-medium ${u.isActive ? 'bg-[rgba(76,175,80,0.15)] text-[#4CAF50]' : 'bg-[rgba(248,110,100,0.15)] text-status-high'}`}>
                {u.isActive ? 'Hoạt động' : 'Bị cấm'}
              </span>
              <span className="text-text-subtle text-[10px] ml-auto">{new Date(u.createdAt).toLocaleDateString()}</span>
            </div>
          </div>
        ))}
      </div>
      {/* Desktop table */}
      <div className="hidden sm:block rounded-section border border-border-default bg-surface-elevated overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border-default text-text-muted text-xs uppercase tracking-wider">
              <th className="text-left px-4 py-3 font-semibold">Tên</th>
              <th className="text-left px-4 py-3 font-semibold">Email</th>
              <th className="text-left px-4 py-3 font-semibold">Vai trò</th>
              <th className="text-left px-4 py-3 font-semibold">Trạng thái</th>
              <th className="text-left px-4 py-3 font-semibold">Ngày tạo</th>
              <th className="text-right px-4 py-3 font-semibold">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {users.map(u => (
              <tr key={u.id} className={`border-b border-border-default last:border-0 hover:bg-surface-medium transition-colors ${currentUser?.id === u.id ? 'bg-primary-subtle/5' : ''}`}>
                <td className="px-4 py-3 text-text-body font-medium">
                  {u.name}
                  {currentUser?.id === u.id && <span className="text-xs text-text-muted ml-1">(bạn)</span>}
                </td>
                <td className="px-4 py-3 text-text-muted">{u.email}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-tag text-xs font-medium ${
                    u.role === 'superadmin' ? 'bg-[rgba(179,157,219,0.15)] text-[#B39DDB]' :
                    u.role === 'admin' ? 'bg-primary-subtle text-status-high' :
                    'bg-surface-card text-text-muted'
                  }`}>
                    {u.role}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-tag text-xs font-medium ${u.isActive ? 'bg-[rgba(76,175,80,0.15)] text-[#4CAF50]' : 'bg-[rgba(248,110,100,0.15)] text-status-high'}`}>
                    {u.isActive ? 'Hoạt động' : 'Bị cấm'}
                  </span>
                </td>
                <td className="px-4 py-3 text-text-muted text-xs">
                  {new Date(u.createdAt).toLocaleDateString()}
                </td>
                <td className="px-4 py-3 text-right">
                  <button
                    onClick={() => setDeleteTarget(u)}
                    disabled={!canDelete(u)}
                    className={`text-xs px-2.5 py-1 rounded-pill transition-colors cursor-pointer ${
                      canDelete(u)
                        ? 'bg-[rgba(248,110,100,0.15)] text-status-high hover:bg-[rgba(248,110,100,0.25)]'
                        : 'bg-surface-card text-text-subtle opacity-40 cursor-not-allowed'
                    }`}
                  >
                    Xóa
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      </>

      {showCreate && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
          <div className="bg-surface-elevated rounded-section p-4 sm:p-6 w-full max-w-md border border-border-default">
            <h2 className="text-lg font-bold text-text-primary mb-4">Tạo tài khoản Admin</h2>
            {error && <p className="text-status-high text-sm mb-3">{error}</p>}
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs text-text-muted mb-1">Họ tên</label>
                <input
                  value={createName}
                  onChange={e => setCreateName(e.target.value)}
                  required
                  className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
                />
              </div>
              <div>
                <label className="block text-xs text-text-muted mb-1">Email</label>
                <input
                  type="email"
                  value={createEmail}
                  onChange={e => setCreateEmail(e.target.value)}
                  required
                  className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
                />
              </div>
              <div>
                <label className="block text-xs text-text-muted mb-1">Mật khẩu</label>
                <input
                  type="password"
                  value={createPassword}
                  onChange={e => setCreatePassword(e.target.value)}
                  required
                  minLength={6}
                  className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
                />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => { setShowCreate(false); setError(''); }}
                  className="px-4 py-2 rounded-pill border border-border-default text-text-muted text-sm hover:bg-surface-card transition-colors cursor-pointer"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={creating}
                  className="px-4 py-2 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 disabled:opacity-50 transition-opacity cursor-pointer"
                >
                  {creating ? 'Đang tạo...' : 'Tạo'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
          <div className="bg-surface-elevated rounded-section p-4 sm:p-6 w-full max-w-sm border border-border-default">
            <h2 className="text-lg font-bold text-text-primary mb-2">Xóa người dùng</h2>
            <p className="text-text-muted text-sm mb-1">Bạn có chắc muốn xóa</p>
            <p className="text-text-body font-medium mb-4">&ldquo;{deleteTarget.name}&rdquo;?</p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => { setDeleteTarget(null); setError(''); }}
                className="px-4 py-2 rounded-pill border border-border-default text-text-muted text-sm hover:bg-surface-card transition-colors cursor-pointer"
              >
                Hủy
              </button>
              <button
                onClick={handleDelete}
                className="px-4 py-2 rounded-pill bg-status-high text-white text-sm font-medium hover:opacity-90 transition-opacity cursor-pointer"
              >
                Xóa
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
