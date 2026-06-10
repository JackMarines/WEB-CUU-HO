package com.emergency.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "disaster_types")
public class DisasterType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @NotBlank
    @Column(unique = true)
    private String slug;

    private String icon;

    @Min(0) @Max(100)
    @Column(name = "base_urgency_score")
    private int baseUrgencyScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public DisasterType() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public int getBaseUrgencyScore() { return baseUrgencyScore; }
    public void setBaseUrgencyScore(int baseUrgencyScore) { this.baseUrgencyScore = baseUrgencyScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
