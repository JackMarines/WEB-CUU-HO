package com.emergency.dto.response;

import com.emergency.model.RescueCenter;
import java.math.BigDecimal;

public record CenterResponse(
    Long id,
    String name,
    String type,
    BigDecimal lat,
    BigDecimal lng,
    String address,
    String phone,
    String supplies,
    Integer capacity,
    String createdAt
) {
    public static CenterResponse fromEntity(RescueCenter c) {
        return new CenterResponse(
            c.getId(),
            c.getName(),
            c.getType().name(),
            c.getLat(),
            c.getLng(),
            c.getAddress(),
            c.getPhone(),
            c.getSupplies(),
            c.getCapacity(),
            c.getCreatedAt() != null ? c.getCreatedAt().toString() : null
        );
    }
}
