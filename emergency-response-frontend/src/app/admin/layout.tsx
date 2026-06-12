'use client';

import { useState, useEffect } from 'react';
import { usePathname } from 'next/navigation';
import { Menu } from 'lucide-react';
import ProtectedRoute from '@/components/common/ProtectedRoute';
import AdminSidebar from '@/components/admin/AdminSidebar';
import { useCallEvents } from '@/hooks/useCallEvents';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const { newCallCount, resetCount } = useCallEvents();
  const pathname = usePathname();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    if (pathname === '/admin/calls') {
      resetCount();
    }
  }, [pathname, resetCount]);

  useEffect(() => {
    setSidebarOpen(false);
  }, [pathname]);

  return (
    <ProtectedRoute roles={['admin', 'superadmin']}>
      <div className="flex min-h-0 h-full bg-surface-medium">
        <AdminSidebar newCallCount={newCallCount} open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
        <div className="flex-1 flex flex-col min-w-0">
          {/* Mobile header */}
          <div className="md:hidden flex items-center gap-3 px-4 py-2.5 border-b border-border-default bg-surface-elevated">
            <button
              onClick={() => setSidebarOpen(true)}
              className="w-11 h-11 rounded-full flex items-center justify-center text-text-muted hover:bg-surface-card hover:text-text-body transition-colors cursor-pointer"
              aria-label="Mở menu"
            >
              <Menu size={20} />
            </button>
            <span className="text-xs font-semibold tracking-wider text-text-muted uppercase">Quản trị</span>
          </div>
          <div className="flex-1 p-4 sm:p-6 overflow-auto">
            {children}
          </div>
        </div>
      </div>
    </ProtectedRoute>
  );
}
