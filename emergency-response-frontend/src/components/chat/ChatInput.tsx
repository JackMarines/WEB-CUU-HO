'use client';

import { useState } from 'react';

interface QuickAction {
  label: string;
  query: string;
}

const DEFAULT_ACTIONS: QuickAction[] = [
  { label: 'Vật tư cho lũ lụt', query: 'vật tư cho lũ lụt' },
  { label: 'Thông tin cuộc gọi', query: 'thông tin cuộc gọi' },
  { label: 'Trung tâm gần đây', query: 'trung tâm cứu hộ gần đây' },
];

interface Props {
  onSend: (message: string) => void;
  disabled: boolean;
  quickActions?: QuickAction[];
}

export default function ChatInput({ onSend, disabled, quickActions }: Props) {
  const actions = quickActions ?? DEFAULT_ACTIONS;
  const [input, setInput] = useState('');

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const trimmed = input.trim();
    if (!trimmed || disabled) return;
    onSend(trimmed);
    setInput('');
  }

  return (
    <div className="border-t border-border-default px-4 py-3 space-y-2">
      <div className="flex flex-wrap gap-1.5">
        {actions.map((a) => (
          <button
            key={a.query}
            type="button"
            disabled={disabled}
            onClick={() => { onSend(a.query); }}
            className="text-[10px] font-semibold tracking-wider uppercase px-2.5 py-1.5 rounded-pill bg-surface-elevated text-text-muted hover:bg-[#3A3A3A] hover:text-text-body transition-colors disabled:opacity-40 cursor-pointer"
          >
            {a.label}
          </button>
        ))}
      </div>
      <form onSubmit={handleSubmit} className="flex items-center gap-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Nhập câu hỏi..."
          disabled={disabled}
          className="flex-1 px-3 py-2 rounded-pill bg-surface-card border border-border-default text-text-body text-sm placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
        />
        <button
          type="submit"
          disabled={disabled || !input.trim()}
          className="w-8 h-8 rounded-full bg-status-high text-surface-bg flex items-center justify-center hover:opacity-90 disabled:opacity-40 transition-opacity cursor-pointer flex-shrink-0"
          aria-label="Send"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <line x1="22" y1="2" x2="11" y2="13" />
            <polygon points="22 2 15 22 11 13 2 9 22 2" />
          </svg>
        </button>
      </form>
    </div>
  );
}
