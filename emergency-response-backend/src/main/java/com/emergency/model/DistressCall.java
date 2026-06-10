package com.emergency.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "distress_calls")
public class DistressCall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "caller_name")
    private String callerName;

    @Column(name = "caller_phone")
    private String callerPhone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "disaster_type_id", nullable = false)
    private DisasterType disasterType;

    @NotNull
    private BigDecimal lat;

    @NotNull
    private BigDecimal lng;

    private String locationName;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private CallStatus status = CallStatus.active;

    private int urgencyScore;

    private String imageUrl;

    @Column(name = "person_count")
    private Integer personCount = 1;

    @Column(columnDefinition = "JSON")
    private String suggestedSupplies;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;

    public DistressCall() {}

    public enum CallStatus { active, in_progress, resolved, dismissed }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public DisasterType getDisasterType() { return disasterType; }
    public void setDisasterType(DisasterType disasterType) { this.disasterType = disasterType; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public CallStatus getStatus() { return status; }
    public void setStatus(CallStatus status) { this.status = status; }
    public int getUrgencyScore() { return urgencyScore; }
    public void setUrgencyScore(int urgencyScore) { this.urgencyScore = urgencyScore; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSuggestedSupplies() { return suggestedSupplies; }
    public void setSuggestedSupplies(String suggestedSupplies) { this.suggestedSupplies = suggestedSupplies; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getCallerName() { return callerName; }
    public void setCallerName(String callerName) { this.callerName = callerName; }
    public String getCallerPhone() { return callerPhone; }
    public void setCallerPhone(String callerPhone) { this.callerPhone = callerPhone; }
    public Integer getPersonCount() { return personCount; }
    public void setPersonCount(Integer personCount) { this.personCount = personCount; }
}
