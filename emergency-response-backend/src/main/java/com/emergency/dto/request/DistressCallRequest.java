package com.emergency.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DistressCallRequest(
    @NotNull Long disasterTypeId,
    @NotNull BigDecimal lat,
    @NotNull BigDecimal lng,
    String locationName,
    @NotBlank String description,
    String imageUrl,
    @NotBlank String callerName,
    String callerPhone,
    @Min(1) Integer personCount
) {}
