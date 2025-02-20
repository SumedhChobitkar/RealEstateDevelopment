package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.Location;

import java.util.List;

public interface LocationService {

    Location saveLocation(Long propertyId, Location location);

    List<Location> getLocationsByPropertyTitle(String propertyTitle);

    List<Location> getLocationsByName(String name);

    public Location getLocationById(Long locationId);

    void deleteLocation(Long locationId);

    List<Location> getAllLocations();

}
