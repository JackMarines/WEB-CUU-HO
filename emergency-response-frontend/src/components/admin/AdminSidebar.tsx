'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LayoutDashboard, PhoneCall, Building2, Users, TriangleAlert, X } from 'lucide-react';

const links = [
  { href: '/admin', label: 'Tổng quan', icon: LayoutDashboard },
  { href: '/admin/calls', label: 'Cuộc gọi', icon: PhoneCall },
  { href: '/admin/centers', label: 'Trung tâm', icon: Building2 },
  { href: '/admin/users', label: 'Người dùng', icon: Users },
  { href: '/admin/disaster-types', label: 'Loại thiên tai', icon: TriangleAlert },
];

export default function AdminSidebar({ newCallCount = 0, open = false, onClose }: { newCallCount?: number; open?: boolean; onClose?: () => void }) {
  const pathname = usePathname();

  const nav = (
    <nav className="space-y-1 flex-1">
      {links.map((link) => {
        const Icon = link.icon;
        const isActive = pathname === link.href;
        return (
          <Link
            key={link.href}
            href={link.href}
            onClick={onClose}
            className={`block px-4 py-2 rounded-pill text-sm font-medium transition-colors relative flex items-center gap-2 min-h-[44px] ${
              isActive
                ? 'bg-status-high text-surface-bg'
                : 'text-text-muted hover:bg-surface-card hover:text-text-body'
            }`}
          >
            <Icon size={16} />
            {link.label}
            {link.href === '/admin/calls' && newCallCount > 0 && (
              <span className="absolute right-3 top-1/2 -translate-y-1/2 bg-status-high text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full min-w-[18px] text-center leading-tight">
                {newCallCount > 99 ? '99+' : newCallCount}
              </span>
            )}
          </Link>
        );
      })}
    </nav>
  );

  return (
    <>
      {/* Mobile drawer overlay */}
      {open && (
        <div className="fixed inset-0 z-40 bg-black/50 md:hidden" onClick={onClose} />
      )}

      {/* Mobile drawer */}
      <aside className={`fixed top-0 left-0 z-50 h-full w-64 bg-surface-bg p-4 flex flex-col transition-transform duration-200 md:hidden ${
        open ? 'translate-x-0' : '-translate-x-full'
      }`}>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-base font-bold text-text-primary tracking-wide flex items-center gap-2">
            <LayoutDashboard size={18} />Quản trị
          </h2>
          <button onClick={onClose} className="w-9 h-9 rounded-full flex items-center justify-center text-text-muted hover:bg-surface-card hover:text-text-body transition-colors cursor-pointer">
            <X size={18} />
          </button>
        </div>
        {nav}
      </aside>

      {/* Desktop sidebar */}
      <aside className="hidden md:flex w-60 bg-surface-bg text-text-body min-h-full p-4 flex-col shrink-0">
        <h2 className="text-base font-bold text-text-primary mb-6 tracking-wide flex items-center gap-2">
          <LayoutDashboard size={18} />Quản trị
        </h2>
        {nav}
      </aside>
    </>
  );
}
