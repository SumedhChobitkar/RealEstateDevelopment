package com.RealEstateDevelopment.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Property {
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
    private String status;

    @ElementCollection
    private List<String> galleryImages;

    private String proximity;

    private String agentName;
    private String agentContact;
}
