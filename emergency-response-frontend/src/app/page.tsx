'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/AuthContext';
import LandingContent from '@/components/landing/LandingContent';

const HAS_VISITED_KEY = 'hasVisitedLanding';

export default function LandingPage() {
  const { user, loading } = useAuth();
  const router = useRouter();
  const hasVisited = typeof window !== 'undefined' && localStorage.getItem(HAS_VISITED_KEY);

  useEffect(() => {
    if (loading) return;

    if (user) {
      router.replace('/map');
      return;
    }

    if (hasVisited) {
      router.replace('/map');
    }
  }, [user, loading, hasVisited, router]);

  if (loading) {
    return <div className="min-h-screen bg-surface-bg" />;
  }

  if (user || hasVisited) return null;

  localStorage.setItem(HAS_VISITED_KEY, 'true');

  return <LandingContent />;
}
