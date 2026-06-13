'use client';

import { useState, useEffect, useRef } from 'react';
import Link from 'next/link';
import { Shield, ChevronDown, Bot, LogOut, Map, LayoutDashboard, User } from 'lucide-react';
import { callService } from '@/services/callService';
import { useAuth } from '@/contexts/AuthContext';
import Reveal from '@/components/common/Reveal';

function VietnamMapPattern() {
  return (
    <svg viewBox="0 0 800 900" className="w-full h-full" preserveAspectRatio="xMidYMid slice"
         xmlns="http://www.w3.org/2000/svg">
      <g fill="none" stroke="currentColor" strokeWidth="0.4" opacity="0.5">
        <path d="M380 40 Q400 60 395 90 Q388 120 400 160 Q415 200 405 240 Q395 280 410 320 Q425 360 415 400 Q405 440 420 480 Q435 520 425 560 Q410 590 420 620 Q430 650 425 690 Q418 730 430 760 Q440 790 435 810" />
        <path d="M370 50 Q355 100 370 150 Q385 200 375 250 Q365 300 380 350 Q395 400 385 450 Q375 500 390 550 Q405 600 395 640 Q380 680 390 720 Q400 750 395 780" />
        <path d="M400 30 Q425 80 410 130 Q395 180 415 230 Q435 280 420 330 Q405 380 425 430 Q445 480 430 530 Q415 580 435 620 Q455 660 440 700 Q425 740 440 770 Q455 800 445 830" />
        <path d="M390 60 Q410 110 395 160 Q435 130 440 180 Q445 230 450 280 Q440 330 445 380 Q450 430 440 480 Q435 530 450 570 Q465 610 450 650 Q440 690 450 730" />
        <path d="M405 90 Q425 140 440 190 Q445 240 435 290 Q425 340 445 390 Q455 440 445 490 Q440 540 455 580 Q465 620 455 670" />
      </g>
      <g fill="none" stroke="currentColor" strokeWidth="0.25" opacity="0.3">
        <path d="M340 80 Q330 130 345 180 Q360 230 350 280 Q340 330 355 380 Q370 430 360 480 Q350 530 365 580 Q380 630 370 680 Q360 730 375 780" />
        <path d="M450 50 Q465 100 455 150 Q445 200 460 250 Q475 300 465 350 Q455 400 470 450 Q485 500 475 550 Q465 600 480 640 Q495 680 485 720" />
        <path d="M355 150 Q370 200 360 250 Q350 300 365 350 Q380 400 370 450 Q360 500 375 550 Q390 600 380 640 Q370 680 385 720" />
      </g>
      <g fill="none" stroke="currentColor" strokeWidth="0.15" opacity="0.2">
        <path d="M330 200 Q320 250 335 300 Q350 350 340 400 Q330 450 345 500 Q360 550 350 600 Q340 650 355 700" />
        <path d="M470 150 Q485 200 475 250 Q465 300 480 350 Q495 400 485 450 Q475 500 490 540 Q505 580 495 620" />
        <path d="M350 350 Q365 400 355 450 Q345 500 360 550 Q375 600 365 640" />
        <path d="M430 200 Q445 250 435 300 Q425 350 440 400 Q455 450 445 500 Q435 550 450 590" />
      </g>
    </svg>
  );
}

function CallFormMockup() {
  return (
    <svg viewBox="0 0 280 440" className="w-full max-w-[240px] sm:max-w-[280px] h-auto mx-auto"
         xmlns="http://www.w3.org/2000/svg">
      <rect x="0" y="0" width="280" height="440" rx="20" fill="#303030" />
      <rect x="0" y="0" width="280" height="32" rx="20" fill="#303030" />
      <rect x="100" y="12" width="80" height="4" rx="2" fill="#555" />
      <rect x="12" y="44" width="120" height="6" rx="3" fill="#f86e64" opacity="0.6" />
      <rect x="12" y="60" width="256" height="36" rx="8" fill="#252525" />
      <rect x="20" y="72" width="100" height="5" rx="2.5" fill="#555" />
      <rect x="12" y="106" width="80" height="6" rx="3" fill="#888" />
      <rect x="12" y="122" width="256" height="36" rx="8" fill="#252525" />
      <rect x="20" y="134" width="140" height="5" rx="2.5" fill="#555" />
      <rect x="12" y="168" width="100" height="6" rx="3" fill="#888" />
      <rect x="12" y="184" width="256" height="80" rx="8" fill="#252525" />
      <rect x="20" y="200" width="200" height="5" rx="2.5" fill="#555" />
      <rect x="20" y="214" width="180" height="5" rx="2.5" fill="#555" />
      <rect x="20" y="228" width="160" height="5" rx="2.5" fill="#555" />
      <rect x="12" y="278" width="100" height="6" rx="3" fill="#888" />
      <rect x="12" y="294" width="256" height="36" rx="8" fill="#252525" />
      <circle cx="24" cy="311" r="6" fill="#3A3A3A" />
      <circle cx="44" cy="311" r="6" fill="#3A3A3A" />
      <circle cx="64" cy="311" r="6" fill="#3A3A3A" />
      <rect x="12" y="350" width="256" height="40" rx="8" fill="#f86e64" />
      <text x="140" y="375" textAnchor="middle" fill="#1E1E1E" fontSize="12" fontWeight="600" fontFamily="DM Sans, sans-serif">Gửi yêu cầu</text>
    </svg>
  );
}

function MapMockup() {
  return (
    <svg viewBox="0 0 280 440" className="w-full max-w-[240px] sm:max-w-[280px] h-auto mx-auto"
         xmlns="http://www.w3.org/2000/svg">
      <defs>
        <clipPath id="mapClip"><rect x="0" y="0" width="280" height="440" rx="20" /></clipPath>
      </defs>
      <rect x="0" y="0" width="280" height="440" rx="20" fill="#303030" />
      <rect x="0" y="0" width="280" height="32" rx="20" fill="#303030" />
      <rect x="100" y="12" width="80" height="4" rx="2" fill="#555" />
      <g clipPath="url(#mapClip)">
        <rect x="0" y="0" width="280" height="440" fill="#1E1E1E" />
        <g fill="#2E2E2E" opacity="0.5">
          <path d="M0 100 Q70 80 140 120 Q210 160 280 140" />
          <path d="M0 180 Q60 160 130 200 Q200 240 280 220" />
          <path d="M0 260 Q80 240 150 280 Q220 320 280 300" />
          <path d="M0 340 Q70 320 140 360 Q210 400 280 380" />
        </g>
        <g fill="#2E2E2E" opacity="0.3">
          <path d="M0 140 Q70 120 140 160 Q210 200 280 180" />
          <path d="M0 220 Q60 200 130 240 Q200 280 280 260" />
          <path d="M0 300 Q80 280 150 320 Q220 360 280 340" />
        </g>
        <circle cx="140" cy="180" r="6" fill="#f86e64" />
        <circle cx="140" cy="180" r="12" fill="none" stroke="#f86e64" strokeWidth="1.5" opacity="0.4" />
        <circle cx="90" cy="240" r="5" fill="#FF8A50" />
        <circle cx="200" cy="200" r="5" fill="#D4A84B" />
        <circle cx="60" cy="140" r="4" fill="#66BB6A" />
        <circle cx="180" cy="300" r="5" fill="#f86e64" />
        <circle cx="180" cy="300" r="10" fill="none" stroke="#f86e64" strokeWidth="1.5" opacity="0.4" />
        <rect x="10" y="10" width="60" height="22" rx="6" fill="#f86e64" />
        <text x="40" y="25" textAnchor="middle" fill="#1E1E1E" fontSize="10" fontWeight="600" fontFamily="DM Sans, sans-serif">3 cuộc gọi</text>
      </g>
    </svg>
  );
}

export default function LandingContent() {
  const [scrolled, setScrolled] = useState(false);
  const [activeCalls, setActiveCalls] = useState<number | null>(null);
  const [imgError, setImgError] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const profileRef = useRef<HTMLDivElement>(null);
  const { user, logout } = useAuth();

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (profileRef.current && !profileRef.current.contains(e.target as Node)) {
        setProfileOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    callService.getStats()
      .then(stats => setActiveCalls(stats.activeCalls + stats.inProgressCalls))
      .catch(() => setActiveCalls(null));
  }, []);

  const scrollTo = (id: string) => {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <>
      <nav className={`fixed top-0 left-0 right-0 z-50 h-12 flex items-center justify-between px-5 transition-all duration-200 ${
        scrolled ? 'bg-surface-bg/92 border-b border-border-default backdrop-blur' : 'bg-transparent border-b border-transparent'
      }`}>
        <Link href="/" className="flex items-center gap-1.5 text-primary font-semibold tracking-wider text-xs uppercase">
          <Shield size={13} />Cứu Trợ Khẩn Cấp
        </Link>
        <div className="flex items-center gap-3">
          {user ? (
            <div className="relative" ref={profileRef}>
              <button onClick={() => setProfileOpen(!profileOpen)}
                      className="flex items-center gap-2 cursor-pointer bg-transparent border-none text-text-body hover:text-text-primary transition-colors">
                {user.avatarUrl ? (
                  <img src={user.avatarUrl} alt="" className="w-7 h-7 rounded-full object-cover" />
                ) : (
                  <div className="w-7 h-7 rounded-full bg-status-high flex items-center justify-center text-surface-bg text-xs font-semibold">
                    {user.name.charAt(0).toUpperCase()}
                  </div>
                )}
                <span className="text-xs hidden sm:inline">{user.name}</span>
              </button>
              {profileOpen && (
                <div className="absolute right-0 top-full mt-2 w-48 bg-surface-card border border-border-default rounded-card shadow-xl z-50 py-1.5 backdrop-blur">
                  <Link href="/map"
                        onClick={() => setProfileOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2 text-xs text-text-body hover:text-text-primary hover:bg-surface-medium transition-colors">
                    <Map size={14} /> Bản đồ
                  </Link>
                  {(user.role === 'admin' || user.role === 'superadmin') && (
                    <Link href="/admin"
                          onClick={() => setProfileOpen(false)}
                          className="flex items-center gap-2.5 px-4 py-2 text-xs text-text-body hover:text-text-primary hover:bg-surface-medium transition-colors">
                      <LayoutDashboard size={14} /> Bảng điều khiển
                    </Link>
                  )}
                  <Link href="/submit"
                        onClick={() => setProfileOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2 text-xs text-text-body hover:text-text-primary hover:bg-surface-medium transition-colors">
                    <User size={14} /> Gửi yêu cầu
                  </Link>
                  <hr className="border-border-default my-1" />
                  <button onClick={() => { setProfileOpen(false); logout(); }}
                          className="w-full flex items-center gap-2.5 px-4 py-2 text-xs text-text-muted hover:text-status-high hover:bg-surface-medium transition-colors bg-transparent border-none cursor-pointer text-left">
                    <LogOut size={14} /> Đăng xuất
                  </button>
                </div>
              )}
            </div>
          ) : (
            <>
              <Link href="/login" className="text-text-muted text-xs hover:text-text-body transition-colors">
                Đăng nhập
              </Link>
              <Link href="/submit"
                    className="bg-status-high text-surface-bg px-3 py-1.5 rounded-pill text-xs font-semibold hover:opacity-90 transition-all duration-200 hover:-translate-y-0.5">
                Gửi yêu cầu
              </Link>
            </>
          )}
        </div>
      </nav>

      <section className="relative min-h-screen flex items-center justify-center overflow-hidden pt-12">
        <div className="absolute inset-0 opacity-[0.03] pointer-events-none text-text-primary">
          <VietnamMapPattern />
        </div>
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] rounded-full pointer-events-none"
             style={{ background: 'radial-gradient(circle, var(--color-status-high) 0%, transparent 70%)', opacity: 0.08 }} />

        <div className="relative z-10 text-center px-5 max-w-2xl mx-auto">
          <Reveal>
            <h1 className="text-[clamp(24px,5vw,40px)] font-semibold leading-[1.2] text-text-primary mb-4">
              Khi thiên tai xảy ra,<br />mọi giây đều quý giá
            </h1>
          </Reveal>
          <Reveal delay={150}>
            <p className="text-[clamp(13px,2vw,15px)] text-text-body leading-relaxed mb-8 max-w-lg mx-auto">
              Gửi tín hiệu cầu cứu kèm vị trí chính xác. Kết nối ngay với trung tâm cứu hộ gần nhất.
            </p>
          </Reveal>
          <Reveal delay={300}>
            <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
              <Link href="/submit"
                    className="bg-status-high text-surface-bg px-6 py-3 rounded-pill font-semibold text-sm hover:opacity-90 transition-all duration-200 hover:-translate-y-0.5 active:translate-y-0">
                Gửi yêu cầu cứu trợ
              </Link>
              <button onClick={() => scrollTo('about')}
                      className="text-text-body text-xs hover:text-text-primary transition-colors flex items-center gap-1 cursor-pointer bg-transparent border-none">
                Tìm hiểu thêm <ChevronDown size={14} />
              </button>
            </div>
          </Reveal>
          {activeCalls !== null && (
            <Reveal delay={450}>
              <p className="mt-10 text-text-muted text-xs">
                Đang hỗ trợ: {activeCalls} trường hợp
              </p>
            </Reveal>
          )}
        </div>
      </section>

      <section id="about" className="py-20 sm:py-28 px-5">
        <div className="max-w-5xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-10 lg:gap-16 items-center">
          <Reveal>
            <div>
              <h2 className="text-[clamp(20px,3vw,28px)] font-semibold leading-[1.25] text-text-primary mb-5">
                Về Cứu Trợ Khẩn Cấp
              </h2>
              <div className="space-y-4 text-text-body text-[clamp(13px,1.5vw,14px)] leading-relaxed max-w-[70ch]">
                <p>
                  Việt Nam là một trong những quốc gia chịu nhiều ảnh hưởng của thiên tai trên thế giới. Bão, lũ lụt,
                  sạt lở đất và hỏa hoạn xảy ra thường xuyên ở nhiều vùng miền, gây thiệt hại lớn về người và tài sản.
                </p>
                <p>
                  Cứu Trợ Khẩn Cấp là nền tảng web kết nối người dân gặp thiên tai với các trung tâm cứu hộ và cứu trợ
                  trên toàn quốc. Dự án được phát triển bởi nhóm sinh viên tham gia AI WEB CHALLENGE 2026.
                </p>
                <p>
                  Nền tảng cho phép người dân gửi tín hiệu cầu cứu kèm vị trí và mô tả tình hình. Các quản trị viên tại
                  trung tâm cứu hộ có thể xem, phân loại và điều phối lực lượng đến hiện trường một cách nhanh chóng.
                </p>
              </div>
            </div>
          </Reveal>
          <Reveal delay={150}>
            <div className="relative rounded-section overflow-hidden">
              {!imgError ? (
                <img
                  src="https://images.unsplash.com/photo-1582213782179-e0d53f98f2ca?auto=format&fit=crop&w=800&q=80"
                  alt="Đội cứu hộ đang làm nhiệm vụ"
                  className="w-full h-[320px] sm:h-[400px] object-cover rounded-section"
                  onError={() => setImgError(true)}
                  loading="lazy"
                />
              ) : (
                <div className="w-full h-[320px] sm:h-[400px] rounded-section bg-surface-card flex items-center justify-center">
                  <div className="opacity-20 w-40 h-40">
                    <VietnamMapPattern />
                  </div>
                </div>
              )}
            </div>
          </Reveal>
        </div>
      </section>

      <section id="features" className="py-20 sm:py-28 px-5 bg-surface-medium">
        <div className="max-w-5xl mx-auto">
          <Reveal>
            <h2 className="text-[clamp(20px,3vw,28px)] font-semibold leading-[1.25] text-text-primary mb-4 text-center">
              Tính năng chính
            </h2>
            <p className="text-text-muted text-sm text-center mb-14 max-w-md mx-auto">
              Nền tảng được thiết kế để hoạt động nhanh chóng và đáng tin cậy trong mọi tình huống khẩn cấp
            </p>
          </Reveal>

          <div className="space-y-20">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-14 items-center">
              <Reveal className="order-2 lg:order-1">
                <div className="max-w-md">
                  <span className="inline-block text-[10px] font-semibold tracking-[0.04em] uppercase text-status-high mb-2">
                    Tính năng 01
                  </span>
                  <h3 className="text-title text-text-primary mb-3">Gửi yêu cầu cứu trợ</h3>
                  <p className="text-body text-text-body leading-relaxed">
                    Chọn vị trí trên bản đồ, chọn loại thiên tai (lũ lụt, cháy, sạt lở đất, bão&hellip;),
                    mô tả tình hình và gửi yêu cầu. Hệ thống tự động tính điểm khẩn cấp dựa trên loại thiên tai.
                  </p>
                </div>
              </Reveal>
              <Reveal delay={150} className="order-1 lg:order-2 flex justify-center">
                <CallFormMockup />
              </Reveal>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-14 items-center">
              <Reveal delay={150} className="flex justify-center lg:order-2">
                <MapMockup />
              </Reveal>
              <Reveal className="lg:order-1">
                <div className="max-w-md lg:ml-auto">
                  <span className="inline-block text-[10px] font-semibold tracking-[0.04em] uppercase text-status-high mb-2">
                    Tính năng 02
                  </span>
                  <h3 className="text-title text-text-primary mb-3">Bản đồ tương tác</h3>
                  <p className="text-body text-text-body leading-relaxed">
                    Toàn bộ yêu cầu cứu trợ và trung tâm cứu hộ được hiển thị trên bản đồ OpenStreetMap.
                    Marker có màu sắc phân biệt theo mức độ khẩn cấp và trạng thái xử lý.
                  </p>
                </div>
              </Reveal>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <Reveal>
                <div className="bg-surface-card rounded-card p-6 border border-border-default">
                  <div className="w-10 h-10 rounded-full bg-primary-subtle flex items-center justify-center mb-4">
                    <Bot size={20} className="text-primary" />
                  </div>
                  <h3 className="text-title text-text-primary mb-2">Chatbot hỗ trợ</h3>
                  <p className="text-body text-text-body leading-relaxed">
                    Trò chuyện với AI chatbot để được hướng dẫn sử dụng nền tảng, các biện pháp an toàn
                    khi gặp thiên tai, hoặc tìm thông tin về trung tâm cứu hộ gần nhất.
                  </p>
                </div>
              </Reveal>
              <Reveal delay={150}>
                <div className="bg-surface-card rounded-card p-6 border border-border-default">
                  <div className="w-10 h-10 rounded-full bg-primary-subtle flex items-center justify-center mb-4">
                    <Shield size={20} className="text-primary" />
                  </div>
                  <h3 className="text-title text-text-primary mb-2">Quản lý cho admin</h3>
                  <p className="text-body text-text-body leading-relaxed">
                    Quản trị viên có bảng điều khiển riêng với thống kê tổng quan, danh sách yêu cầu cứu trợ
                    sắp xếp theo mức độ khẩn cấp, và công cụ phân công trung tâm cứu hộ.
                  </p>
                </div>
              </Reveal>
            </div>
          </div>
        </div>
      </section>

      <section className="py-20 sm:py-28 px-5">
        <div className="max-w-2xl mx-auto text-center">
          <Reveal>
            <h2 className="text-[clamp(20px,3vw,28px)] font-semibold leading-[1.25] text-text-primary mb-5">
              Đội Ngũ Phát Triển
            </h2>
            <p className="text-body text-text-body leading-relaxed max-w-[65ch] mx-auto">
              Dự án được thực hiện bởi nhóm sinh viên chuyên ngành Phát triển Phần mềm trong vòng 72h,
              dưới sự hướng dẫn của giảng viên Bộ môn Công nghệ Thông tin.
            </p>
          </Reveal>
        </div>
      </section>

      <footer className="border-t border-border-default">
        <div className="max-w-5xl mx-auto px-5 py-8 flex flex-col sm:flex-row items-center justify-between gap-3">
          <p className="text-text-muted text-xs">
            Cuộc thi AI WEB CHALLENGE 2026 &middot; Cứu trợ khẩn cấp
          </p>
          <a href="mailto:lienhe@cuutrokhancap.vn"
             className="text-text-muted text-xs hover:text-text-body transition-colors">
            lienhe@cuutrokhancap.vn
          </a>
        </div>
      </footer>
    </>
  );
}
