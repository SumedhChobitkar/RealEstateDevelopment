package com.RealEstateDevelopment.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
@Setter
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
    private List<String> amenities = new ArrayList<>();
}
    // Initialize to avoid shared references    private String features;    private String status;    private List<String> galleryImages = new ArrayList<>(); // Initialize to avoid shared references    private String proximity;//    private String agentName;//    private String agentContact;    @ManyToOne // Many properties can belong to one agent    @JoinColumn(name = "agent_id") // Foreign key column in the property table    private Agent agent;    @ManyToOne    @JoinColumn(name = "admin_id")    private Admin admin;}