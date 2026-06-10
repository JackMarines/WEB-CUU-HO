package com.emergency.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DisasterTypeRequest(
    @NotBlank String name,
    @NotBlank String slug,
    String icon,
    @Min(0) @Max(100) int baseUrgencyScore
) {}
