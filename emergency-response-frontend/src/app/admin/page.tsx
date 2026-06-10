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
      .catch(() => setError('Failed to load dashboard'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="text-text-muted">Loading...</p>;
  if (error) return <p className="text-status-high">{error}</p>;
  if (!stats) return null;

  const statusLabels: Record<string, string> = {
    active: 'Active',
    in_progress: 'In Progress',
    resolved: 'Resolved',
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
      <h1 className="text-2xl font-bold text-text-primary mb-6">Dashboard</h1>

      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-8">
        <div className="rounded-section border border-border-default bg-surface-elevated p-4">
          <p className="text-text-muted text-xs uppercase tracking-wider mb-1">Total Calls</p>
          <p className="text-3xl font-bold text-text-primary">{stats.totalCalls}</p>
        </div>
        <div className="rounded-section border border-border-default bg-surface-elevated p-4">
          <p className="text-text-muted text-xs uppercase tracking-wider mb-1">Active</p>
          <p className="text-3xl font-bold text-status-high">{stats.activeCalls}</p>
        </div>
        <div className="rounded-section border border-border-default bg-surface-elevated p-4">
          <p className="text-text-muted text-xs uppercase tracking-wider mb-1">In Progress</p>
          <p className="text-3xl font-bold text-status-medium">{stats.inProgressCalls}</p>
        </div>
        <div className="rounded-section border border-border-default bg-surface-elevated p-4">
          <p className="text-text-muted text-xs uppercase tracking-wider mb-1">Resolved</p>
          <p className="text-3xl font-bold text-[#4CAF50]">{stats.resolvedCalls}</p>
        </div>
        <div className="rounded-section border border-border-default bg-surface-elevated p-4">
          <p className="text-text-muted text-xs uppercase tracking-wider mb-1">Centers</p>
          <p className="text-3xl font-bold text-primary">{stats.totalCenters}</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1 rounded-section border border-border-default bg-surface-elevated p-6">
          <h2 className="text-lg font-bold text-text-primary mb-4">Calls by Type</h2>
          <div className="space-y-3">
            {stats.callsByType.length === 0 ? (
              <p className="text-text-muted text-sm">No data</p>
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
          <h2 className="text-lg font-bold text-text-primary mb-4">Recent Calls</h2>
          {stats.recentCalls.length === 0 ? (
            <p className="text-text-muted text-sm">No recent calls.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-text-muted uppercase text-xs tracking-wider border-b border-border-default">
                    <th className="text-left pb-3 font-medium">User</th>
                    <th className="text-left pb-3 font-medium">Type</th>
                    <th className="text-left pb-3 font-medium">Urgency</th>
                    <th className="text-left pb-3 font-medium">Status</th>
                    <th className="text-left pb-3 font-medium">Time</th>
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
          )}
        </div>
      </div>
    </div>
  );
}
