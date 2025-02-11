package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.Location;
import com.RealEstateDevelopment.Service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {
    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
    private final LocationService locationService;

    @PostMapping("/save/{propertyId}")
    public ResponseEntity<Location> saveLocation(@PathVariable Long propertyId, @RequestBody Location location) {
        Location savedLocation = locationService.saveLocation(propertyId, location);
        return ResponseEntity.ok(savedLocation);
    }


    @GetMapping("/{locationId}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long locationId) {
        Location location = locationService.getLocationById(locationId);
        return ResponseEntity.ok(location);
    }


    @GetMapping("/name/{name}")
    public ResponseEntity<?> getLocationsByName(@PathVariable String name) {
        try {
            List<Location> locations = locationService.getLocationsByName(name);
            return ResponseEntity.ok(locations);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/all")
    public ResponseEntity<?> getAllLocations() {
        try {
            logger.info("Received request to fetch all locations.");
            List<Location> locations = locationService.getAllLocations();

            if (locations.isEmpty()) {
                logger.warn("No locations found.");
                return ResponseEntity.status(404).body("No locations available.");
            }

            logger.info("Returning {} locations.", locations.size());
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            logger.error("Exception occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }


@DeleteMapping("/{locationId}")
public ResponseEntity<String> deleteLocation(@PathVariable Long locationId) {
    try {
        locationService.deleteLocation(locationId);
        return ResponseEntity.ok("Location with ID " + locationId + " deleted successfully.");
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

    @GetMapping("/{locationId}/map-link")
    public String getGoogleMapsLink(@PathVariable Long locationId) {
        Location location = locationService.getLocationById(locationId);
        return "https://www.google.com/maps/search/?api=1&query=" + location.getLatitude() + "," + location.getLongitude();
    }
}
