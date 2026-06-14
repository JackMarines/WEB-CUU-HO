package com.emergency.service;

import com.emergency.dto.request.DistressCallRequest;
import com.emergency.dto.response.DistressCallResponse;
import com.emergency.exception.ResourceNotFoundException;
import com.emergency.model.DisasterType;
import com.emergency.model.DistressCall;
import com.emergency.model.DistressCall.CallStatus;
import com.emergency.model.User;
import com.emergency.repository.DisasterTypeRepository;
import com.emergency.repository.DistressCallRepository;
import com.emergency.repository.RescueCenterRepository;
import com.emergency.repository.ResponseRepository;
import com.emergency.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CallService {

    @Autowired
    private DistressCallRepository distressCallRepository;

    @Autowired
    private DisasterTypeRepository disasterTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventEmitter eventEmitter;

    @Autowired
    private RescueCenterRepository rescueCenterRepository;

    @Autowired
    private ResponseRepository responseRepository;

    public List<DistressCallResponse> getAll(String type, String status, String q, String dateFrom, String dateTo) {
        java.time.LocalDateTime from = dateFrom != null ? java.time.LocalDate.parse(dateFrom).atStartOfDay() : null;
        java.time.LocalDateTime to = dateTo != null ? java.time.LocalDate.parse(dateTo).plusDays(1).atStartOfDay() : null;
        CallStatus statusEnum = status != null ? CallStatus.valueOf(status) : null;
        return distressCallRepository.findByFilters(type, statusEnum, q, from, to).stream()
            .map(DistressCallResponse::fromEntity)
            .toList();
    }

    public DistressCallResponse getById(Long id) {
        DistressCall call = distressCallRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Distress call not found"));
        return DistressCallResponse.fromEntity(call);
    }

    public List<DistressCallResponse> getMine(Long userId) {
        return distressCallRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(DistressCallResponse::fromEntity)
            .toList();
    }

    @Transactional
    public DistressCallResponse create(DistressCallRequest request, Long userId) {
        DisasterType dt = disasterTypeRepository.findById(request.disasterTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("Disaster type not found"));

        DistressCall call = new DistressCall();
        if (userId != null) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            call.setUser(user);
        }
        call.setDisasterType(dt);
        call.setLat(request.lat());
        call.setLng(request.lng());
        call.setLocationName(request.locationName());
        call.setDescription(request.description());
        call.setImageUrl(request.imageUrl());
        call.setCallerName(request.callerName());
        call.setCallerPhone(request.callerPhone());
        call.setPersonCount(request.personCount() != null ? request.personCount() : 1);
        call.setUrgencyScore(dt.getBaseUrgencyScore());

        DistressCallResponse response = DistressCallResponse.fromEntity(distressCallRepository.save(call));
        eventEmitter.emitNewCall(response);
        return response;
    }

    @Transactional
    public DistressCallResponse updateStatus(Long id, String status) {
        DistressCall call = distressCallRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Distress call not found"));

        DistressCall.CallStatus newStatus;
        try {
            newStatus = DistressCall.CallStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        DistressCall.CallStatus currentStatus = call.getStatus();

        if (newStatus != DistressCall.CallStatus.dismissed) {
            if (currentStatus == DistressCall.CallStatus.resolved || currentStatus == DistressCall.CallStatus.dismissed) {
                throw new IllegalStateException("Cannot update status of a " + currentStatus + " call");
            }
        }

        if (newStatus == DistressCall.CallStatus.in_progress && currentStatus != DistressCall.CallStatus.active) {
            throw new IllegalStateException("Only active calls can be marked in progress");
        }

        if (newStatus == DistressCall.CallStatus.resolved) {
            if (currentStatus != DistressCall.CallStatus.in_progress) {
                throw new IllegalStateException("Call must be in progress before it can be resolved");
            }
            call.setResolvedAt(java.time.LocalDateTime.now());
        }

        if (newStatus == DistressCall.CallStatus.active) {
            throw new IllegalStateException("Cannot revert to active status");
        }

        call.setStatus(newStatus);
        return DistressCallResponse.fromEntity(distressCallRepository.save(call));
    }

    @Transactional
    public void deleteCall(Long id) {
        if (!distressCallRepository.existsById(id)) {
            throw new ResourceNotFoundException("Distress call not found");
        }
        responseRepository.deleteByDistressCallId(id);
        distressCallRepository.deleteById(id);
    }

    public Map<String, Object> getDashboardStats() {
        long activeCount = distressCallRepository.countByStatus(DistressCall.CallStatus.active);
        long inProgressCount = distressCallRepository.countByStatus(DistressCall.CallStatus.in_progress);
        long resolvedCount = distressCallRepository.countByStatus(DistressCall.CallStatus.resolved);
        List<Object[]> typeCounts = distressCallRepository.countByDisasterType();

        List<Map<String, Object>> callsByType = typeCounts.stream()
            .map(row -> Map.of("type", row[0], "count", row[1]))
            .toList();

        List<DistressCallResponse> recent = distressCallRepository.findTop10ByOrderByCreatedAtDesc().stream()
            .map(DistressCallResponse::fromEntity)
            .toList();

        return Map.of(
            "activeCalls", activeCount,
            "inProgressCalls", inProgressCount,
            "resolvedCalls", resolvedCount,
            "totalCalls", activeCount + inProgressCount + resolvedCount,
            "totalCenters", rescueCenterRepository.count(),
            "callsByType", callsByType,
            "recentCalls", recent
        );
    }
}
