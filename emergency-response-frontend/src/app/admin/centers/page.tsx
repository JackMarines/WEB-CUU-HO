'use client';

import { useState, useEffect, useCallback, useRef } from 'react';
import { MapPin } from 'lucide-react';
import { centerService } from '@/services/centerService';
import { RescueCenter } from '@/types';

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

const centerTypes = ['shelter', 'supply_distribution', 'rescue_team'] as const;
const typeLabels: Record<string, string> = {
  shelter: 'Nơi trú ẩn',
  supply_distribution: 'Phân phối nhu yếu phẩm',
  rescue_team: 'Đội cứu hộ',
};

type FormData = {
  name: string;
  type: string;
  lat: number;
  lng: number;
  address: string;
  phone: string;
  capacity: number | null;
  supplies: string;
};

const emptyForm: FormData = {
  name: '', type: 'shelter', lat: 16, lng: 108, address: '', phone: '',
  capacity: null, supplies: '',
};

export default function AdminCentersPage() {
  const [centers, setCenters] = useState<RescueCenter[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('');
  const [modal, setModal] = useState<{ open: boolean; edit?: RescueCenter }>({ open: false });
  const [deleteTarget, setDeleteTarget] = useState<RescueCenter | null>(null);
  const [form, setForm] = useState<FormData>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<NominatimResult[]>([]);
  const [searching, setSearching] = useState(false);
  const [showResults, setShowResults] = useState(false);
  const [geoLoading, setGeoLoading] = useState(false);

  const timerRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const loadCenters = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const data = await centerService.getAll(filter || undefined);
      setCenters(data);
    } catch {
      setError('Không thể tải danh sách trung tâm');
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => { loadCenters(); }, [loadCenters]);

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

  function openCreate() {
    setForm(emptyForm);
    setModal({ open: true });
    setError('');
  }

  function openEdit(c: RescueCenter) {
    setForm({
      name: c.name,
      type: c.type,
      lat: c.lat,
      lng: c.lng,
      address: c.address,
      phone: c.phone,
      capacity: c.capacity,
      supplies: typeof c.supplies === 'object' ? JSON.stringify(c.supplies, null, 2) : '',
    });
    setModal({ open: true, edit: c });
    setError('');
  }

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

  function selectResult(r: NominatimResult) {
    const lat = parseFloat(r.lat);
    const lng = parseFloat(r.lon);
    if (!isInVietnam(lat, lng)) {
      setError('Vị trí phải nằm trong phạm vi Việt Nam');
      return;
    }
    setError('');
    setForm(f => ({ ...f, lat, lng, address: r.display_name }));
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
        const address = await reverseGeocode(lat, lng);
        setForm(f => ({ ...f, lat, lng, address }));
        setSearchQuery(address);
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

  async function handleSave(e: React.FormEvent) {
    e.preventDefault();
    try {
      setSaving(true);
      setError('');
      const data = {
        ...form,
        supplies: form.supplies || null,
      };
      if (modal.edit) {
        await centerService.update(modal.edit.id, data);
      } else {
        await centerService.create(data);
      }
      setModal({ open: false });
      await loadCenters();
    } catch (err: any) {
      setError(err.response?.data?.error || 'Lưu thất bại');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      setError('');
      await centerService.delete(deleteTarget.id);
      setDeleteTarget(null);
      await loadCenters();
    } catch (err: any) {
      setError(err.response?.data?.error || 'Xóa thất bại');
      setDeleteTarget(null);
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-text-primary">Trung tâm cứu hộ</h1>
        <button onClick={openCreate} className="px-4 py-2 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 transition-opacity cursor-pointer">
          + Thêm trung tâm
        </button>
      </div>

      {error && !modal.open && (
        <p className="text-status-high text-sm mb-4">{error}</p>
      )}

      <div className="flex gap-1.5 mb-4 flex-wrap">
        {['', ...centerTypes].map(t => (
          <button
            key={t}
            onClick={() => setFilter(t)}
            className={`text-[10px] font-semibold tracking-wider uppercase px-2.5 py-1 rounded-pill transition-colors cursor-pointer ${
              filter === t ? 'bg-status-high text-surface-bg' : 'bg-surface-card text-text-muted hover:text-text-body'
            }`}
          >
              {t === '' ? 'Tất cả' : typeLabels[t]}
          </button>
        ))}
      </div>

      {loading ? (
        <p className="text-text-muted">Đang tải...</p>
      ) : centers.length === 0 ? (
        <p className="text-text-muted">Không tìm thấy trung tâm cứu hộ nào.</p>
      ) : (
        <div className="overflow-x-auto rounded-section border border-border-default">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-surface-section text-text-muted uppercase text-xs tracking-wider">
                <th className="text-left px-4 py-3 font-medium">Tên</th>
                <th className="text-left px-4 py-3 font-medium">Loại</th>
                <th className="text-left px-4 py-3 font-medium">Địa chỉ</th>
                <th className="text-left px-4 py-3 font-medium">SĐT</th>
                <th className="text-left px-4 py-3 font-medium">Sức chứa</th>
                <th className="text-right px-4 py-3 font-medium">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-default">
              {centers.map(c => (
                <tr key={c.id} className="hover:bg-surface-card transition-colors">
                  <td className="px-4 py-3 text-text-body font-medium">{c.name}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-tag text-xs font-medium ${
                      c.type === 'rescue_team' ? 'bg-primary-subtle text-status-high' :
                      c.type === 'supply_distribution' ? 'bg-[rgba(255,138,80,0.15)] text-status-medium' :
                      'bg-surface-card text-text-muted'
                    }`}>
                      {typeLabels[c.type]}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-text-muted max-w-[200px] truncate">{c.address}</td>
                  <td className="px-4 py-3 text-text-muted">{c.phone}</td>
                  <td className="px-4 py-3 text-text-body">{c.capacity ?? '—'}</td>
                  <td className="px-4 py-3 text-right">
                    <button onClick={() => openEdit(c)} className="text-text-muted hover:text-text-body mr-3 transition-colors cursor-pointer" title="Sửa">
                      <svg className="w-4 h-4 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                    </button>
                    <button onClick={() => setDeleteTarget(c)} className="text-text-muted hover:text-status-high transition-colors cursor-pointer" title="Xóa">
                      <svg className="w-4 h-4 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modal.open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
          <div className="bg-surface-elevated rounded-section p-6 w-full max-w-lg border border-border-default max-h-[90vh] overflow-y-auto">
            <h2 className="text-lg font-bold text-text-primary mb-4">
              {modal.edit ? 'Chỉnh sửa trung tâm' : 'Thêm trung tâm cứu hộ'}
            </h2>

            {error && <p className="text-status-high text-sm mb-3">{error}</p>}

            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-xs text-text-muted mb-1">Tên</label>
                <input value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required
                  className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
                  placeholder="Tên trung tâm" />
              </div>
              <div>
                <label className="block text-xs text-text-muted mb-1">Loại</label>
                <select value={form.type} onChange={e => setForm(f => ({ ...f, type: e.target.value }))} required
                  className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors">
                  {centerTypes.map(t => (
                    <option key={t} value={t}>{typeLabels[t]}</option>
                  ))}
                </select>
              </div>
              <div ref={dropdownRef} className="relative">
                <label className="block text-xs text-text-muted mb-1">Tìm kiếm địa điểm</label>
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={e => setSearchQuery(e.target.value)}
                    onFocus={() => searchResults.length > 0 && setShowResults(true)}
                    placeholder="Nhập địa chỉ..."
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
                  <label className="block text-xs text-text-muted mb-1">Vĩ độ</label>
                  <input type="number" step="any" value={form.lat} onChange={e => setForm(f => ({ ...f, lat: parseFloat(e.target.value) || 0 }))} required
                    className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors" />
                </div>
                <div>
                  <label className="block text-xs text-text-muted mb-1">Kinh độ</label>
                  <input type="number" step="any" value={form.lng} onChange={e => setForm(f => ({ ...f, lng: parseFloat(e.target.value) || 0 }))} required
                    className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors" />
                </div>
              </div>
              <div>
                <label className="block text-xs text-text-muted mb-1">Địa chỉ</label>
                <input value={form.address} readOnly
                  className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
                  placeholder="Địa chỉ (tự động từ tìm kiếm)" />
              </div>
              <div>
                <label className="block text-xs text-text-muted mb-1">Số điện thoại</label>
                <input value={form.phone} onChange={e => setForm(f => ({ ...f, phone: e.target.value }))}
                  className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
                  placeholder="Số điện thoại" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs text-text-muted mb-1">Sức chứa</label>
                  <input type="number" value={form.capacity ?? ''} onChange={e => setForm(f => ({ ...f, capacity: e.target.value ? parseInt(e.target.value) : null }))}
                    className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm focus:outline-none focus:border-primary transition-colors" />
                </div>
              </div>
              <div>
                <label className="block text-xs text-text-muted mb-1">Vật tư (JSON)</label>
                <textarea value={form.supplies} onChange={e => setForm(f => ({ ...f, supplies: e.target.value }))} rows={3}
                  className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors resize-none font-mono"
                  placeholder='{"rice": 100, "water": 200}' />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setModal({ open: false })}
                  className="px-4 py-2 rounded-pill border border-border-default text-text-muted text-sm hover:bg-surface-card transition-colors cursor-pointer">
                  Hủy
                </button>
                <button type="submit" disabled={saving}
                  className="px-4 py-2 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 disabled:opacity-50 transition-opacity cursor-pointer">
                  {saving ? 'Đang lưu...' : modal.edit ? 'Cập nhật' : 'Tạo'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
          <div className="bg-surface-elevated rounded-section p-6 w-full max-w-sm border border-border-default">
            <h2 className="text-lg font-bold text-text-primary mb-2">Xóa trung tâm</h2>
            <p className="text-text-muted text-sm mb-1">Bạn có chắc muốn xóa</p>
            <p className="text-text-body font-medium mb-4">&ldquo;{deleteTarget.name}&rdquo;?</p>
            {error && <p className="text-status-high text-sm mb-3">{error}</p>}
            <div className="flex justify-end gap-3">
              <button onClick={() => { setDeleteTarget(null); setError(''); }}
                className="px-4 py-2 rounded-pill border border-border-default text-text-muted text-sm hover:bg-surface-card transition-colors cursor-pointer">
                Hủy
              </button>
              <button onClick={handleDelete}
                className="px-4 py-2 rounded-pill bg-status-high text-white text-sm font-medium hover:opacity-90 transition-opacity cursor-pointer">
                Xóa
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
