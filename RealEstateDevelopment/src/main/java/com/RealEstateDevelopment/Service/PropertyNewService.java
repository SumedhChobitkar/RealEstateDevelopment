package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.Location;
import com.RealEstateDevelopment.Entity.PendingProperty;
import com.RealEstateDevelopment.Entity.PropertyNew;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PropertyNewService {

//    @Transactional
//            PendingProperty addProperty(Long agentId, PendingProperty pendingProperty, List<MultipartFile> images, Location location) throws Exception;

    public PendingProperty addProperty(Long agentId, PendingProperty pendingProperty, List<MultipartFile> images, List<Location> locations);
    @Transactional
    PropertyNew approveProperty(Long propertyId) throws Exception;

    List<PendingProperty> getPendingPropertiesByAgent(Long agentId);

    PropertyNew getPropertyById(Long propertyId);
    List<PropertyNew> getAllProperties();
    List<PendingProperty> getAllPendingProperties();
    List<PropertyNew> getPropertiesByAgentId(Long agentId);
    List<PropertyNew> searchProperties(String keyword);

    @Transactional
    PropertyNew updateProperty(Long propertyId, PropertyNew updatedProperty, List<MultipartFile> newImages);

   // PropertyNew updateAgentAndProperty(Long propertyId, PropertyNew updatedProperty, List<MultipartFile> newImages);

    List<PropertyNew> searchProperty(String keyword, Double minPrice, Double maxPrice,
                                       String propertyType, Integer minBedrooms, Integer maxBedrooms,
                                       Integer minBathrooms, Integer maxBathrooms,
                                       String amenities, String features);

    List<PendingProperty> getPendingPropertiesByAgentId(Long agentId);
    void deleteProperty(Long propertyId);

    // property image methods

    @Transactional
    void rejectProperty(Long propertyId);

    void addImagesToProperty(Long propertyId, List<MultipartFile> images);
    void updateImages(Long propertyId, List<MultipartFile> images);
    List<byte[]> getImages(Long propertyId);

    @Transactional
    void deletePropertiesImages(Long propertyId);
}
