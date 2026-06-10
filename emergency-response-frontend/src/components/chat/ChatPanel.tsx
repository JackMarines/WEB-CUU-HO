'use client';

import { useState, useRef, useEffect } from 'react';
import type { ChatMessage as ChatMessageType } from '@/types/chat';
import { chatService } from '@/services/chatService';
import ChatMessage from './ChatMessage';
import ChatInput from './ChatInput';
import ChatTypingIndicator from './ChatTypingIndicator';
import ChatSkeleton from './ChatSkeleton';

interface Props {
  callId?: number;
}

export default function ChatPanel({ callId }: Props) {
  const [messages, setMessages] = useState<ChatMessageType[]>([]);
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const listRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const timer = setTimeout(() => setLoading(false), 600);
    return () => clearTimeout(timer);
  }, []);

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
      const res = await chatService.sendMessage(text, callId, messages);
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
    <div className="rounded-section border border-border-default bg-surface-elevated flex flex-col overflow-hidden">
      <div className="px-4 py-3 border-b border-border-default">
        <h2 className="text-[15px] font-semibold text-text-primary">Trợ lý AI</h2>
        <p className="text-[10px] font-semibold tracking-wider uppercase text-text-muted mt-0.5">
          Hỏi về vật tư, cuộc gọi, trung tâm cứu hộ
        </p>
      </div>

      <div ref={listRef} className="flex-1 overflow-y-auto py-3 space-y-1 max-h-[400px]">
        {loading ? (
          <ChatSkeleton />
        ) : messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-center px-6 py-12">
            <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#888" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" className="mb-3">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
              <circle cx="12" cy="10" r="1" fill="#888" />
              <circle cx="9" cy="10" r="1" fill="#888" />
              <circle cx="15" cy="10" r="1" fill="#888" />
            </svg>
            <p className="text-sm text-text-muted mb-1">Tôi có thể giúp gì cho bạn?</p>
            <p className="text-[10px] text-text-subtle">
              Ví dụ: &quot;vật tư cho lũ lụt&quot;, &quot;thông tin cuộc gọi #5&quot;
            </p>
          </div>
        ) : (
          messages.map((msg, i) => <ChatMessage key={i} message={msg} />)
        )}
        {sending && <ChatTypingIndicator />}
      </div>

      <ChatInput onSend={handleSend} disabled={sending} />
    </div>
  );
}
