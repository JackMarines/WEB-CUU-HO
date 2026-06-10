'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { MapPin } from 'lucide-react';
import dynamic from 'next/dynamic';
import { disasterTypeService } from '@/services/disasterTypeService';
import { callService } from '@/services/callService';
import api from '@/services/api';
import { DisasterType } from '@/types';

const MapView = dynamic(() => import('@/components/map/MapView'), { ssr: false });

interface NominatimResult {
  lat: string;
  lon: string;
  display_name: string;
}

const VIETNAM_BOUNDS = { minLat: 5.0, maxLat: 25.0, minLng: 100.0, maxLng: 112.0 };

function isInVietnam(lat: number, lng: number) {
  return lat >= VIETNAM_BOUNDS.minLat && lat <= VIETNAM_BOUNDS.maxLat
      && lng >= VIETNAM_BOUNDS.minLng && lng <= VIETNAM_BOUNDS.maxLng;
}

export default function SubmitCallPage() {
  const router = useRouter();
  const [disasterTypes, setDisasterTypes] = useState<DisasterType[]>([]);
  const [disasterTypeId, setDisasterTypeId] = useState('');
  const [callerName, setCallerName] = useState('');
  const [callerPhone, setCallerPhone] = useState('');
  const [personCount, setPersonCount] = useState(1);
  const [description, setDescription] = useState('');
  const [locationName, setLocationName] = useState('');
  const [position, setPosition] = useState<[number, number]>([16.0, 108.0]);
  const [mapZoom, setMapZoom] = useState(6);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<NominatimResult[]>([]);
  const [searching, setSearching] = useState(false);
  const [showResults, setShowResults] = useState(false);
  const [geoLoading, setGeoLoading] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string>('');
  const [imageUrl, setImageUrl] = useState('');
  const [uploading, setUploading] = useState(false);

  const timerRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    disasterTypeService.getAll()
      .then(setDisasterTypes)
      .catch(() => setError('Failed to load disaster types'));
  }, []);

  useEffect(() => {
    if (!searchQuery.trim()) {
      setSearchResults([]);
      setShowResults(false);
      return;
    }
    setSearching(true);
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(async () => {
      try {
        const res = await fetch(
          `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(searchQuery)}&format=json&limit=5&accept-language=vi`,
          { headers: { 'User-Agent': 'EmergencyResponse/1.0' } }
        );
        if (!res.ok) return;
        const data = await res.json();
        setSearchResults(data);
        setShowResults(data.length > 0);
      } catch {
        // network error
      } finally {
        setSearching(false);
      }
    }, 300);
    return () => clearTimeout(timerRef.current);
  }, [searchQuery]);

  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setShowResults(false);
      }
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  async function reverseGeocode(lat: number, lng: number): Promise<string> {
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&accept-language=vi`,
        { headers: { 'User-Agent': 'EmergencyResponse/1.0' } }
      );
      if (res.ok) {
        const data = await res.json();
        return data.display_name || `${lat.toFixed(4)}, ${lng.toFixed(4)}`;
      }
    } catch {
      // ignore
    }
    return `${lat.toFixed(4)}, ${lng.toFixed(4)}`;
  }

  async function handleMapClick(e: { lat: number; lng: number }) {
    if (!isInVietnam(e.lat, e.lng)) {
      setError('Vị trí phải nằm trong phạm vi Việt Nam');
      return;
    }
    setError('');
    setPosition([e.lat, e.lng]);
    setMapZoom(14);
    const name = await reverseGeocode(e.lat, e.lng);
    setLocationName(name);
    setSearchQuery(name);
  }

  function selectResult(r: NominatimResult) {
    const lat = parseFloat(r.lat);
    const lng = parseFloat(r.lon);
    if (!isInVietnam(lat, lng)) {
      setError('Vị trí phải nằm trong phạm vi Việt Nam');
      return;
    }
    setError('');
    setPosition([lat, lng]);
    setMapZoom(14);
    setLocationName(r.display_name);
    setSearchQuery(r.display_name);
    setShowResults(false);
  }

  async function handleGeolocate() {
    if (!navigator.geolocation) {
      setError('Trình duyệt không hỗ trợ định vị');
      return;
    }
    setGeoLoading(true);
    setError('');
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const { latitude: lat, longitude: lng } = pos.coords;
        setPosition([lat, lng]);
        setMapZoom(15);
        const name = await reverseGeocode(lat, lng);
        setLocationName(name);
        setSearchQuery(name);
        setGeoLoading(false);
      },
      (err) => {
        setGeoLoading(false);
        if (err.code === err.PERMISSION_DENIED) {
          setError('Vui lòng cho phép truy cập vị trí');
        } else {
          setError('Không thể xác định vị trí');
        }
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  }

  async function handleFileSelect(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setSelectedFile(file);
    setImagePreview(URL.createObjectURL(file));
    setUploading(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await api.post('/upload', formData);
      setImageUrl(res.data.url);
    } catch {
      setError('Tải ảnh thất bại');
      setSelectedFile(null);
      setImagePreview('');
    } finally {
      setUploading(false);
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!disasterTypeId || !description.trim() || !callerName.trim()) return;
    if (uploading) return;
    if (!isInVietnam(position[0], position[1])) {
      setError('Vị trí phải nằm trong phạm vi Việt Nam');
      return;
    }
    setSaving(true);
    setError('');
    try {
      await callService.create({
        disasterTypeId: Number(disasterTypeId),
        lat: position[0],
        lng: position[1],
        locationName: locationName || searchQuery || null,
        description: description.trim(),
        imageUrl: imageUrl || null,
        callerName: callerName.trim(),
        callerPhone: callerPhone.trim() || null,
        personCount,
      });
      router.push('/');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to submit call');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-xl font-bold text-text-primary mb-6">Gửi yêu cầu cứu trợ</h1>

      {error && <p className="text-status-high text-sm mb-4">{error}</p>}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="block text-xs text-text-muted mb-1.5 font-semibold uppercase tracking-wider">
            Vị trí trên bản đồ
          </label>
          <div className="rounded-section overflow-hidden border border-border-default">
            <MapView
              center={position}
              zoom={mapZoom}
              className="h-[300px] w-full"
              onClick={handleMapClick}
            />
          </div>
          <p className="text-xs text-text-muted mt-1.5">
            Tọa độ: {position[0].toFixed(4)}, {position[1].toFixed(4)}
          </p>
        </div>

        <div ref={dropdownRef} className="relative">
          <label className="block text-xs text-text-muted mb-1.5 font-semibold uppercase tracking-wider">
            Tìm kiếm địa điểm
          </label>
          <div className="flex gap-2">
            <input
              type="text"
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              onFocus={() => searchResults.length > 0 && setShowResults(true)}
              placeholder="Nhập địa điểm cần tìm..."
              className="flex-1 px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
            />
            <button
              type="button"
              onClick={handleGeolocate}
              disabled={geoLoading}
              className="px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm hover:border-primary transition-colors disabled:opacity-50 cursor-pointer whitespace-nowrap"
            >
              {geoLoading ? '...' : <><MapPin className="inline mr-1" size={16} />Vị trí hiện tại</>}
            </button>
          </div>
          {showResults && (
            <div className="absolute z-[2000] w-full mt-1 rounded-section bg-surface-elevated border border-border-default shadow-lg max-h-60 overflow-y-auto">
              {searching ? (
                <div className="px-3 py-2 text-text-muted text-sm">Đang tìm kiếm...</div>
              ) : (
                searchResults.map((r, i) => (
                  <button
                    key={i}
                    type="button"
                    onClick={() => selectResult(r)}
                    className="w-full text-left px-3 py-2 text-text-body text-sm hover:bg-surface-card transition-colors border-b border-border-default last:border-b-0 cursor-pointer"
                  >
                    {r.display_name}
                  </button>
                ))
              )}
            </div>
          )}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs text-text-muted mb-1.5 font-semibold uppercase tracking-wider">
              Họ tên
            </label>
            <input
              type="text"
              value={callerName}
              onChange={e => setCallerName(e.target.value)}
              required
              placeholder="Nguyễn Văn A"
              className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
            />
          </div>
          <div>
            <label className="block text-xs text-text-muted mb-1.5 font-semibold uppercase tracking-wider">
              Số điện thoại
            </label>
            <input
              type="tel"
              value={callerPhone}
              onChange={e => setCallerPhone(e.target.value)}
              placeholder="0901234567"
              className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs text-text-muted mb-1.5 font-semibold uppercase tracking-wider">
            Loại thiên tai
          </label>
          <select
            value={disasterTypeId}
            onChange={e => setDisasterTypeId(e.target.value)}
            required
            className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
          >
            <option value="">Chọn loại thiên tai...</option>
            {disasterTypes.map(dt => (
              <option key={dt.id} value={dt.id}>{dt.icon} {dt.name}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-xs text-text-muted mb-1.5 font-semibold uppercase tracking-wider">
            Số người cần cứu trợ
          </label>
          <input
            type="number" min={1} value={personCount}
            onChange={e => setPersonCount(Math.max(1, parseInt(e.target.value) || 1))}
            className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors"
          />
        </div>

        <div>
          <label className="block text-xs text-text-muted mb-1.5 font-semibold uppercase tracking-wider">
            Mô tả tình hình
          </label>
          <textarea
            value={description}
            onChange={e => setDescription(e.target.value)}
            required
            rows={4}
            placeholder="Mô tả tình hình khẩn cấp, số người cần cứu trợ..."
            className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors resize-none"
          />
        </div>

        <div>
          <label className="block text-xs text-text-muted mb-1.5 font-semibold uppercase tracking-wider">
            Hình ảnh (không bắt buộc)
          </label>
          <div className="flex items-center gap-3">
            <label className="px-4 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm hover:border-primary transition-colors cursor-pointer">
              {uploading ? 'Đang tải...' : 'Chọn ảnh'}
              <input
                type="file"
                accept="image/*"
                onChange={handleFileSelect}
                disabled={uploading}
                className="hidden"
              />
            </label>
            {selectedFile && (
              <span className="text-text-muted text-xs truncate max-w-[200px]">
                {selectedFile.name}
              </span>
            )}
          </div>
          {imagePreview && (
            <img
              src={imagePreview}
              alt="Preview"
              className="mt-2 w-full max-h-48 object-cover rounded-section border border-border-default"
            />
          )}
        </div>

        <button
          type="submit"
          disabled={saving || uploading || !disasterTypeId || !description.trim() || !callerName.trim()}
          className="w-full px-4 py-3 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 disabled:opacity-50 transition-opacity cursor-pointer"
        >
          {saving ? 'Đang gửi...' : 'Gửi yêu cầu cứu trợ'}
        </button>
      </form>
    </div>
  );
}
