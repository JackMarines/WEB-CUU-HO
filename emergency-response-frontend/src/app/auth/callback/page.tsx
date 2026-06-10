'use client';

import { Suspense, useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuth } from '@/contexts/AuthContext';
import { authService } from '@/services/authService';

function CallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { setUser } = useAuth();
  const [error, setError] = useState('');

  useEffect(() => {
    const token = searchParams.get('token');
    if (token) {
      localStorage.setItem('token', token);
      authService
        .getMe()
        .then((user) => {
          localStorage.setItem('user', JSON.stringify(user));
          setUser(user);
          router.push('/');
        })
        .catch(() => {
          setError('Đăng nhập thất bại');
        });
    } else {
      setError('Không tìm thấy token');
    }
  }, [searchParams, router, setUser]);

  if (error) {
    return <div className="text-center mt-20 text-status-high">{error}</div>;
  }

  return <div className="text-center mt-20 text-text-muted">Đang đăng nhập...</div>;
}

export default function AuthCallbackPage() {
  return (
    <Suspense fallback={<div className="text-center mt-20 text-text-muted">Đang đăng nhập...</div>}>
      <CallbackContent />
    </Suspense>
  );
}
