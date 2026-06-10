package com.emergency.service;

import com.emergency.dto.request.FeedbackRequest;
import com.emergency.dto.request.ResponseRequest;
import com.emergency.dto.response.ResponseResponse;
import com.emergency.exception.ResourceNotFoundException;
import com.emergency.model.DistressCall;
import com.emergency.model.RescueCenter;
import com.emergency.model.Response;
import com.emergency.model.User;
import com.emergency.repository.DistressCallRepository;
import com.emergency.repository.RescueCenterRepository;
import com.emergency.repository.ResponseRepository;
import com.emergency.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ResponseService {

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private DistressCallRepository distressCallRepository;

    @Autowired
    private RescueCenterRepository rescueCenterRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ResponseResponse assign(Long callId, ResponseRequest request, Long userId) {
        DistressCall call = distressCallRepository.findById(callId)
            .orElseThrow(() -> new ResourceNotFoundException("Distress call not found"));

        RescueCenter center = rescueCenterRepository.findById(request.rescueCenterId())
            .orElseThrow(() -> new ResourceNotFoundException("Rescue center not found"));

        User assigner = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (call.getStatus() == DistressCall.CallStatus.active) {
            call.setStatus(DistressCall.CallStatus.in_progress);
            distressCallRepository.save(call);
        }

        Response response = new Response();
        response.setDistressCall(call);
        response.setRescueCenter(center);
        response.setAssignedBy(assigner);
        response.setNote(request.note());

        return ResponseResponse.fromEntity(responseRepository.save(response));
    }

    public List<ResponseResponse> getByCallId(Long callId) {
        return responseRepository.findByDistressCallIdOrderByCreatedAtDesc(callId).stream()
            .map(ResponseResponse::fromEntity)
            .toList();
    }

    @Transactional
    public ResponseResponse updateStatus(Long id, String status) {
        Response response = responseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Response not found"));

        Response.ResponseStatus newStatus;
        try {
            newStatus = Response.ResponseStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        response.setStatus(newStatus);
        return ResponseResponse.fromEntity(responseRepository.save(response));
    }

    @Transactional
    public ResponseResponse submitFeedback(Long callId, FeedbackRequest request, Long userId) {
        DistressCall call = distressCallRepository.findById(callId)
            .orElseThrow(() -> new ResourceNotFoundException("Distress call not found"));

        if (!call.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only submit feedback for your own calls");
        }

        if (call.getStatus() != DistressCall.CallStatus.resolved) {
            throw new IllegalArgumentException("Feedback is only allowed for resolved calls");
        }

        Response response = responseRepository.findTopByDistressCallIdOrderByCreatedAtDesc(callId)
            .orElseThrow(() -> new ResourceNotFoundException("No response found for this call"));

        response.setRating(request.rating());
        response.setFeedback(request.feedback());
        response.setFeedbackAt(java.time.LocalDateTime.now());

        return ResponseResponse.fromEntity(responseRepository.save(response));
    }
}
