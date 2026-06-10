'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { callService } from '@/services/callService';
import { disasterTypeService } from '@/services/disasterTypeService';
import { DistressCall, DisasterType } from '@/types';

export default function CallsPage() {
  const router = useRouter();
  const [calls, setCalls] = useState<DistressCall[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [disasterTypes, setDisasterTypes] = useState<DisasterType[]>([]);

  const fetch = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const data = await callService.getAll(
        typeFilter || undefined,
        statusFilter || undefined,
        searchQuery || undefined,
        dateFrom || undefined,
        dateTo || undefined,
      );
      setCalls(data);
    } catch {
      setError('Không thể tải danh sách cuộc gọi');
    } finally {
      setLoading(false);
    }
  }, [typeFilter, statusFilter, searchQuery, dateFrom, dateTo]);

  useEffect(() => { fetch(); }, [fetch]);

  useEffect(() => {
    disasterTypeService.getAll().then(setDisasterTypes).catch(() => {});
  }, []);

  function getUrgencyClass(score: number) {
    if (score >= 80) return 'bg-primary-subtle text-status-high';
    if (score >= 60) return 'bg-[rgba(255,138,80,0.15)] text-status-medium';
    return 'bg-[rgba(212,168,75,0.15)] text-status-low';
  }

  function getStatusClass(status: string) {
    switch (status) {
      case 'active': return 'bg-status-high/10 text-status-high';
      case 'in_progress': return 'bg-[rgba(255,138,80,0.15)] text-status-medium';
      case 'resolved': return 'bg-[rgba(76,175,80,0.15)] text-[#4CAF50]';
      default: return 'text-text-muted';
    }
  }

  const statusLabels: Record<string, string> = {
    active: 'Đang hoạt động',
    in_progress: 'Đang xử lý',
    resolved: 'Đã giải quyết',
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-text-primary">Cuộc gọi cứu hộ</h1>
      </div>

      {error && <p className="text-status-high text-sm mb-4">{error}</p>}

      <div className="flex flex-wrap gap-3 mb-4">
        <input
          type="text"
          value={searchQuery}
          onChange={e => setSearchQuery(e.target.value)}
          placeholder="Tìm kiếm người dùng, địa điểm..."
          className="px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors min-w-[220px]"
        />
        <input
          type="date"
          value={dateFrom}
          onChange={e => setDateFrom(e.target.value)}
          className="px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
        />
        <input
          type="date"
          value={dateTo}
          onChange={e => setDateTo(e.target.value)}
          className="px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
        />
        <select
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value)}
          className="px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="active">Đang hoạt động</option>
          <option value="in_progress">Đang xử lý</option>
          <option value="resolved">Đã giải quyết</option>
        </select>
        <select
          value={typeFilter}
          onChange={e => setTypeFilter(e.target.value)}
          className="px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
        >
          <option value="">Tất cả loại</option>
          {disasterTypes.map(dt => (
            <option key={dt.id} value={dt.slug}>{dt.icon} {dt.name}</option>
          ))}
        </select>
      </div>

      {loading ? (
        <p className="text-text-muted">Đang tải...</p>
      ) : calls.length === 0 ? (
        <p className="text-text-muted">Không tìm thấy cuộc gọi nào.</p>
      ) : (
        <div className="overflow-x-auto rounded-section border border-border-default">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-surface-section text-text-muted uppercase text-xs tracking-wider">
                <th className="text-left px-4 py-3 font-medium">Người dùng</th>
                <th className="text-left px-4 py-3 font-medium">Loại</th>
                <th className="text-left px-4 py-3 font-medium">Vị trí</th>
                <th className="text-left px-4 py-3 font-medium">Mức độ</th>
                <th className="text-left px-4 py-3 font-medium">Trạng thái</th>
                <th className="text-left px-4 py-3 font-medium">Thời gian</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-default">
              {calls.map((call) => (
                <tr
                  key={call.id}
                  onClick={() => router.push(`/admin/calls/${call.id}`)}
                  className="hover:bg-surface-card transition-colors cursor-pointer"
                >
                  <td className="px-4 py-3 text-text-body font-medium">{call.userName}</td>
                  <td className="px-4 py-3">
                    <span className="text-xl mr-1">{call.disasterType.icon}</span>
                    <span className="text-text-body">{call.disasterType.name}</span>
                  </td>
                  <td className="px-4 py-3 text-text-muted max-w-[200px] truncate">
                    {call.locationName || `${call.lat.toFixed(4)}, ${call.lng.toFixed(4)}`}
                  </td>
                  <td className="px-4 py-3">
                    <span className={`inline-block px-2 py-0.5 rounded-tag text-xs font-medium ${getUrgencyClass(call.urgencyScore)}`}>
                      {call.urgencyScore}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`inline-block px-2 py-0.5 rounded-tag text-xs font-medium ${getStatusClass(call.status)}`}>
                      {statusLabels[call.status] || call.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-text-muted text-xs">
                    {new Date(call.createdAt).toLocaleString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
