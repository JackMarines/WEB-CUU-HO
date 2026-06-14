'use client';

import { useState, useEffect, useCallback } from 'react';
import { disasterTypeService } from '@/services/disasterTypeService';
import { DisasterType } from '@/types';

type FormData = {
  name: string;
  slug: string;
  icon: string;
  baseUrgencyScore: number;
};

const emptyForm: FormData = { name: '', slug: '', icon: '', baseUrgencyScore: 50 };

export default function DisasterTypesPage() {
  const [types, setTypes] = useState<DisasterType[]>([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState<{ open: boolean; edit?: DisasterType }>({ open: false });
  const [deleteTarget, setDeleteTarget] = useState<DisasterType | null>(null);
  const [form, setForm] = useState<FormData>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const fetch = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const data = await disasterTypeService.getAll();
      setTypes(data);
    } catch {
      setError('Không thể tải loại thiên tai');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetch(); }, [fetch]);

  function openCreate() {
    setForm(emptyForm);
    setModal({ open: true });
    setError('');
  }

  function openEdit(t: DisasterType) {
    setForm({ name: t.name, slug: t.slug, icon: t.icon, baseUrgencyScore: t.baseUrgencyScore });
    setModal({ open: true, edit: t });
    setError('');
  }

  async function handleSave(e: React.FormEvent) {
    e.preventDefault();
    try {
      setSaving(true);
      setError('');
      if (modal.edit) {
        await disasterTypeService.update(modal.edit.id, form);
      } else {
        await disasterTypeService.create(form);
      }
      setModal({ open: false });
      await fetch();
    } catch (err: any) {
      setError(err.response?.data?.error || err.response?.data?.errors?.[0]?.message || 'Lưu thất bại');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      setError('');
      await disasterTypeService.delete(deleteTarget.id);
      setDeleteTarget(null);
      await fetch();
    } catch (err: any) {
      setError(err.response?.data?.error || 'Xóa thất bại');
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-text-primary">Loại thiên tai</h1>
        <button onClick={openCreate} className="px-4 py-2 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 transition-opacity cursor-pointer">
          + Thêm loại
        </button>
      </div>

      {error && !modal.open && (
        <p className="text-status-high text-sm mb-4">{error}</p>
      )}

      {loading ? (
        <p className="text-text-muted">Đang tải...</p>
      ) : types.length === 0 ? (
        <p className="text-text-muted">Không tìm thấy loại thiên tai nào.</p>
      ) : (
        <><div className="sm:hidden space-y-3">
          {types.map((t) => (
            <div key={t.id} className="rounded-section border border-border-default bg-surface-elevated p-4">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <span className="text-xl">{t.icon}</span>
                  <span className="text-text-body font-medium text-sm">{t.name}</span>
                </div>
                <div className="flex gap-1">
                  <button onClick={() => openEdit(t)} className="text-text-muted hover:text-text-body transition-colors cursor-pointer min-w-[44px] min-h-[44px] flex items-center justify-center" title="Edit">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                  </button>
                  <button onClick={() => setDeleteTarget(t)} className="text-text-muted hover:text-status-high transition-colors cursor-pointer min-w-[44px] min-h-[44px] flex items-center justify-center" title="Delete">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                  </button>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-text-muted text-xs">{t.slug}</span>
                <span className={`inline-block px-2 py-0.5 rounded-tag text-xs font-medium ${
                  t.baseUrgencyScore >= 80 ? 'bg-primary-subtle text-status-high' :
                  t.baseUrgencyScore >= 60 ? 'bg-[rgba(255,138,80,0.15)] text-status-medium' :
                  'bg-[rgba(212,168,75,0.15)] text-status-low'
                }`}>
                  {t.baseUrgencyScore}
                </span>
              </div>
            </div>
          ))}
        </div>
        <div className="hidden sm:block overflow-x-auto rounded-section border border-border-default">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-surface-section text-text-muted uppercase text-xs tracking-wider">
                <th className="text-left px-4 py-3 font-medium">Biểu tượng</th>
                <th className="text-left px-4 py-3 font-medium">Tên</th>
                <th className="text-left px-4 py-3 font-medium">Slug</th>
                <th className="text-left px-4 py-3 font-medium">Mức độ</th>
                <th className="text-right px-4 py-3 font-medium">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-default">
              {types.map((t) => (
                <tr key={t.id} className="hover:bg-surface-card transition-colors">
                  <td className="px-4 py-3 text-xl">{t.icon}</td>
                  <td className="px-4 py-3 text-text-body font-medium">{t.name}</td>
                  <td className="px-4 py-3 text-text-muted">{t.slug}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-block px-2 py-0.5 rounded-tag text-xs font-medium ${
                      t.baseUrgencyScore >= 80 ? 'bg-primary-subtle text-status-high' :
                      t.baseUrgencyScore >= 60 ? 'bg-[rgba(255,138,80,0.15)] text-status-medium' :
                      'bg-[rgba(212,168,75,0.15)] text-status-low'
                    }`}>
                      {t.baseUrgencyScore}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button onClick={() => openEdit(t)} className="text-text-muted hover:text-text-body mr-3 transition-colors cursor-pointer" title="Edit">
                      <svg className="w-4 h-4 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                    </button>
                    <button onClick={() => setDeleteTarget(t)} className="text-text-muted hover:text-status-high transition-colors cursor-pointer" title="Delete">
                      <svg className="w-4 h-4 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        </>
      )}

      {modal.open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
          <div className="bg-surface-elevated rounded-section p-6 w-full max-w-md border border-border-default">
            <h2 className="text-lg font-bold text-text-primary mb-4">
              {modal.edit ? 'Chỉnh sửa loại thiên tai' : 'Thêm loại thiên tai'}
            </h2>

            {error && <p className="text-status-high text-sm mb-3">{error}</p>}

            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-xs text-text-muted mb-1">Tên</label>
                <input
                  value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                  required className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
                  placeholder="VD: Lũ lụt"
                />
              </div>
              <div>
                <label className="block text-xs text-text-muted mb-1">Slug</label>
                <input
                  value={form.slug} onChange={e => setForm(f => ({ ...f, slug: e.target.value }))}
                  required className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
                  placeholder="VD: lu-lut"
                />
              </div>
              <div>
                <label className="block text-xs text-text-muted mb-1">Biểu tượng (emoji)</label>
                <input
                  value={form.icon} onChange={e => setForm(f => ({ ...f, icon: e.target.value }))}
                  className="w-full px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
                  placeholder="VD: 🌊"
                />
              </div>
              <div>
                <label className="block text-xs text-text-muted mb-1">
                  Điểm mức độ: <span className="text-text-body font-medium">{form.baseUrgencyScore}</span>
                </label>
                <input
                  type="range" min={0} max={100}
                  value={form.baseUrgencyScore} onChange={e => setForm(f => ({ ...f, baseUrgencyScore: Number(e.target.value) }))}
                  className="w-full accent-primary"
                />
              </div>
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setModal({ open: false })} className="px-4 py-2 rounded-pill border border-border-default text-text-muted text-sm hover:bg-surface-card transition-colors cursor-pointer">
                  Hủy
                </button>
                <button type="submit" disabled={saving} className="px-4 py-2 rounded-pill bg-status-high text-surface-bg text-sm font-medium hover:opacity-90 disabled:opacity-50 transition-opacity cursor-pointer">
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
            <h2 className="text-lg font-bold text-text-primary mb-2">Xóa loại thiên tai</h2>
            <p className="text-text-muted text-sm mb-1">Bạn có chắc muốn xóa</p>
            <p className="text-text-body font-medium mb-4">&ldquo;{deleteTarget.name}&rdquo;?</p>
            {error && <p className="text-status-high text-sm mb-3">{error}</p>}
            <div className="flex justify-end gap-3">
              <button onClick={() => { setDeleteTarget(null); setError(''); }} className="px-4 py-2 rounded-pill border border-border-default text-text-muted text-sm hover:bg-surface-card transition-colors cursor-pointer">
                Hủy
              </button>
              <button onClick={handleDelete} className="px-4 py-2 rounded-pill bg-status-high text-white text-sm font-medium hover:opacity-90 transition-opacity cursor-pointer">
                Xóa
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
