package com.kalibyte.tradex.commodity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "commodities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"qualityGrades"})
@EqualsAndHashCode(exclude = {"qualityGrades"})
public class Commodity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String commodityCode; // Auto-generated: COM-0001, COM-0002, etc.
    
    @Column(nullable = false)
    private String commodityName;

    @Column(name = "parent_name")
    private String parentName;

    @Column(name = "quality_name")
    private String qualityName;
    
    @Column(nullable = false)
    private String category; // Example: Grains
    
    @Column(name = "sub_category")
    private String subCategory; // Example: Feed Grains
    
    @Column(nullable = false)
    private String defaultUnit; // MT / KG / Ton
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CommodityStatus status = CommodityStatus.ACTIVE; // ACTIVE / INACTIVE
    
    @OneToMany(mappedBy = "commodity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<QualityGrade> qualityGrades = new HashSet<>();
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum CommodityStatus {
        ACTIVE, INACTIVE
    }
}
