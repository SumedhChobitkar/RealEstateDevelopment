package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Entity.Location;
import com.RealEstateDevelopment.Entity.PropertyNew;
import com.RealEstateDevelopment.Handler.LocationWebSocketHandler;
import com.RealEstateDevelopment.Repository.LocationRepository;

import com.RealEstateDevelopment.Repository.PropertyNewRepository;
import com.RealEstateDevelopment.Service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LocationServiceImpl implements LocationService {
    private static final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PropertyNewRepository propertyRepository;

    @Autowired
    private LocationWebSocketHandler locationWebSocketHandler;


    @Override
    public Location saveLocation(Long propertyId, Location location) {
        PropertyNew property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found with ID: " + propertyId));

        location.setProperty(property);
        Location savedLocation = locationRepository.save(location);

        // Send WebSocket message
        try {
            locationWebSocketHandler.broadcastMessage(savedLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedLocation;
    }


    @Override
    public List<Location> getLocationsByPropertyTitle(String title) {
        try {
            List<Location> locations = locationRepository.findByPropertyTitle(title);

            if (locations.isEmpty()) {
                throw new RuntimeException("No locations found for property title: " + title);
            }

            return locations;
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            throw new RuntimeException("An error occurred while fetching locations: " + e.getMessage());
        }
    }

    @Override
    public void deleteLocation(Long locationId) {
        try {
            if (!locationRepository.existsById(locationId)) {
                throw new RuntimeException("Location not found with ID: " + locationId);
            }
            locationRepository.deleteById(locationId);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting location with ID: " + locationId + ". " + e.getMessage());
        }
    }


    @Override
    public List<Location> getLocationsByName(String name) {
        try {
            List<Location> locations = locationRepository.findByName(name);
            if (!locations.isEmpty()) {
                return locations;
            } else {
                throw new RuntimeException("No locations found with name: " + name);
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching locations: " + e.getMessage());
        }
    }


    @Override
    public Location getLocationById(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found with ID: " + locationId));

        // Send real-time location update via WebSocket
        try {
            locationWebSocketHandler.sendLocationUpdate(location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public List<Location> getAllLocations() {
        try {
            logger.info("Fetching all locations...");
            List<Location> locations = locationRepository.findAll();

            if (locations.isEmpty()) {
                logger.warn("No locations found in the database.");
                throw new RuntimeException("No locations found.");
            }

            logger.info("Successfully fetched {} locations.", locations.size());
            return locations;
        } catch (Exception e) {
            logger.error("Error while fetching locations: {}", e.getMessage(), e);
            throw new RuntimeException("An error occurred while fetching locations: " + e.getMessage());
        }
    }
}

//    public String getGoogleMapsLink(Long locationId) {
//        Location location = getLocationById(locationId);
//        return "https://www.google.com/maps/search/?api=1&query="
//                + location.getLatitude() + "," + location.getLongitude();
//    }
//
//}
