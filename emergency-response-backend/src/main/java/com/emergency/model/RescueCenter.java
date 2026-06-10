package com.emergency.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_centers")
public class RescueCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Enumerated(EnumType.STRING)
    private CenterType type;

    @NotNull
    private BigDecimal lat;

    @NotNull
    private BigDecimal lng;

    private String address;
    private String phone;

    @Column(columnDefinition = "JSON")
    private String supplies;

    private Integer capacity;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public RescueCenter() {}

    public enum CenterType { shelter, supply_distribution, rescue_team }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CenterType getType() { return type; }
    public void setType(CenterType type) { this.type = type; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSupplies() { return supplies; }
    public void setSupplies(String supplies) { this.supplies = supplies; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
