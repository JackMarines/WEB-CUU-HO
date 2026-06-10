'use client';

import type { ChatMessage as ChatMessageType } from '@/types/chat';

interface Props {
  message: ChatMessageType;
}

export default function ChatMessage({ message }: Props) {
  const isUser = message.role === 'user';

  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'} px-4 py-1.5`}>
      <div className={`max-w-[85%] ${isUser ? 'items-end' : 'items-start'} flex flex-col`}>
        <span className="text-[10px] font-semibold tracking-wider uppercase text-text-muted mb-1">
          {isUser ? 'Bạn' : 'Trợ lý'}
        </span>
        <div
          className={`rounded-lg px-4 py-3 text-sm whitespace-pre-wrap ${
            isUser
              ? 'bg-primary-subtle text-text-primary'
              : 'bg-surface-card text-text-body'
          }`}
        >
          {message.content}
        </div>
      </div>
    </div>
  );
}
