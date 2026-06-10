package com.emergency.dto.response;

import com.emergency.model.DisasterType;

public record DisasterTypeResponse(
    Long id,
    String name,
    String slug,
    String icon,
    int baseUrgencyScore,
    String createdAt
) {
    public static DisasterTypeResponse fromEntity(DisasterType dt) {
        return new DisasterTypeResponse(
            dt.getId(),
            dt.getName(),
            dt.getSlug(),
            dt.getIcon(),
            dt.getBaseUrgencyScore(),
            dt.getCreatedAt() != null ? dt.getCreatedAt().toString() : null
        );
    }
}
