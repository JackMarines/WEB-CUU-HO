package com.emergency.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "responses")
public class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distress_call_id", nullable = false)
    private DistressCall distressCall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescue_center_id", nullable = false)
    private RescueCenter rescueCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;

    @Enumerated(EnumType.STRING)
    private ResponseStatus status = ResponseStatus.assigned;

    @Column(columnDefinition = "TEXT")
    private String note;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "feedback_at")
    private LocalDateTime feedbackAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Response() {}

    public enum ResponseStatus { assigned, in_progress, delivered }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DistressCall getDistressCall() { return distressCall; }
    public void setDistressCall(DistressCall distressCall) { this.distressCall = distressCall; }
    public RescueCenter getRescueCenter() { return rescueCenter; }
    public void setRescueCenter(RescueCenter rescueCenter) { this.rescueCenter = rescueCenter; }
    public User getAssignedBy() { return assignedBy; }
    public void setAssignedBy(User assignedBy) { this.assignedBy = assignedBy; }
    public ResponseStatus getStatus() { return status; }
    public void setStatus(ResponseStatus status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public LocalDateTime getFeedbackAt() { return feedbackAt; }
    public void setFeedbackAt(LocalDateTime feedbackAt) { this.feedbackAt = feedbackAt; }
}
