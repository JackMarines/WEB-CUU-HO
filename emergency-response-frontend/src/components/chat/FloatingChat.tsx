'use client';

import { useState, useRef, useEffect } from 'react';
import type { ChatMessage as ChatMessageType } from '@/types/chat';
import { chatService } from '@/services/chatService';
import ChatMessage from './ChatMessage';
import ChatInput from './ChatInput';
import ChatTypingIndicator from './ChatTypingIndicator';

const PUBLIC_ACTIONS = [
  { label: 'Vật tư cho lũ lụt', query: 'vật tư cho lũ lụt' },
  { label: 'Trung tâm gần đây', query: 'trung tâm cứu hộ gần đây' },
];

export default function FloatingChat() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessageType[]>([]);
  const [sending, setSending] = useState(false);
  const listRef = useRef<HTMLDivElement>(null);

  const isAuthenticated = typeof window !== 'undefined' && !!localStorage.getItem('token');

  useEffect(() => {
    if (listRef.current) {
      listRef.current.scrollTop = listRef.current.scrollHeight;
    }
  }, [messages, sending]);

  async function handleSend(text: string) {
    const userMsg: ChatMessageType = { role: 'user', content: text };
    setMessages((prev) => [...prev, userMsg]);
    setSending(true);
    try {
      const res = isAuthenticated
        ? await chatService.sendMessage(text, undefined, messages)
        : await chatService.sendPublicMessage(text, messages);
      const botMsg: ChatMessageType = { role: 'assistant', content: res.reply };
      setMessages((prev) => [...prev, botMsg]);
    } catch {
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', content: 'Đã xảy ra lỗi. Vui lòng thử lại.' },
      ]);
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="fixed bottom-6 right-6 z-[1000] flex flex-col items-end">
      {open && (
        <div className="mb-3 w-[350px] max-h-[500px] rounded-section border border-border-default bg-surface-elevated shadow-lg flex flex-col overflow-hidden">
          <div className="px-4 py-3 border-b border-border-default flex items-center justify-between">
            <div>
              <h2 className="text-[15px] font-semibold text-text-primary">Trợ lý AI</h2>
              <p className="text-[10px] font-semibold tracking-wider uppercase text-text-muted mt-0.5">
                Hỏi về vật tư, trung tâm cứu hộ
              </p>
            </div>
            <button
              onClick={() => setOpen(false)}
              className="text-text-muted hover:text-text-body cursor-pointer"
              aria-label="Đóng"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>

          <div ref={listRef} className="flex-1 overflow-y-auto py-3 space-y-1 max-h-[380px]">
            {messages.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-full text-center px-6 py-12">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#888" strokeWidth="1.5" strokeLinecap="round" className="mb-3">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                </svg>
                <p className="text-sm text-text-muted mb-1">Tôi có thể giúp gì cho bạn?</p>
                <p className="text-[10px] text-text-subtle">
                  Ví dụ: &quot;vật tư cho lũ lụt&quot;, &quot;trung tâm gần đây&quot;
                </p>
              </div>
            ) : (
              messages.map((msg, i) => <ChatMessage key={i} message={msg} />)
            )}
            {sending && <ChatTypingIndicator />}
          </div>

          <ChatInput onSend={handleSend} disabled={sending} quickActions={PUBLIC_ACTIONS} />
        </div>
      )}

      <button
        onClick={() => setOpen((prev) => !prev)}
        className="w-14 h-14 rounded-full bg-status-high text-surface-bg shadow-lg flex items-center justify-center hover:opacity-90 transition-all cursor-pointer"
        aria-label="Mở trợ lý AI"
      >
        {open ? (
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        ) : (
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
        )}
      </button>
    </div>
  );
}
