'use client';

import { Marker, Popup } from 'react-leaflet';
import { DistressCall } from '@/types';
import { Users, Clock, MapPin, Phone, User } from 'lucide-react';
import L from 'leaflet';

function getUrgencyColor(call: DistressCall): string {
  if (call.status === 'resolved') return '#66BB6A';
  if (call.urgencyScore >= 80) return '#f86e64';
  if (call.urgencyScore >= 60) return '#FF8A50';
  return '#D4A84B';
}

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

function createMarkerIcon(color: string, score: number, status: string) {
  const shouldPulse = status !== 'resolved' && status !== 'dismissed' && score >= 60;
  const dot = `<div style="background:${color};width:32px;height:32px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:800;color:#fff;box-shadow:0 2px 8px rgba(0,0,0,0.3);border:2px solid #fff;position:relative;z-index:1;">${score}</div>`;
  const ring = shouldPulse ? '<div class="marker-pulse-ring"></div>' : '';
  return L.divIcon({
    className: '',
    html: `<div style="position:relative;width:32px;height:32px;">${ring}${dot}</div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 16],
  });
}

const statusLabels: Record<string, string> = {
  active: 'Đang hoạt động',
  in_progress: 'Đang xử lý',
  resolved: 'Đã giải quyết',
  dismissed: 'Đã huỷ',
};

export default function DistressCallMarker({ call }: { call: DistressCall }) {
  const color = getUrgencyColor(call);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const markerExtra = { urgencyScore: call.urgencyScore, status: call.status } as any;
  return (
    <Marker
      position={[call.lat, call.lng]}
      icon={createMarkerIcon(color, call.urgencyScore, call.status)}
      {...markerExtra}
    >
      <Popup>
        <div className="font-sans text-sm min-w-[220px]">
          <div className="flex items-center justify-between mb-1.5">
            <div className="flex items-center gap-1.5">
              <span className="text-base">{call.disasterType.icon}</span>
              <span className="font-semibold text-text-primary text-sm">{call.disasterType.name}</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className={`px-2 py-0.5 rounded-tag text-[10px] font-semibold transition-all duration-200 ease-out ${
                call.urgencyScore >= 80 ? 'bg-primary-subtle text-status-high' :
                call.urgencyScore >= 60 ? 'bg-[rgba(255,138,80,0.15)] text-status-medium' :
                'bg-[rgba(212,168,75,0.15)] text-status-low'
              }`}>{call.urgencyScore}</span>
              <span className={`px-2 py-0.5 rounded-tag text-[10px] font-semibold uppercase tracking-wider transition-all duration-200 ease-out ${
                call.status === 'active' ? 'bg-primary-subtle text-status-high' :
                call.status === 'in_progress' ? 'bg-[rgba(255,138,80,0.15)] text-status-medium' :
                call.status === 'resolved' ? 'bg-[rgba(76,175,80,0.15)] text-status-resolved' :
                'bg-[rgba(255,255,255,0.08)] text-text-muted'
              }`}>{statusLabels[call.status]}</span>
            </div>
          </div>

          <div className="text-text-muted text-[11px] mb-0.5">
            <span className="flex items-center gap-0.5"><Clock size={12} />{getRelativeTime(call.createdAt)}</span>
          </div>
          <div className="text-text-muted text-[11px] mb-1.5">
            <span className="flex items-center gap-0.5"><MapPin size={12} /><span className="truncate max-w-[200px]">{call.locationName || `${call.lat.toFixed(2)}, ${call.lng.toFixed(2)}`}</span></span>
          </div>

          <div className="flex items-center gap-1.5 mb-2">
            <Users size={12} className="text-status-high" />
            <span className="text-xs font-semibold text-text-primary">{call.personCount} người cần cứu trợ</span>
          </div>

          <p className="text-text-body text-xs leading-relaxed mb-2 line-clamp-2">{call.description}</p>

          {(call.callerName || call.callerPhone) && (
            <div className="flex items-center gap-2 text-text-muted text-[11px] mb-2">
              {call.callerName && <span className="flex items-center gap-0.5"><User size={12} />{call.callerName}</span>}
              {call.callerPhone && <span className="flex items-center gap-0.5"><Phone size={12} />{call.callerPhone}</span>}
            </div>
          )}

          {call.suggestedSupplies?.length > 0 && (
            <div className="mt-2 pt-2 border-t border-border-default">
              <div className="flex flex-wrap gap-1">
                {call.suggestedSupplies.map((s, i) => (
                  <span key={i} className="bg-supplies-bg text-supplies-text text-[10px] font-semibold px-2 py-0.5 rounded-tag">
                    {s}
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      </Popup>
    </Marker>
  );
}
