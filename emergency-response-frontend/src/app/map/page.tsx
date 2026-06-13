'use client';

import { useState, useEffect } from 'react';
import dynamic from 'next/dynamic';
import { callService } from '@/services/callService';
import { DistressCall } from '@/types';

const MapView = dynamic(() => import('@/components/map/MapView'), { ssr: false });
const DistressCallMarker = dynamic(() => import('@/components/map/DistressCallMarker'), { ssr: false });

export default function MapPage() {
  const [calls, setCalls] = useState<DistressCall[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    callService.getAll()
      .then(setCalls)
      .catch(() => setError('Không thể tải dữ liệu bản đồ'));
  }, []);

  return (
    <div className="relative h-full">
      <div className="absolute top-4 left-1/2 -translate-x-1/2 z-[1000]">
        <div className="px-4 py-2 rounded-pill text-sm font-semibold"
             style={{ background: 'rgba(0,0,0,0.7)' }}>
          <span className="text-white">
            Bản đồ cứu trợ &middot; {calls.length} cuộc gọi
          </span>
        </div>
      </div>

      {error && (
        <div className="absolute top-16 left-1/2 -translate-x-1/2 z-[1000]">
          <p className="text-status-high text-xs bg-surface-card px-3 py-1.5 rounded-pill">{error}</p>
        </div>
      )}

      <MapView center={[16.0, 108.0]} zoom={6} className="h-full w-full">
        {calls.map(call => (
          <DistressCallMarker key={call.id} call={call} />
        ))}
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
