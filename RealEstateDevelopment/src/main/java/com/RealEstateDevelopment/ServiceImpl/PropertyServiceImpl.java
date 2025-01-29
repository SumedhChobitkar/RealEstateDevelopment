package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Entity.Property;
import com.RealEstateDevelopment.Exceptions.PropertyNotFoundException;
import com.RealEstateDevelopment.Repository.propertyRepository;
import com.RealEstateDevelopment.Service.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PropertyServiceImpl implements PropertyService {

    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceImpl.class);

    @Autowired
    private propertyRepository propertyRepository;

    private static final String IMAGE_UPLOAD_DIR = "uploads/";  // Standardized upload directory

    @Override
    public Property saveProperty(Property property, MultipartFile[] files) {
        try {
            if (files != null && files.length > 0) {
                List<String> imagePaths = saveImages(List.of(files));
                property.setGalleryImages(imagePaths);
            }
            return propertyRepository.save(property);
        } catch (IOException e) {
            logger.error("Error saving images for property: {}", e.getMessage());
            throw new RuntimeException("Failed to save images", e);
        } catch (Exception e) {
            logger.error("Error saving property: {}", e.getMessage());
            throw new RuntimeException("Failed to save property", e);
        }
    }

    @Override
    public Property updateProperty(Long id, Property property, List<MultipartFile> files) {
        try {
            logger.info("Updating property with ID: {}", id);

            // Fetch the existing property from the repository
            Property existingProperty = propertyRepository.findById(id)
                    .orElseThrow(() -> new PropertyNotFoundException("Property not found with ID: " + id));

            // Update the property details
            existingProperty.setTitle(property.getTitle());
            existingProperty.setPrice(property.getPrice());
            existingProperty.setSize(property.getSize());
            existingProperty.setAddress(property.getAddress());
            existingProperty.setYearBuilt(property.getYearBuilt());
            existingProperty.setPropertyType(property.getPropertyType());
            existingProperty.setBedrooms(property.getBedrooms());
            existingProperty.setBathrooms(property.getBathrooms());
            existingProperty.setAmenities(property.getAmenities());
            existingProperty.setFeatures(property.getFeatures());
            existingProperty.setStatus(property.getStatus());
            existingProperty.setProximity(property.getProximity());
            existingProperty.setAgentName(property.getAgentName());
            existingProperty.setAgentContact(property.getAgentContact());

            // Handle file uploads if present
            if (files != null && !files.isEmpty()) {
                logger.info("Saving {} files for property with ID: {}", files.size(), id);
                List<String> imagePaths = saveImages(files);
                existingProperty.setGalleryImages(imagePaths); // Update gallery images
            }

            // Save and return the updated property
            return propertyRepository.save(existingProperty);

        } catch (PropertyNotFoundException e) {
            logger.error("Property with ID {} not found: {}", id, e.getMessage());
            throw e; // Re-throw the specific exception
        } catch (Exception e) {
            logger.error("Error occurred while updating property with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update property", e);
        }
    }


    @Override
    public void deleteProperty(Long id) {
        try {
            logger.info("Deleting property with ID: {}", id);
            Property property = propertyRepository.findById(id)
                    .orElseThrow(() -> new PropertyNotFoundException("Property not found with ID: " + id));
            propertyRepository.delete(property);
        } catch (Exception e) {
            logger.error("Error occurred while deleting property with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete property", e);
        }
    }

    @Override
    public Property getPropertyById(Long id) {
        try {
            logger.info("Fetching property with ID: {}", id);
            return propertyRepository.findById(id)
                    .orElseThrow(() -> new PropertyNotFoundException("Property not found with ID: " + id));
        } catch (Exception e) {
            logger.error("Error occurred while fetching property with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to fetch property", e);
        }
    }

    @Override
    public List<Property> getAllProperties() {
        try {
            logger.info("Fetching all properties");
            return propertyRepository.findAll();
        } catch (Exception e) {
            logger.error("Error occurred while fetching all properties: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch properties", e);
        }
    }

    @Override
    public List<Property> searchProperties(String propertyType, Double minPrice, Double maxPrice,
                                           Integer bedrooms, Integer bathrooms, String location) {
        try {
            logger.info("Searching properties with filters: propertyType={}, minPrice={}, maxPrice={}, bedrooms={}, bathrooms={}, location={}",
                    propertyType, minPrice, maxPrice, bedrooms, bathrooms, location);

            // Use filtering conditions only if a field is provided.
            List<Property> filteredProperties = propertyRepository.findAll().stream()
                    .filter(p -> propertyType == null || p.getPropertyType().equalsIgnoreCase(propertyType))  // Filter by property type
                    .filter(p -> minPrice == null || p.getPrice() >= minPrice)  // Filter by minimum price
                    .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)  // Filter by maximum price
                    .filter(p -> bedrooms == null || p.getBedrooms().equals(bedrooms))  // Filter by number of bedrooms
                    .filter(p -> bathrooms == null || p.getBathrooms().equals(bathrooms))  // Filter by number of bathrooms
                    .filter(p -> location == null || p.getAddress().contains(location))  // Filter by location (part of address match)
                    .collect(Collectors.toList());

            // If no properties match the criteria, log and return empty list.
            if (filteredProperties.isEmpty()) {
                logger.info("No properties found matching the given filters.");
            }

            return filteredProperties;
        } catch (Exception e) {
            logger.error("Error occurred while searching properties: {}", e.getMessage());
            throw new RuntimeException("Failed to search properties", e);
        }
    }


    // Helper method to save images and return their paths
    private List<String> saveImages(List<MultipartFile> files) throws IOException {
        List<String> imagePaths = new ArrayList<>();
        Path uploadPath = Paths.get(IMAGE_UPLOAD_DIR);

        // Ensure the directory exists or create it
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("Created directory for image uploads at: {}", uploadPath.toString());
        }

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // Generate a unique file name to prevent overwrites
                String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

                // Resolve the path for the new file
                Path filePath = uploadPath.resolve(uniqueFileName);

                try {
                    // Write the file to the resolved path
                    Files.write(filePath, file.getBytes());

                    // Add the file path to the list of saved image paths
                    imagePaths.add(filePath.toString());
                    logger.info("Saved file: {}", filePath.toString());
                } catch (IOException e) {
                    logger.error("Failed to save file: {}", file.getOriginalFilename(), e);
                    throw new IOException("Could not save file: " + file.getOriginalFilename(), e);
                }
            } else {
                logger.warn("Skipped an empty file during image upload.");
            }
        }

        return imagePaths;
    }
}