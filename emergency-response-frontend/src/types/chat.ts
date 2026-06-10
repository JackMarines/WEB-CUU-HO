export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatRequest {
  message: string;
  callId?: number;
  history?: ChatMessage[];
}

export interface ChatResponse {
  reply: string;
  suggestions?: string[];
  data?: Record<string, unknown>;
}
