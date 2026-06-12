import api from './api';
import type { ChatMessage, ChatResponse } from '@/types/chat';

export const chatService = {
  sendMessage: async (
    message: string,
    callId?: number,
    history?: ChatMessage[]
  ): Promise<ChatResponse> => {
    const res = await api.post<ChatResponse>('/chat', { message, callId, history });
    return res.data;
  },

  sendPublicMessage: async (
    message: string,
    history?: ChatMessage[]
  ): Promise<ChatResponse> => {
    const res = await api.post<ChatResponse>('/chat/public', { message, history });
    return res.data;
  },
};
