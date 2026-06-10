package com.emergency.dto.response;

import java.util.List;

public record ChatResponse(
    String reply,
    List<String> suggestions,
    Object data
) {}
