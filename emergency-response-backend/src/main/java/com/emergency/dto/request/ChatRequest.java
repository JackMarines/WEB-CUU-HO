package com.emergency.dto.request;

import com.emergency.dto.ChatMessage;
import java.util.List;

public record ChatRequest(
    String message,
    Integer callId,
    List<ChatMessage> history
) {}
