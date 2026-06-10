'use client';

import { useState, useEffect } from 'react';
import dynamic from 'next/dynamic';
import { Phone } from 'lucide-react';
import { centerService } from '@/services/centerService';
import { RescueCenter } from '@/types';

const MapView = dynamic(() => import('@/components/map/MapView'), { ssr: false });
const RescueCenterMarker = dynamic(() => import('@/components/map/RescueCenterMarker'), { ssr: false });

const typeLabels: Record<string, string> = {
  shelter: 'Nhà tạm trú',
  supply_distribution: 'Điểm phát cứu trợ',
  rescue_team: 'Đội cứu hộ',
};

export default function CentersPage() {
  const [centers, setCenters] = useState<RescueCenter[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    centerService.getAll()
      .then(setCenters)
      .catch(() => setError('Không thể tải dữ liệu'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="h-full flex flex-col lg:flex-row overflow-hidden">
      <div className="lg:w-96 p-6 overflow-y-auto border-r border-border-default">
        <h1 className="text-xl font-bold text-text-primary mb-6">Trung tâm cứu trợ</h1>
        {error && <p className="text-status-high text-sm mb-4">{error}</p>}
        {loading ? (
          <p className="text-text-muted">Đang tải...</p>
        ) : centers.length === 0 ? (
          <p className="text-text-muted">Không có trung tâm nào.</p>
        ) : (
          <div className="space-y-3">
            {centers.map(c => (
              <div
                key={c.id}
                className="rounded-section border border-border-default bg-surface-elevated p-4"
              >
                <h3 className="text-text-body font-medium text-sm">{c.name}</h3>
                <p className="text-xs text-text-body mt-1">{typeLabels[c.type]}</p>
                <p className="text-xs text-text-body mt-0.5">{c.address}</p>
                <p className="text-xs text-text-body mt-0.5"><Phone className="inline mr-0.5" size={12} />{c.phone}</p>
                {c.capacity != null && (
                  <p className="text-xs text-text-body mt-0.5">
                    Sức chứa: {c.capacity} người
                  </p>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
      <div className="flex-1 h-full">
        <MapView center={[16.0, 108.0]} zoom={6} className="h-full w-full">
          {centers.map(c => (
            <RescueCenterMarker key={c.id} center={c} />
          ))}
        </MapView>
      </div>
    </div>
  );
}
