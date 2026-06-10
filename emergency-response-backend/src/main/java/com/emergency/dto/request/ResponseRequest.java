package com.emergency.dto.request;

import jakarta.validation.constraints.NotNull;

public record ResponseRequest(
    @NotNull Long rescueCenterId,
    String note
) {}
