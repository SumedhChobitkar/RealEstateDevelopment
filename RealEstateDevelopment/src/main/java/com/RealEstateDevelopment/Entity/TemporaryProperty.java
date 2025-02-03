package com.RealEstateDevelopment.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class TemporaryProperty {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long tempPropertyId;

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
        //        @ManyToOne // Many properties can belong to one agent
//        @JoinColumn(name = "agent_id") // Foreign key column in the property table
//        @JsonIgnore
//        private Agent agent;
//
//        @ManyToOne // Many properties can belong to one admin
//        @JoinColumn(name = "admin_id") // Foreign key column in the property table
//        @JsonIgnore
//        private Admin admin;
        @ManyToOne // Many properties can belong to one agent
        @JoinColumn(name = "agent_id") // Foreign key column in the property table
        @JsonIgnore // Prevent agent from being serialized
        private Agent agent;

        @ManyToOne // Many properties can belong to one admin
        @JoinColumn(name = "admin_id") // Foreign key column in the property table
        @JsonIgnore // Prevent admin from being serialized
        private Admin admin;
}