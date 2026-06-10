'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LayoutDashboard, PhoneCall, Building2, Users, TriangleAlert } from 'lucide-react';

const links = [
  { href: '/admin', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/admin/calls', label: 'Distress Calls', icon: PhoneCall },
  { href: '/admin/centers', label: 'Rescue Centers', icon: Building2 },
  { href: '/admin/users', label: 'Users', icon: Users },
  { href: '/admin/disaster-types', label: 'Disaster Types', icon: TriangleAlert },
];

export default function AdminSidebar({ newCallCount = 0 }: { newCallCount?: number }) {
  const pathname = usePathname();

  return (
    <aside className="w-60 bg-surface-bg text-text-body min-h-full p-4 flex flex-col">
      <h2 className="text-base font-bold text-text-primary mb-6 tracking-wide flex items-center gap-2">
        <LayoutDashboard size={18} />Admin Panel
      </h2>
      <nav className="space-y-1 flex-1">
        {links.map((link) => {
          const Icon = link.icon;
          const isActive = pathname === link.href;
          return (
            <Link
              key={link.href}
              href={link.href}
              className={`block px-4 py-2 rounded-pill text-sm font-medium transition-colors relative flex items-center gap-2 ${
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
    </aside>
  );
}
