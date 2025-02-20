package com.RealEstateDevelopment.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pending_properties")
public class PendingProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long propertyId;

    private String title;
    private Double price;
    private Double size;
    private String address;
    private Integer yearBuilt;
    private String propertyType;
    private Integer bedrooms;
    private Integer bathrooms;

    @ElementCollection
    private List<String> amenities;

    private String features;

    @Enumerated(EnumType.STRING)
    private Status status; // "PENDING", "REJECTED"

    @Enumerated(EnumType.STRING)
    private Status availability; // "AVAILABLE", "RENTED", "SOLD"

    @Lob
    @ElementCollection
    @CollectionTable(name = "pendingProperty_images", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private List<byte[]> images = new ArrayList<>();

    private String proximity;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent; // Linking property to an agent

    @OneToMany(mappedBy = "pendingProperty", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("pendingProperty-location")
    private List<Location> locations = new ArrayList<>();

}
