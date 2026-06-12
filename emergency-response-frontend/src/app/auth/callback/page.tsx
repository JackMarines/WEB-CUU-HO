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
    const errorParam = searchParams.get('error');
    if (errorParam) {
      if (errorParam === 'not_whitelisted') {
        setError('Tài khoản của bạn chưa được cấp quyền truy cập. Vui lòng liên hệ quản trị viên.');
      } else {
        setError('Đăng nhập thất bại');
      }
      return;
    }

    const token = searchParams.get('token');
    if (token) {
      localStorage.setItem('token', token);
      authService
        .getMe()
        .then((user) => {
          localStorage.setItem('user', JSON.stringify(user));
          setUser(user);
          router.push('/map');
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
