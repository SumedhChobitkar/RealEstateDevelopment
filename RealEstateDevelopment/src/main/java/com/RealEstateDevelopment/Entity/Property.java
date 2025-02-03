package com.RealEstateDevelopment.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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


    private List<String> amenities = new ArrayList<>(); // Initialize to avoid shared references

    private String features;
    private String status;


    private List<String> galleryImages = new ArrayList<>(); // Initialize to avoid shared references

    private String proximity;
//    private String agentName;
//    private String agentContact;
//    @ManyToOne // Many properties can belong to one agent
//    @JoinColumn(name = "agent_id") // Foreign key column in the property table
//    @JsonIgnoreProperties("properties")
//    private Agent agent;
//
//    @ManyToOne
//    @JoinColumn(name = "admin_id")
//    @JsonIgnoreProperties("properties")
//    private Admin admin;

    @ManyToOne // Many properties can belong to one agent
    @JoinColumn(name = "agent_id") // Foreign key column in the property table
    @JsonIgnoreProperties("properties") // Ignore the properties field in Agent
    private Agent agent;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    @JsonIgnoreProperties("properties") // Ignore the properties field in Admin
    private Admin admin;
}