package com.emergency.dto.response;

import com.emergency.model.Response;

public record ResponseResponse(
    Long id,
    Long distressCallId,
    Long rescueCenterId,
    String rescueCenterName,
    Long assignedBy,
    String assignedByName,
    String status,
    String note,
    String createdAt,
    String updatedAt,
    Integer rating,
    String feedback,
    String feedbackAt
) {
    public static ResponseResponse fromEntity(Response response) {
        return new ResponseResponse(
            response.getId(),
            response.getDistressCall().getId(),
            response.getRescueCenter().getId(),
            response.getRescueCenter().getName(),
            response.getAssignedBy().getId(),
            response.getAssignedBy().getName(),
            response.getStatus().name(),
            response.getNote(),
            response.getCreatedAt() != null ? response.getCreatedAt().toString() : null,
            response.getUpdatedAt() != null ? response.getUpdatedAt().toString() : null,
            response.getRating(),
            response.getFeedback(),
            response.getFeedbackAt() != null ? response.getFeedbackAt().toString() : null
        );
    }
}
