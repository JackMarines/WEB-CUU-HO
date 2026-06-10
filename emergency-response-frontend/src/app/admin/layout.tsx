'use client';

import { useEffect } from 'react';
import { usePathname } from 'next/navigation';
import ProtectedRoute from '@/components/common/ProtectedRoute';
import AdminSidebar from '@/components/admin/AdminSidebar';
import { useCallEvents } from '@/hooks/useCallEvents';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const { newCallCount, resetCount } = useCallEvents();
  const pathname = usePathname();

  useEffect(() => {
    if (pathname === '/admin/calls') {
      resetCount();
    }
  }, [pathname, resetCount]);

  return (
    <ProtectedRoute roles={['admin', 'superadmin']}>
      <div className="flex min-h-0 h-full bg-surface-medium">
        <AdminSidebar newCallCount={newCallCount} />
        <div className="flex-1 p-6 overflow-auto">{children}</div>
      </div>
    </ProtectedRoute>
  );
}
