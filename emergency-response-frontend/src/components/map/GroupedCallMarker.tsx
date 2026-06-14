'use client';

import { useState } from 'react';
import { Marker, Popup } from 'react-leaflet';
import { DistressCall } from '@/types';
import { Clock, MapPin, Users, Phone, User, ArrowLeft } from 'lucide-react';
import L from 'leaflet';

function getRelativeTime(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'Vừa xong';
  if (mins < 60) return `${mins} phút trước`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours} giờ trước`;
  const days = Math.floor(hours / 24);
  return `${days} ngày trước`;
}

function getGroupColor(calls: DistressCall[]): string {
  const maxUrgency = Math.max(...calls.map(c => c.urgencyScore));
  const allResolved = calls.every(c => c.status === 'resolved');
  if (allResolved) return '#66BB6A';
  if (maxUrgency >= 80) return '#f86e64';
  if (maxUrgency >= 60) return '#FF8A50';
  return '#D4A84B';
}

function createGroupIcon(color: string, count: number, maxUrgency: number) {
  const shouldPulse = maxUrgency >= 60;
  const dot = `<div style="background:${color};width:32px;height:32px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:800;color:#fff;box-shadow:0 2px 8px rgba(0,0,0,0.3);border:2px solid #fff;position:relative;z-index:1;">+${count}</div>`;
  const ring = shouldPulse ? '<div class="marker-pulse-ring"></div>' : '';
  return L.divIcon({
    className: '',
    html: `<div style="position:relative;width:32px;height:32px;">${ring}${dot}</div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 16],
  });
}

const statusClassMap: Record<string, string> = {
  active: 'bg-primary-subtle text-status-high',
  in_progress: 'bg-[rgba(255,138,80,0.15)] text-status-medium',
  resolved: 'bg-[rgba(76,175,80,0.15)] text-status-resolved',
  dismissed: 'bg-[rgba(255,255,255,0.08)] text-text-muted',
};

const statusLabels: Record<string, string> = {
  active: 'Đang hoạt động',
  in_progress: 'Đang xử lý',
  resolved: 'Đã giải quyết',
  dismissed: 'Đã huỷ',
};

function CollapsedRow({ call, onSelect }: { call: DistressCall; onSelect: () => void }) {
  return (
    <div
      className="flex items-start gap-2 p-2 rounded-md bg-surface-card hover:bg-surface-section transition-colors cursor-pointer"
      onClick={onSelect}
    >
      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-1.5 flex-wrap">
          <span className="px-1.5 py-0.5 rounded-tag text-[10px] font-semibold bg-primary-subtle text-status-high">
            {call.urgencyScore}
          </span>
          <span className={`px-1.5 py-0.5 rounded-tag text-[10px] font-semibold uppercase tracking-wider ${statusClassMap[call.status]}`}>
            {statusLabels[call.status]}
          </span>
          {call.callerName && (
            <span className="text-text-muted text-[10px] truncate max-w-[100px]">{call.callerName}</span>
          )}
        </div>
        <p className="text-text-body text-xs leading-relaxed mt-0.5 line-clamp-1">{call.description}</p>
        <div className="flex items-center gap-2 text-text-muted text-[10px] mt-0.5">
          <span className="flex items-center gap-0.5"><Clock size={10} />{getRelativeTime(call.createdAt)}</span>
          <span className="flex items-center gap-0.5"><Users size={10} />{call.personCount}</span>
        </div>
      </div>
    </div>
  );
}

function DetailModal({ call, onBack }: { call: DistressCall; onBack: () => void }) {
  return (
    <div>
      <button
        onClick={onBack}
        className="flex items-center gap-1 text-text-muted hover:text-text-body transition-colors text-xs mb-3"
      >
        <ArrowLeft size={14} />
        Quay lại
      </button>

      <div className="flex items-center justify-between gap-2 mb-3">
        <span className="font-semibold text-text-primary text-sm">{call.disasterType.name}</span>
        <div className="flex items-center gap-1">
          <span className="px-1.5 py-0.5 rounded-tag text-[10px] font-semibold bg-primary-subtle text-status-high">
            {call.urgencyScore}
          </span>
          <span className={`px-1.5 py-0.5 rounded-tag text-[10px] font-semibold uppercase tracking-wider ${statusClassMap[call.status]}`}>
            {statusLabels[call.status]}
          </span>
        </div>
      </div>

      <div className="space-y-1.5 mb-3">
        {call.callerName && (
          <div className="flex items-center gap-1.5 text-text-body text-xs">
            <User size={12} className="text-text-muted shrink-0" />
            <span>{call.callerName}</span>
          </div>
        )}
        {call.callerPhone && (
          <div className="flex items-center gap-1.5 text-text-body text-xs">
            <Phone size={12} className="text-text-muted shrink-0" />
            <span>{call.callerPhone}</span>
          </div>
        )}
        <div className="flex items-start gap-1.5 text-text-body text-xs">
          <MapPin size={12} className="text-text-muted shrink-0 mt-0.5" />
          <span>{call.locationName || `${call.lat.toFixed(4)}, ${call.lng.toFixed(4)}`}</span>
        </div>
      </div>

      <p className="text-text-body text-xs leading-relaxed mb-3">{call.description}</p>

      <div className="flex items-center gap-3 text-text-muted text-[10px]">
        <span className="flex items-center gap-1"><Clock size={11} />{getRelativeTime(call.createdAt)}</span>
        <span className="flex items-center gap-1"><Users size={11} />{call.personCount} người</span>
      </div>
    </div>
  );
}

export default function GroupedCallMarker({ calls }: { calls: DistressCall[] }) {
  const [expandedCall, setExpandedCall] = useState<DistressCall | null>(null);
  const color = getGroupColor(calls);
  const maxUrgency = Math.max(...calls.map(c => c.urgencyScore));
  const first = calls[0];

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const markerExtra = { urgencyScore: maxUrgency, status: 'active' } as any;

  return (
    <Marker
      position={[first.lat, first.lng]}
      icon={createGroupIcon(color, calls.length, maxUrgency)}
      {...markerExtra}
    >
      <Popup>
        <div className="font-sans text-sm min-w-[260px]">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-1.5">
              <MapPin size={14} className="text-text-muted shrink-0" />
              <span className="font-semibold text-text-primary text-sm truncate max-w-[180px]">
                {first.locationName || `${first.lat.toFixed(4)}, ${first.lng.toFixed(4)}`}
              </span>
            </div>
            <span className="px-2 py-0.5 rounded-tag text-[10px] font-semibold bg-surface-card text-text-muted">
              {calls.length} cuộc gọi
            </span>
          </div>

          <div className={`max-h-[240px] overflow-y-auto space-y-1.5 ${expandedCall ? 'hidden' : ''}`}>
            {calls.map(call => (
              <CollapsedRow
                key={call.id}
                call={call}
                onSelect={() => setExpandedCall(call)}
              />
            ))}
          </div>
          <div className={expandedCall ? '' : 'hidden'}>
            <DetailModal call={expandedCall ?? calls[0]} onBack={() => setExpandedCall(null)} />
          </div>
        </div>
      </Popup>
    </Marker>
  );
}
