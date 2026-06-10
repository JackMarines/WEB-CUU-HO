'use client';

import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, useMapEvents, useMap } from 'react-leaflet';
import L from 'leaflet';

interface Props {
  center?: [number, number];
  zoom?: number;
  className?: string;
  children?: React.ReactNode;
  onClick?: (latlng: { lat: number; lng: number }) => void;
}

function MapClickHandler({ onClick }: { onClick?: (latlng: { lat: number; lng: number }) => void }) {
  useMapEvents({
    click(e) {
      onClick?.({ lat: e.latlng.lat, lng: e.latlng.lng });
    },
  });
  return null;
}

function MapCenterController({ center, zoom }: { center: [number, number]; zoom: number }) {
  const map = useMap();
  useEffect(() => {
    map.setView(center, zoom);
  }, [map, center, zoom]);
  return null;
}

export default function MapView({
  center = [16.0, 108.0],
  zoom = 6,
  className = 'h-full w-full',
  children,
  onClick,
}: Props) {
  const [mounted, setMounted] = useState(false);
  useEffect(() => { setMounted(true); }, []);
  if (!mounted) {
    return <div className={className} style={{ background: '#e8e8e8' }} />;
  }

  return (
    <div className="relative h-full">
      <MapContainer
        center={center}
        zoom={zoom}
        className={className}
        zoomControl={false}
        attributionControl={false}
        minZoom={5}
        maxBounds={L.latLngBounds([5.0, 100.0], [25.0, 112.0])}
        maxBoundsViscosity={1.0}
        style={{ background: '#e8e8e8' }}
      >
        <TileLayer
          attribution=""
          noWrap={true}
          url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
        />
        <MapClickHandler onClick={onClick} />
        <MapCenterController center={center} zoom={zoom} />
        {children}
      </MapContainer>
      <div className="absolute bottom-3 left-1/2 -translate-x-1/2 z-[1000]">
        <div className="px-3 py-1.5 rounded-pill text-xs text-white"
             style={{ background: 'rgba(0,0,0,0.7)' }}>
          {center[0].toFixed(2)}, {center[1].toFixed(2)}
        </div>
      </div>
    </div>
  );
}
