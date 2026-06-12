'use client';

import { Marker, Popup } from 'react-leaflet';
import { RescueCenter } from '@/types';
import { Home, Package, Truck, Phone } from 'lucide-react';
import L from 'leaflet';

function createCenterIcon() {
  return L.divIcon({
    className: '',
    html: '<div style="background:#f86e64;color:#fff;width:34px;height:34px;border-radius:50%;display:flex;align-items:center;justify-content:center;box-shadow:0 2px 8px rgba(0,0,0,0.2);border:2px solid #fff;"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z"/><path d="M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2"/><path d="M18 9h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-2"/><path d="M10 6h4"/><path d="M10 10h4"/><path d="M10 14h4"/><path d="M10 18h4"/></svg></div>',
    iconSize: [34, 34],
    iconAnchor: [17, 17],
  });
}

const typeIcons: Record<string, React.ReactNode> = {
  shelter: <Home className="inline mr-1" size={14} />,
  supply_distribution: <Package className="inline mr-1" size={14} />,
  rescue_team: <Truck className="inline mr-1" size={14} />,
};

const typeLabels: Record<string, string> = {
  shelter: 'Nhà tạm trú',
  supply_distribution: 'Điểm phát cứu trợ',
  rescue_team: 'Đội cứu hộ',
};

export default function RescueCenterMarker({ center }: { center: RescueCenter }) {
  return (
    <Marker position={[center.lat, center.lng]} icon={createCenterIcon()}>
      <Popup>
        <div className="font-sans text-sm">
          <h3 className="font-bold text-text-primary text-base">{center.name}</h3>
          <p className="text-text-body text-xs">{typeIcons[center.type]}{typeLabels[center.type]}</p>
          <p className="text-text-body mt-1 truncate max-w-[220px]">{center.address}</p>
          <p className="flex items-center gap-1 text-text-body"><Phone size={12} />{center.phone}</p>
        </div>
      </Popup>
    </Marker>
  );
}
