'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Home, Map, Send, Building2, MessageSquare, Settings, Shield, LogIn, LogOut } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';

interface NavItem {
  href: string;
  label: string;
  icon: typeof Map;
  auth?: boolean;
  admin?: boolean;
}

const navItems: NavItem[] = [
  { href: '/home', label: 'Trang chủ', icon: Home },
  { href: '/map', label: 'Bản đồ', icon: Map },
  { href: '/submit', label: 'Gửi yêu cầu', icon: Send },
  { href: '/centers', label: 'Trung tâm', icon: Building2 },
  { href: '/calls/mine', label: 'Yêu cầu của tôi', icon: MessageSquare, auth: true },
  { href: '/profile', label: 'Cài đặt', icon: Settings, auth: true },
  { href: '/admin', label: 'Admin', icon: Shield, admin: true },
];

export default function AppSidebar() {
  const pathname = usePathname();
  const { user, logout } = useAuth();

  const filtered = navItems.filter(item => {
    if (item.admin && user?.role !== 'admin' && user?.role !== 'superadmin') return false;
    if (item.auth && !user) return false;
    return true;
  });

  const isActive = (href: string) =>
    href === '/' ? pathname === '/' : pathname.startsWith(href);

  return (
    <aside className="w-[60px] bg-surface-bg border-r border-border-default flex flex-col items-center py-5 shrink-0 max-md:w-full max-md:flex-row max-md:py-2.5 max-md:px-4 max-md:gap-4 max-md:justify-center max-md:border-r-0 max-md:border-b max-md:border-border-default">
      <nav className="flex flex-col items-center gap-7 flex-1 max-md:flex-row max-md:gap-4">
        {filtered.map(item => {
          const Icon = item.icon;
          return (
            <Link
              key={item.href}
              href={item.href}
              title={item.label}
              className={`w-9 h-9 rounded-full flex items-center justify-center transition-all duration-150 shrink-0 ${
                isActive(item.href)
                  ? 'bg-status-high text-surface-bg'
                  : 'text-text-muted hover:bg-primary-subtle hover:text-primary'
              }`}
            >
              <Icon size={20} />
            </Link>
          );
        })}
      </nav>

      <div className="flex flex-col items-center gap-3 max-md:flex-row">
        {user ? (
          <>
            <div
              title={user.name || user.email}
              className="w-9 h-9 rounded-full flex items-center justify-center bg-primary-subtle text-primary text-xs font-bold shrink-0"
            >
              {(user.name || user.email || '?').charAt(0).toUpperCase()}
            </div>
            <button
              onClick={logout}
              title="Đăng xuất"
              className="w-9 h-9 rounded-full flex items-center justify-center text-text-muted hover:bg-primary-subtle hover:text-primary transition-all duration-150 shrink-0 cursor-pointer"
            >
              <LogOut size={18} />
            </button>
          </>
        ) : (
          <Link
            href="/login"
            title="Đăng nhập"
            className="w-9 h-9 rounded-full flex items-center justify-center text-text-muted hover:bg-primary-subtle hover:text-primary transition-all duration-150 shrink-0"
          >
            <LogIn size={18} />
          </Link>
        )}
      </div>
    </aside>
  );
}
