package com.emergency.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CallStatusRequest(
    @NotBlank String status
) {}
