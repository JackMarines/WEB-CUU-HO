'use client';

import { useState, useEffect, useMemo } from 'react';
import dynamic from 'next/dynamic';
import L from 'leaflet';
import MarkerClusterGroup from 'react-leaflet-cluster';
import { callService } from '@/services/callService';
import { DistressCall } from '@/types';

const MapView = dynamic(() => import('@/components/map/MapView'), { ssr: false });
const DistressCallMarker = dynamic(() => import('@/components/map/DistressCallMarker'), { ssr: false });
const GroupedCallMarker = dynamic(() => import('@/components/map/GroupedCallMarker'), { ssr: false });

function createClusterIcon(cluster: L.MarkerCluster) {
  const markers = cluster.getAllChildMarkers();
  const maxUrgency = Math.max(0, ...markers.map(m =>
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (m.options as any).urgencyScore ?? 0
  ));
  const allResolved = markers.every(m =>
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (m.options as any).status === 'resolved' || (m.options as any).status === 'dismissed'
  );
  const count = cluster.getChildCount();

  let color: string;
  if (allResolved) color = '#66BB6A';
  else if (maxUrgency >= 80) color = '#f86e64';
  else if (maxUrgency >= 60) color = '#FF8A50';
  else color = '#3A3A3A';

  const size = count < 5 ? 30 : count < 15 ? 40 : 50;
  const fontSize = size <= 30 ? 12 : size <= 40 ? 14 : 16;

  return L.divIcon({
    html: `<div style="width:${size}px;height:${size}px;border-radius:50%;background:${color};display:flex;align-items:center;justify-content:center;font-size:${fontSize}px;font-weight:800;color:#fff;border:2px solid rgba(255,255,255,0.9);box-shadow:0 2px 10px rgba(0,0,0,0.35);">${count}</div>`,
    className: '',
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
  });
}

export default function MapPageContent() {
  const [calls, setCalls] = useState<DistressCall[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    callService.getAll()
      .then(setCalls)
      .catch(() => setError('Không thể tải dữ liệu bản đồ'));
  }, []);

  const activeCalls = useMemo(
    () => calls.filter(c => c.status === 'active' || c.status === 'in_progress'),
    [calls]
  );

  const { uniqueCalls, groupedCalls } = useMemo(() => {
    const coordMap = new Map<string, DistressCall[]>();
    for (const call of activeCalls) {
      const key = `${call.lat.toFixed(6)},${call.lng.toFixed(6)}`;
      if (!coordMap.has(key)) coordMap.set(key, []);
      coordMap.get(key)!.push(call);
    }
    const unique: DistressCall[] = [];
    const grouped: { key: string; calls: DistressCall[] }[] = [];
    for (const [key, callsAtCoord] of coordMap) {
      if (callsAtCoord.length === 1) unique.push(callsAtCoord[0]);
      else grouped.push({ key, calls: callsAtCoord });
    }
    return { uniqueCalls: unique, groupedCalls: grouped };
  }, [activeCalls]);

  return (
    <div className="relative h-full">
      <div className="absolute top-4 left-1/2 -translate-x-1/2 z-[1000]">
        <div className="px-4 py-2 rounded-pill text-sm font-semibold"
             style={{ background: 'rgba(0,0,0,0.7)' }}>
          <span className="text-white">
            Bản đồ cứu trợ &middot; {activeCalls.length} cuộc gọi
          </span>
        </div>
      </div>

      {error && (
        <div className="absolute top-16 left-1/2 -translate-x-1/2 z-[1000]">
          <p className="text-status-high text-xs bg-surface-card px-3 py-1.5 rounded-pill">{error}</p>
        </div>
      )}

      <MapView center={[16.0, 108.0]} zoom={6} className="h-full w-full">
        <MarkerClusterGroup
          chunkedLoading
          spiderfyOnMaxZoom={false}
          showCoverageOnHover={false}
          maxClusterRadius={60}
          disableClusteringAtZoom={13}
          iconCreateFunction={createClusterIcon}
        >
          {uniqueCalls.map(call => (
            <DistressCallMarker key={call.id} call={call} />
          ))}
          {groupedCalls.map(g => (
            <GroupedCallMarker key={g.key} calls={g.calls} />
          ))}
        </MarkerClusterGroup>
      </MapView>

      <div className="absolute top-4 right-4 z-[1000]">
        <div className="rounded-pill px-3 py-1.5 text-[11px] leading-tight"
             style={{ background: 'rgba(0,0,0,0.75)' }}>
          <div className="flex items-center gap-1.5 mb-0.5">
            <span className="inline-block w-2 h-2 rounded-full bg-[#f86e64]" />
            <span className="text-white/90">Nguy cấp</span>
            <span className="text-white/50 ml-auto">80-100</span>
          </div>
          <div className="flex items-center gap-1.5 mb-0.5">
            <span className="inline-block w-2 h-2 rounded-full bg-[#FF8A50]" />
            <span className="text-white/90">Cao</span>
            <span className="text-white/50 ml-auto">60-79</span>
          </div>
          <div className="flex items-center gap-1.5 mb-0.5">
            <span className="inline-block w-2 h-2 rounded-full bg-[#D4A84B]" />
            <span className="text-white/90">Trung bình</span>
            <span className="text-white/50 ml-auto">0-59</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="inline-block w-2 h-2 rounded-full bg-[#66BB6A]" />
            <span className="text-white/90">Đã xử lý</span>
          </div>
        </div>
      </div>
    </div>
  );
}
