package com.RealEstateDevelopment.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class PropertyNew {

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
    private String City;

    @ElementCollection
    private List<String> amenities;

    private String features;

    @Enumerated(EnumType.STRING)
    private Status status; // "PENDING", "ACTIVE", "REJECTED"

    @Enumerated(EnumType.STRING)
    private Status availability; // "AVAILABLE", "RENTED", "SOLD"

    @Lob
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "property_images", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private List<byte[]> galleryImages = new ArrayList<>(); // Store images as byte arrays

    private String proximity;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent; // The relationship to Agent

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true,  fetch = FetchType.EAGER)
    @JsonManagedReference("property-location")
    private List<Location> locations = new ArrayList<>();
}
