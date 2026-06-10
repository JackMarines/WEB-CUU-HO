'use client';

export default function ChatTypingIndicator() {
  return (
    <div className="flex items-start gap-3 px-4 py-2">
      <div className="flex items-center gap-1 px-4 py-3 rounded-lg bg-surface-card">
        <span className="w-2 h-2 rounded-full bg-primary animate-bounce" style={{ animationDelay: '0ms' }} />
        <span className="w-2 h-2 rounded-full bg-primary animate-bounce" style={{ animationDelay: '150ms' }} />
        <span className="w-2 h-2 rounded-full bg-primary animate-bounce" style={{ animationDelay: '300ms' }} />
      </div>
      <span className="text-[10px] font-semibold tracking-wider uppercase text-text-muted">Trợ lý</span>
    </div>
  );
}
