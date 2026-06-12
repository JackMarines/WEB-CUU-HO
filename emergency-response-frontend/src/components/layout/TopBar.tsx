'use client';

import { Shield } from 'lucide-react';

export default function TopBar() {
  return (
    <div className="h-9 bg-surface-elevated border-b border-border-light flex items-center justify-between px-5">
      <span className="text-primary font-semibold tracking-wider text-xs uppercase flex items-center gap-1.5">
        <Shield size={13} />Cứu Trợ Khẩn Cấp
      </span>
      <span className="hidden sm:inline text-text-subtle text-[11px] tracking-wide">
        Emergency Response Platform
      </span>
    </div>
  );
}
