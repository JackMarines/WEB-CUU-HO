'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { callService } from '@/services/callService';
import { DashboardStats } from '@/types';

export default function AdminDashboardPage() {
  const router = useRouter();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    callService.getStats()
      .then(setStats)
      .catch(() => setError('Không thể tải dữ liệu'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="text-text-muted">Đang tải...</p>;
  if (error) return <p className="text-status-high">{error}</p>;
  if (!stats) return null;

  const statusLabels: Record<string, string> = {
    active: 'Đang hoạt động',
    in_progress: 'Đang xử lý',
    resolved: 'Đã giải quyết',
  };

  function getStatusClass(status: string) {
    switch (status) {
      case 'active': return 'bg-status-high/10 text-status-high';
      case 'in_progress': return 'bg-[rgba(255,138,80,0.15)] text-status-medium';
      case 'resolved': return 'bg-[rgba(76,175,80,0.15)] text-[#4CAF50]';
      default: return 'text-text-muted';
    }
  }

  function getUrgencyClass(score: number) {
    if (score >= 80) return 'bg-primary-subtle text-status-high';
    if (score >= 60) return 'bg-[rgba(255,138,80,0.15)] text-status-medium';
    return 'bg-[rgba(212,168,75,0.15)] text-status-low';
  }

  const maxTypeCount = Math.max(...stats.callsByType.map(t => t.count), 1);

  return (
    <div>
      <h1 className="text-2xl font-bold text-text-primary mb-6">Tổng quan</h1>

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3 sm:gap-4 mb-8">
        <div className="rounded-section border border-border-default bg-surface-elevated p-4">
          <p className="text-text-muted text-xs uppercase tracking-wider mb-1">Tổng cuộc gọi</p>
          <p className="text-3xl font-bold text-text-primary">{stats.totalCalls}</p>
        </div>
        <div className="rounded-section border border-border-default bg-surface-elevated p-4">
          <p className="text-text-muted text-xs uppercase tracking-wider mb-1">Đang hoạt động</p>
          <p className="text-3xl font-bold text-status-high">{stats.activeCalls}</p>
        </div>
        <div className="rounded-section border border-border-default bg-surface-elevated p-4">
          <p className="text-text-muted text-xs uppercase tracking-wider mb-1">Đang xử lý</p>
          <p className="text-3xl font-bold text-status-medium">{stats.inProgressCalls}</p>
        </div>
        <div className="rounded-section border border-border-default bg-surface-elevated p-4">
          <p className="text-text-muted text-xs uppercase tracking-wider mb-1">Đã giải quyết</p>
          <p className="text-3xl font-bold text-[#4CAF50]">{stats.resolvedCalls}</p>
        </div>
        <div className="rounded-section border border-border-default bg-surface-elevated p-4">
          <p className="text-text-muted text-xs uppercase tracking-wider mb-1">Trung tâm</p>
          <p className="text-3xl font-bold text-primary">{stats.totalCenters}</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1 rounded-section border border-border-default bg-surface-elevated p-6">
          <h2 className="text-lg font-bold text-text-primary mb-4">Theo loại</h2>
          <div className="space-y-3">
            {stats.callsByType.length === 0 ? (
              <p className="text-text-muted text-sm">Không có dữ liệu</p>
            ) : (
              stats.callsByType.map(t => (
                <div key={t.type}>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-text-body capitalize">{t.type.replace('_', ' ')}</span>
                    <span className="text-text-muted">{t.count}</span>
                  </div>
                  <div className="h-2 rounded-full bg-surface-card overflow-hidden">
                    <div
                      className="h-full rounded-full bg-primary transition-all"
                      style={{ width: `${(t.count / maxTypeCount) * 100}%` }}
                    />
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="lg:col-span-2 rounded-section border border-border-default bg-surface-elevated p-6">
          <h2 className="text-lg font-bold text-text-primary mb-4">Gần đây</h2>
          {stats.recentCalls.length === 0 ? (
            <p className="text-text-muted text-sm">Không có cuộc gọi nào.</p>
          ) : (
            <>
              <div className="sm:hidden space-y-2">
                {stats.recentCalls.map(call => (
                  <div
                    key={call.id}
                    onClick={() => router.push(`/admin/calls/${call.id}`)}
                    className="flex items-center justify-between p-3 rounded-pill bg-surface-card cursor-pointer hover:bg-surface-section transition-colors"
                  >
                    <div className="flex items-center gap-2 min-w-0">
                      <span className="text-lg shrink-0">{call.disasterType.icon}</span>
                      <div className="min-w-0">
                        <p className="text-text-body text-sm truncate">{call.userName}</p>
                        <p className="text-text-muted text-[10px] truncate">{new Date(call.createdAt).toLocaleString()}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-1.5 shrink-0">
                      <span className={`px-2 py-0.5 rounded-tag text-[10px] font-medium ${getUrgencyClass(call.urgencyScore)}`}>
                        {call.urgencyScore}
                      </span>
                      <span className={`px-2 py-0.5 rounded-tag text-[10px] font-medium ${getStatusClass(call.status)}`}>
                        {statusLabels[call.status] || call.status}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
              <div className="hidden sm:block overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-text-muted uppercase text-xs tracking-wider border-b border-border-default">
                      <th className="text-left pb-3 font-medium">Người dùng</th>
                      <th className="text-left pb-3 font-medium">Loại</th>
                      <th className="text-left pb-3 font-medium">Mức độ</th>
                      <th className="text-left pb-3 font-medium">Trạng thái</th>
                      <th className="text-left pb-3 font-medium">Thời gian</th>
                    </tr>
                  </thead>
                  <tbody>
                    {stats.recentCalls.map(call => (
                      <tr
                        key={call.id}
                        onClick={() => router.push(`/admin/calls/${call.id}`)}
                        className="border-b border-border-default hover:bg-surface-card transition-colors cursor-pointer"
                      >
                        <td className="py-3 text-text-body font-medium">{call.userName}</td>
                        <td className="py-3">
                          <span className="text-xl mr-1">{call.disasterType.icon}</span>
                          <span className="text-text-body">{call.disasterType.name}</span>
                        </td>
                        <td className="py-3">
                          <span className={`inline-block px-2 py-0.5 rounded-tag text-xs font-medium ${getUrgencyClass(call.urgencyScore)}`}>
                            {call.urgencyScore}
                          </span>
                        </td>
                        <td className="py-3">
                          <span className={`inline-block px-2 py-0.5 rounded-tag text-xs font-medium ${getStatusClass(call.status)}`}>
                            {statusLabels[call.status] || call.status}
                          </span>
                        </td>
                        <td className="py-3 text-text-muted text-xs">
                          {new Date(call.createdAt).toLocaleString()}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
