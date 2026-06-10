package com.emergency.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CenterRequest(
    @NotBlank String name,
    @NotBlank String type,
    @NotNull BigDecimal lat,
    @NotNull BigDecimal lng,
    String address,
    String phone,
    String supplies,
    Integer capacity
) {}
