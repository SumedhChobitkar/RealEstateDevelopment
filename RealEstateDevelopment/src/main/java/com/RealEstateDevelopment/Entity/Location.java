package com.RealEstateDevelopment.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    private String propertyName;;
    private double latitude;
    private double longitude;
    private String route;
    private double distanceInKm;

    @ManyToOne
    @JoinColumn(name = "pending_property_id")
    @JsonBackReference("pendingProperty-location")
    private PendingProperty pendingProperty; //  Link to PendingProperty

    @ManyToOne
    @JoinColumn(name = "property_id")
    @JsonBackReference("property-location")
    private PropertyNew property; //  Link to Approved Property
}
