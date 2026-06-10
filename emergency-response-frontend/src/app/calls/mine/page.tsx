'use client';

import { useState, useEffect } from 'react';
import { Star, Check } from 'lucide-react';
import { callService } from '@/services/callService';
import { DistressCall } from '@/types';

const statusLabels: Record<string, string> = {
  active: 'Đang hoạt động',
  in_progress: 'Đang xử lý',
  resolved: 'Đã giải quyết',
};

export default function MyCallsPage() {
  const [calls, setCalls] = useState<DistressCall[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [ratings, setRatings] = useState<Record<number, number>>({});
  const [feedbacks, setFeedbacks] = useState<Record<number, string>>({});
  const [submitting, setSubmitting] = useState<Record<number, boolean>>({});
  const [submitted, setSubmitted] = useState<Record<number, boolean>>({});

  useEffect(() => {
    callService.getMine()
      .then(setCalls)
      .catch(() => setError('Không thể tải danh sách'))
      .finally(() => setLoading(false));
  }, []);

  function getResponse(callId: number) {
    return callService.getResponses(callId).then(responses => responses[0]);
  }

  async function handleSubmit(callId: number) {
    const rating = ratings[callId];
    if (!rating) return;
    setSubmitting(prev => ({ ...prev, [callId]: true }));
    try {
      await callService.submitFeedback(callId, rating, feedbacks[callId] || '');
      setSubmitted(prev => ({ ...prev, [callId]: true }));
    } catch {
      setError('Không thể gửi đánh giá');
    } finally {
      setSubmitting(prev => ({ ...prev, [callId]: false }));
    }
  }

  if (loading) return <p className="text-text-muted p-6">Đang tải...</p>;

  return (
    <div className="max-w-3xl mx-auto p-6">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-bold text-text-primary">Yêu cầu của tôi</h1>
        <a
          href="/submit"
          className="px-4 py-2 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 transition-opacity"
        >
          + Gửi yêu cầu mới
        </a>
      </div>
      {error && <p className="text-status-high text-sm mb-4">{error}</p>}

      {calls.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-text-muted">Bạn chưa có yêu cầu cứu trợ nào.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {calls.map(call => (
            <div
              key={call.id}
              className="rounded-section border border-border-default bg-surface-elevated p-4 hover:border-[#555] transition-colors cursor-pointer"
              onClick={() => setExpandedId(expandedId === call.id ? null : call.id)}
            >
              <div className="flex items-start justify-between mb-2">
                <div>
                  <span className="text-text-body font-medium text-sm">
                    {call.disasterType.icon} {call.disasterType.name}
                  </span>
                  <p className="text-text-muted text-xs mt-0.5">
                    {call.locationName || `${call.lat.toFixed(4)}, ${call.lng.toFixed(4)}`}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`px-2 py-0.5 rounded-tag text-xs font-medium ${
                    call.urgencyScore >= 80 ? 'bg-primary-subtle text-status-high' :
                    call.urgencyScore >= 60 ? 'bg-[rgba(255,138,80,0.15)] text-status-medium' :
                    'bg-[rgba(212,168,75,0.15)] text-status-low'
                  }`}>
                    {call.urgencyScore}
                  </span>
                  <span className={`px-2 py-0.5 rounded-tag text-xs font-medium ${
                    call.status === 'active' ? 'bg-primary-subtle text-status-high' :
                    call.status === 'in_progress' ? 'bg-[rgba(255,138,80,0.15)] text-status-medium' :
                    'bg-[rgba(76,175,80,0.15)] text-[#4CAF50]'
                  }`}>
                    {statusLabels[call.status]}
                  </span>
                </div>
              </div>
              <p className="text-text-muted text-sm line-clamp-2">{call.description}</p>
              <p className="text-text-subtle text-xs mt-2">
                {new Date(call.createdAt).toLocaleString('vi-VN')}
              </p>

              {expandedId === call.id && (
                <div className="mt-4 pt-4 border-t border-border-default">
                  {call.status === 'resolved' && !submitted[call.id] ? (
                    <>
                      <p className="text-sm font-medium text-text-primary mb-3">Đánh giá chất lượng cứu trợ</p>
                      <div className="flex items-center gap-1 mb-3">
                        {[1, 2, 3, 4, 5].map(star => (
                          <button
                            key={star}
                            type="button"
                            onClick={e => { e.stopPropagation(); setRatings(prev => ({ ...prev, [call.id]: star })); }}
                            className={`transition-colors ${
                              (ratings[call.id] ?? 0) >= star
                                ? 'text-[#f5a623]'
                                : 'text-text-subtle hover:text-[#f5a623]'
                            }`}
                          >
                            <Star size={22} fill={(ratings[call.id] ?? 0) >= star ? '#f5a623' : 'none'} />
                          </button>
                        ))}
                      </div>
                      <textarea
                        placeholder="Nhận xét của bạn (không bắt buộc)..."
                        value={feedbacks[call.id] ?? ''}
                        onChange={e => setFeedbacks(prev => ({ ...prev, [call.id]: e.target.value }))}
                        className="w-full px-3 py-2 rounded-pill bg-surface-default border border-border-default text-text-body text-sm resize-none focus:outline-none focus:border-primary transition-colors"
                        rows={3}
                        onClick={e => e.stopPropagation()}
                      />
                      <button
                        type="button"
                        onClick={e => { e.stopPropagation(); handleSubmit(call.id); }}
                        disabled={!ratings[call.id] || submitting[call.id]}
                        className="mt-3 px-4 py-2 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 transition-opacity disabled:opacity-50"
                      >
                        {submitting[call.id] ? 'Đang gửi...' : 'Gửi đánh giá'}
                      </button>
                    </>
                  ) : call.status === 'resolved' && submitted[call.id] ? (
                    <p className="text-sm text-status-low"><Check className="inline mr-1" size={16} />Đã gửi đánh giá</p>
                  ) : (
                    <p className="text-sm text-text-muted">
                      {call.status === 'active' ? 'Đang chờ xử lý...' : 'Đang được cứu trợ...'}
                    </p>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
