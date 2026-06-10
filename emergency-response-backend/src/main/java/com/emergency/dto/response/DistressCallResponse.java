package com.emergency.dto.response;

import com.emergency.model.DistressCall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public record DistressCallResponse(
    Long id,
    Long userId,
    String userName,
    String callerName,
    String callerPhone,
    DisasterTypeResponse disasterType,
    BigDecimal lat,
    BigDecimal lng,
    String locationName,
    String description,
    String status,
    int urgencyScore,
    int personCount,
    String imageUrl,
    List<String> suggestedSupplies,
    String createdAt,
    String resolvedAt
) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static DistressCallResponse fromEntity(DistressCall call) {
        List<String> supplies = parseSuggestedSupplies(call.getSuggestedSupplies());

        return new DistressCallResponse(
            call.getId(),
            call.getUser() != null ? call.getUser().getId() : null,
            call.getUser() != null ? call.getUser().getName() : null,
            call.getCallerName(),
            call.getCallerPhone(),
            DisasterTypeResponse.fromEntity(call.getDisasterType()),
            call.getLat(),
            call.getLng(),
            call.getLocationName(),
            call.getDescription(),
            call.getStatus().name(),
            call.getUrgencyScore(),
            call.getPersonCount(),
            call.getImageUrl(),
            supplies,
            call.getCreatedAt() != null ? call.getCreatedAt().toString() : null,
            call.getResolvedAt() != null ? call.getResolvedAt().toString() : null
        );
    }

    private static List<String> parseSuggestedSupplies(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
