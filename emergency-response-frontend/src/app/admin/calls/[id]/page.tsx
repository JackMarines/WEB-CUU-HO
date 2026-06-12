'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Star } from 'lucide-react';
import dynamic from 'next/dynamic';
import { callService } from '@/services/callService';
import { centerService } from '@/services/centerService';
import { DistressCall, RescueCenter } from '@/types';
import ChatPanel from '@/components/chat/ChatPanel';

const MapView = dynamic(() => import('@/components/map/MapView'), { ssr: false });
const DistressCallMarker = dynamic(() => import('@/components/map/DistressCallMarker'), { ssr: false });
const RescueCenterMarker = dynamic(() => import('@/components/map/RescueCenterMarker'), { ssr: false });

export default function CallDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const [call, setCall] = useState<DistressCall | null>(null);
  const [responses, setResponses] = useState<any[]>([]);
  const [centers, setCenters] = useState<RescueCenter[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [address, setAddress] = useState('');
  const [selectedCenter, setSelectedCenter] = useState('');
  const [assignNote, setAssignNote] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!id) return;
    Promise.all([
      callService.getById(Number(id)),
      callService.getResponses(Number(id)),
      centerService.getAll(),
    ])
      .then(async ([callData, responsesData, centersData]) => {
        setCall(callData);
        setResponses(responsesData);
        setCenters(centersData);
        if (!callData.locationName) {
          try {
            const res = await fetch(
              `https://nominatim.openstreetmap.org/reverse?lat=${callData.lat}&lon=${callData.lng}&format=json&accept-language=vi`,
              { headers: { 'User-Agent': 'EmergencyResponse/1.0' } }
            );
            if (res.ok) {
              const data = await res.json();
              setAddress(data.display_name || '');
            }
          } catch {
            // ignore
          }
        }
      })
      .catch(() => setError('Không thể tải chi tiết cuộc gọi'))
      .finally(() => setLoading(false));
  }, [id]);

  async function handleStatusUpdate(newStatus: string) {
    if (!call) return;
    try {
      setError('');
      const updated = await callService.updateStatus(call.id, newStatus);
      setCall(updated);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Cập nhật trạng thái thất bại');
    }
  }

  async function handleAssign(e: React.FormEvent) {
    e.preventDefault();
    if (!call || !selectedCenter) return;
    try {
      setSaving(true);
      setError('');
      await callService.assignResponse(call.id, Number(selectedCenter), assignNote);
      const responsesData = await callService.getResponses(call.id);
      setResponses(responsesData);
      setSelectedCenter('');
      setAssignNote('');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Phân công thất bại');
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <p className="text-text-muted">Đang tải...</p>;
  if (!call) return <p className="text-status-high">Không tìm thấy cuộc gọi.</p>;

  const statusLabels: Record<string, string> = {
    active: 'Đang hoạt động',
    in_progress: 'Đang xử lý',
    resolved: 'Đã giải quyết',
    dismissed: 'Đã hủy',
  };

  const gmapsUrl = `https://www.google.com/maps?q=${call.lat},${call.lng}`;

  return (
    <div>
      <button
        onClick={() => router.push('/admin/calls')}
        className="text-text-muted hover:text-text-body text-sm mb-4 transition-colors cursor-pointer"
      >
        &larr; Quay lại danh sách
      </button>

      {error && <p className="text-status-high text-sm mb-4">{error}</p>}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="rounded-section border border-border-default bg-surface-elevated p-6">
            <div className="flex items-center justify-between mb-4">
              <h1 className="text-xl font-bold text-text-primary">Call #{call.id}</h1>
              <span className={`inline-block px-3 py-1 rounded-tag text-xs font-medium ${
                call.status === 'active' ? 'bg-status-high/10 text-status-high' :
                call.status === 'in_progress' ? 'bg-[rgba(255,138,80,0.15)] text-status-medium' :
                call.status === 'resolved' ? 'bg-[rgba(76,175,80,0.15)] text-[#4CAF50]' :
                'bg-surface-card text-text-muted'
              }`}>
                {statusLabels[call.status]}
              </span>
            </div>

            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-text-muted text-xs mb-1">Người dùng</p>
                <p className="text-text-body font-medium">{call.userName}</p>
              </div>
              <div>
                <p className="text-text-muted text-xs mb-1">Loại thiên tai</p>
                <p className="text-text-body">{call.disasterType.icon} {call.disasterType.name}</p>
              </div>
              <div>
                <p className="text-text-muted text-xs mb-1">Vị trí</p>
                <p className="text-text-body">{call.locationName || address || `${call.lat.toFixed(4)}, ${call.lng.toFixed(4)}`}</p>
                <a href={gmapsUrl} target="_blank" rel="noopener noreferrer" className="text-primary text-xs hover:underline">
                  Xem trên Google Maps &rarr;
                </a>
              </div>
              <div>
                <p className="text-text-muted text-xs mb-1">Điểm mức độ</p>
                <span className={`inline-block px-2 py-0.5 rounded-tag text-xs font-medium ${
                  call.urgencyScore >= 80 ? 'bg-primary-subtle text-status-high' :
                  call.urgencyScore >= 60 ? 'bg-[rgba(255,138,80,0.15)] text-status-medium' :
                  'bg-[rgba(212,168,75,0.15)] text-status-low'
                }`}>
                  {call.urgencyScore}
                </span>
              </div>
              <div className="col-span-2">
                <p className="text-text-muted text-xs mb-1">Mô tả</p>
                <p className="text-text-body">{call.description}</p>
              </div>
              {call.imageUrl && (
                <div className="col-span-2">
                  <p className="text-text-muted text-xs mb-1">Hình ảnh</p>
                  <a
                    href={call.imageUrl.startsWith('http') ? call.imageUrl : `http://localhost:8080${call.imageUrl}`}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <img
                      src={call.imageUrl.startsWith('http') ? call.imageUrl : `http://localhost:8080${call.imageUrl}`}
                      alt="Call image"
                      className="w-full max-h-64 object-cover rounded-section border border-border-default hover:opacity-90 transition-opacity"
                    />
                  </a>
                </div>
              )}
              {call.suggestedSupplies && call.suggestedSupplies.length > 0 && (
                <div className="col-span-2">
                  <p className="text-text-muted text-xs mb-1">Vật tư đề xuất</p>
                  <div className="flex flex-wrap gap-1">
                    {call.suggestedSupplies.map((s, i) => (
                      <span key={i} className="px-2 py-0.5 rounded-tag text-xs bg-surface-card text-text-muted border border-border-default">
                        {s}
                      </span>
                    ))}
                  </div>
                </div>
              )}
              <div>
                <p className="text-text-muted text-xs mb-1">Thời gian tạo</p>
                <p className="text-text-body">{new Date(call.createdAt).toLocaleString()}</p>
              </div>
              {call.resolvedAt && (
                <div>
                  <p className="text-text-muted text-xs mb-1">Thời gian giải quyết</p>
                  <p className="text-text-body">{new Date(call.resolvedAt).toLocaleString()}</p>
                </div>
              )}
            </div>
          </div>

          {call.status !== 'resolved' && (
            <div className="rounded-section border border-border-default bg-surface-elevated p-6">
              <h2 className="text-lg font-bold text-text-primary mb-4">Cập nhật trạng thái</h2>
              <div className="flex gap-3">
                {call.status === 'active' && (
                  <button
                    onClick={() => handleStatusUpdate('in_progress')}
                    className="px-4 py-2 rounded-pill bg-[rgba(255,138,80,0.15)] text-status-medium text-sm font-medium hover:opacity-90 transition-opacity cursor-pointer"
                  >
                    Đánh dấu đang xử lý
                  </button>
                )}
                {call.status === 'in_progress' && (
                  <button
                    onClick={() => handleStatusUpdate('resolved')}
                    className="px-4 py-2 rounded-pill bg-[rgba(76,175,80,0.15)] text-[#4CAF50] text-sm font-medium hover:opacity-90 transition-opacity cursor-pointer"
                  >
                    Đánh dấu đã giải quyết
                  </button>
                )}
                <button
                  onClick={() => handleStatusUpdate('dismissed')}
                  className="px-4 py-2 rounded-pill bg-surface-card text-text-muted text-sm font-medium border border-border-default hover:opacity-90 transition-opacity cursor-pointer"
                >
                  Hủy bỏ
                </button>
              </div>
            </div>
          )}

          {call.status !== 'resolved' && (
            <div className="rounded-section border border-border-default bg-surface-elevated p-6">
              <h2 className="text-lg font-bold text-text-primary mb-4">Phân công trung tâm cứu hộ</h2>
              <form onSubmit={handleAssign} className="space-y-4">
                <div>
                  <label className="block text-xs text-text-muted mb-1">Trung tâm cứu hộ</label>
                  <select
                    value={selectedCenter}
                    onChange={e => setSelectedCenter(e.target.value)}
                    required
                    className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
                  >
                    <option value="">Chọn trung tâm...</option>
                    {centers.map(c => (
                      <option key={c.id} value={c.id}>{c.name} ({c.type.replace('_', ' ')})</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs text-text-muted mb-1">Ghi chú (không bắt buộc)</label>
                  <textarea
                    value={assignNote}
                    onChange={e => setAssignNote(e.target.value)}
                    rows={2}
                    className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors resize-none"
                    placeholder="Ghi chú phân công..."
                  />
                </div>
                <button
                  type="submit"
                  disabled={saving || !selectedCenter}
                  className="px-4 py-2 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 disabled:opacity-50 transition-opacity cursor-pointer"
                >
                  {saving ? 'Đang phân công...' : 'Phân công'}
                </button>
              </form>
            </div>
          )}
        </div>

        <div className="space-y-6">
          <div className="rounded-section border border-border-default bg-surface-elevated overflow-hidden">
            <MapView center={[call.lat, call.lng]} zoom={14} className="h-[220px] w-full">
              <DistressCallMarker call={call} />
              {centers.map(c => <RescueCenterMarker key={c.id} center={c} />)}
            </MapView>
          </div>

          <ChatPanel callId={call.id} />

          <div className="rounded-section border border-border-default bg-surface-elevated p-6">
            <h2 className="text-lg font-bold text-text-primary mb-4">Lịch sử phản hồi</h2>
            {responses.length === 0 ? (
              <p className="text-text-muted text-sm">Chưa có phản hồi nào.</p>
            ) : (
              <div className="space-y-3">
                {responses.map((r: any) => (
                  <div key={r.id} className="bg-surface-card rounded-pill p-3 text-sm">
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-medium text-text-body">{r.rescueCenterName}</span>
                      <span className={`px-2 py-0.5 rounded-tag text-xs font-medium ${
                        r.status === 'assigned' ? 'bg-[rgba(255,138,80,0.15)] text-status-medium' :
                        r.status === 'in_progress' ? 'bg-primary-subtle text-status-high' :
                        'bg-[rgba(76,175,80,0.15)] text-[#4CAF50]'
                      }`}>
                        {r.status.replace('_', ' ')}
                      </span>
                    </div>
                    {r.note && <p className="text-text-muted text-xs">{r.note}</p>}
                    {r.rating && (
                      <p className="text-xs mt-1">
                        <span className="text-[#f5a623] inline-flex gap-0.5">
                          {Array.from({ length: 5 }, (_, i) => (
                            <Star key={i} size={14} fill={i < r.rating ? '#f5a623' : 'none'} />
                          ))}
                        </span>
                        <span className="text-text-muted ml-1">{r.rating}/5</span>
                      </p>
                    )}
                    {r.feedback && <p className="text-text-muted text-xs italic mt-0.5">"{r.feedback}"</p>}
                    <p className="text-text-muted text-xs mt-1">
                      Assigned by {r.assignedByName} &middot; {new Date(r.createdAt).toLocaleString()}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
