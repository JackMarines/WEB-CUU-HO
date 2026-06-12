'use client';

import { usePathname } from 'next/navigation';
import { AuthProvider } from '@/contexts/AuthContext';
import TopBar from '@/components/layout/TopBar';
import AppSidebar from '@/components/layout/AppSidebar';
import FloatingChat from '@/components/chat/FloatingChat';

export default function RootLayoutClient({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isLanding = pathname === '/' || pathname === '/home';
  const showFloatingChat = ['/map', '/submit', '/centers'].includes(pathname);

  return (
    <AuthProvider>
      {isLanding ? (
        <div className="flex flex-col h-screen overflow-y-auto bg-surface-bg">
          {children}
        </div>
      ) : (
        <>
          <TopBar />
          <div className="flex flex-1 min-h-0 max-md:flex-col md:flex-row">
            <AppSidebar />
            <main className="flex-1 overflow-auto">{children}</main>
          </div>
          </>
        )}
        {showFloatingChat && <FloatingChat />}
    </AuthProvider>
  );
}
