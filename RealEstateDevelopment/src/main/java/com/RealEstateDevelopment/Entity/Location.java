package com.RealEstateDevelopment.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double latitude;
    private double longitude;
    private String route;
    private double distanceInKm;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false) // Foreign Key to Property
    @JsonIgnore
    private PropertyNew property;
}
