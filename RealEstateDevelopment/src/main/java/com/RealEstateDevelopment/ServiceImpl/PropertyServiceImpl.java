package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Entity.Admin;
import com.RealEstateDevelopment.Entity.Agent;
import com.RealEstateDevelopment.Entity.Property;
import com.RealEstateDevelopment.Entity.TemporaryProperty;
import com.RealEstateDevelopment.Exceptions.PropertyNotFoundException;
import com.RealEstateDevelopment.Exceptions.UnauthorizedActionException;
import com.RealEstateDevelopment.Repository.AdminRepository;
import com.RealEstateDevelopment.Repository.AgentRepository;
import com.RealEstateDevelopment.Repository.TempPropertyRepository;
import com.RealEstateDevelopment.Repository.propertyRepository;
import com.RealEstateDevelopment.Service.PEmailService;
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
    @Autowired
    private PEmailService PEmailService;
    @Autowired
    private TempPropertyRepository tempPropertyRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private AdminRepository adminRepository;

    private static final String IMAGE_UPLOAD_DIR = "uploads/";  // Standardized upload directory


    @Override
    public TemporaryProperty saveProperty(Property property, MultipartFile[] files, Long agentId) {
        try {
            // Set the property status to PENDING
            property.setStatus("PENDING");

            // Find the agent by ID
            Agent agent = agentRepository.findById(agentId)
                    .orElseThrow(() -> new RuntimeException("Agent not found with ID: " + agentId));
            property.setAgent(agent); // Set the agent for the property
// Find the admin by ID (assuming you have the adminId available)
//           Admin admin1 =new Admin();
//           admin1.getAdminId();
            Admin admin = adminRepository.findById(property.getAdmin().getAdminId())
                    .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + property.getAdmin().getAdminId()));
            property.setAdmin(admin); // Set the admin for the property

//            sendRejectionNotification(property);

            // Create a TemporaryProperty object
            TemporaryProperty tempProperty = new TemporaryProperty();
            tempProperty.setTitle(property.getTitle());
            tempProperty.setPrice(property.getPrice());
            tempProperty.setSize(property.getSize());
            tempProperty.setAddress(property.getAddress());
            tempProperty.setYearBuilt(property.getYearBuilt());
            tempProperty.setPropertyType(property.getPropertyType());
            tempProperty.setBedrooms(property.getBedrooms());
            tempProperty.setBathrooms(property.getBathrooms());
            tempProperty.setAmenities(new ArrayList<>(property.getAmenities())); // Ensure a new list is created
            tempProperty.setFeatures(property.getFeatures());
            tempProperty.setStatus(property.getStatus());
            tempProperty.setProximity(property.getProximity());
//            agent1.setFullname(agent1.getFullname());

            tempProperty.setAgent(property.getAgent());
            tempProperty.setAdmin(property.getAdmin());
//            tempProperty.setAdmin(admin1);

            // Handle file uploads if present
            if (files != null && files.length > 0) {
                List<String> imagePaths = saveImages(List.of(files)); // Save images and get their paths
                tempProperty.setGalleryImages(imagePaths); // Set the uploaded image paths
            } else {
                tempProperty.setGalleryImages(new ArrayList<>()); // Initialize to an empty list if no files
            }

            // Save the temporary property
            TemporaryProperty savedTempProperty = tempPropertyRepository.save(tempProperty);
            logger.info("Temporary property saved successfully with ID: {}", savedTempProperty.getTempPropertyId());

//            sendApprovalNotification(savedTempProperty);
            notifyAdminAboutNewProperty(savedTempProperty);
            return savedTempProperty; // Return the saved temporary property
        } catch (Exception e) {
            logger.error("Failed to save property: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save property", e);
        }
    }

    private void sendDeletionEmail(Property property) {
        String adminEmail = "admin@example.com";
        String subject = "Property Deletion Request - " + property.getTitle();
        String body = "The property with title '" + property.getTitle() + "' has been deleted by the admin.";
        PEmailService.sendEmail(adminEmail, subject, body);
    }


    private void notifyAdminAboutNewProperty(TemporaryProperty property) {
        // Fetch the admin's email
        Admin admin = property.getAdmin(); // Assuming the property has an associated admin
        if (admin == null) {
            throw new RuntimeException("Admin not xx found for the property");
        }
        String adminEmail = admin.getEmail();
        String subject = "New Property Submitted - " + property.getTitle();
        String body = "A new property titled '" + property.getTitle() + "' has been submitted for approval.";

        // Send email to admin
        PEmailService.sendEmail(adminEmail, subject, body);
    }

    @Override
    public String approveProperty(Long tempPropertyId, Long adminId) {
        try {
            logger.info("Attempting to approve property with ID: {}", tempPropertyId);

            // Find the temporary property
            TemporaryProperty tempProperty = tempPropertyRepository.findById(tempPropertyId)
                    .orElseThrow(() -> new PropertyNotFoundException("Temporary property not found with ID: " + tempPropertyId));

            Admin admin = adminRepository.findById(adminId)
                    .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + adminId));

            // Create a new Property object
            Property property = new Property();
            property.setTitle(tempProperty.getTitle());
            property.setPrice(tempProperty.getPrice());
            property.setSize(tempProperty.getSize());
            property.setAddress(tempProperty.getAddress());
            property.setYearBuilt(tempProperty.getYearBuilt());
            property.setPropertyType(tempProperty.getPropertyType());
            property.setBedrooms(tempProperty.getBedrooms());
            property.setBathrooms(tempProperty.getBathrooms());
            property.setAmenities(new ArrayList<>(tempProperty.getAmenities())); // Create a new list to avoid shared references
            property.setFeatures(tempProperty.getFeatures());
            property.setStatus("APPROVED"); // Set status to APPROVED
            property.setGalleryImages(new ArrayList<>(tempProperty.getGalleryImages())); // Create a new list to avoid shared references
            property.setProximity(tempProperty.getProximity());
            property.setAdmin(admin);
            property.setAgent(tempProperty.getAgent());

            // Save the property in the main property table
            Property savedProperty = propertyRepository.save(property);

            // Optionally, delete the temporary property after approval
            tempPropertyRepository.delete(tempProperty);

            // Notify the agent about the approval
            sendApprovalNotification(savedProperty);
            return "Property approved successfully!";
        } catch (Exception e) {
            logger.error("Error occurred while approving property: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to approve property", e);
        }
    }

    @Override
    public String rejectProperty(Long tempPropertyId, Long adminId) {
        try {
            TemporaryProperty tempProperty = tempPropertyRepository.findById(tempPropertyId)
                    .orElseThrow(() -> new PropertyNotFoundException("Temporary property not found with ID: " + tempPropertyId));

            // Notify the agent about the rejection
            sendRejectionNotification(tempProperty);

            // Delete the temporary property
            tempPropertyRepository.delete(tempProperty);
            return "Property rejected successfully!";
        } catch (Exception e) {
            logger.error("Error occurred while rejecting property: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reject property", e);
        }
    }

    @Override
    public List<TemporaryProperty> getAllPendingProperties() {
        try {
            logger.info("Fetching all pending properties.");
            List<TemporaryProperty> pendingProperties = tempPropertyRepository.findAll().stream()
                    .filter(tempProperty -> "PENDING".equals(tempProperty.getStatus())) // Filter for pending properties
                    .collect(Collectors.toList());

            logger.info("Total pending properties found: {}", pendingProperties.size());
            return pendingProperties;
        } catch (Exception e) {
            logger.error("Error occurred while fetching pending properties: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch pending properties", e);
        }
    }

    private void sendApprovalNotification(Property property) {
        // Fetch the admin's email
        Admin admin = property.getAdmin(); // Assuming the property has an associated admin
        if (admin == null) {
            throw new RuntimeException("Admin not found for the property");
        }
        String adminEmail = admin.getEmail();
        String subjectForAdmin = "Property Approved - " + property.getTitle();
        String bodyForAdmin = "The property with title '" + property.getTitle() + "' has been approved.";

        // Send email to admin
        PEmailService.sendEmail(adminEmail, subjectForAdmin, bodyForAdmin);

        // Fetch the agent's email (assuming the agent is set in the property)
        Agent agent = property.getAgent(); // Assuming the property has an associated agent
        if (agent == null) {
            throw new RuntimeException("Agent not found for the property");
        }
        String recipientEmail = agent.getEmail(); // Assuming the agent has an email field
        String subjectForAgent = "Your Property Has Been Approved";
        String bodyForAgent = "Congratulations! Your property titled '" + property.getTitle() + "' has been approved.";

        // Send email to agent
        PEmailService.sendEmail(recipientEmail, subjectForAgent, bodyForAgent);
    }

    private void sendRejectionNotification(TemporaryProperty property) {
        // Fetch the agent's email (assuming the agent is set in the property)
        Agent agent = property.getAgent(); // Assuming the property has an associated agent
        if (agent == null) {
            throw new RuntimeException("Agent not found for the property");
        }
        String recipientEmail = agent.getEmail(); // Assuming the agent has an email field
        String subject = "Your Property Has Been Rejected";
        String body = "We regret to inform you that your property titled '" + property.getTitle() + "' has been rejected.";

        // Send email to agent
        PEmailService.sendEmail(recipientEmail, subject, body);
    }

    @Override
    public Property updateProperty(Long id, Property property, List<MultipartFile> files) {
        try {
            logger.info("Updating property with ID: {}", id);

            // Fetch the existing property from the repository
            Property existingProperty = propertyRepository.findById(id)
                    .orElseThrow(() -> new PropertyNotFoundException("Property not found with ID: " + id));

            Agent agent=new Agent();
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
//            agent.setFullname(agent.getFullname());

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
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found with ID: " + id));
    }


    @Override
    public void deleteProperty(Long id, String loggedInUserRole) {
        try {
            // Check if the logged-in user is an admin
            if (!"ADMIN".equalsIgnoreCase(loggedInUserRole)) {
                throw new UnauthorizedActionException("Only admin can delete properties.");
            }

            // Log the action
            logger.info("Deleting property with ID: {}", id);

            // Fetch the property from the repository
            Property property = propertyRepository.findById(id)
                    .orElseThrow(() -> new PropertyNotFoundException("Property not found with ID: " + id));

            // Set the status to 'DELETED BY ADMIN'
            property.setStatus("DELETED BY ADMIN");

            // Save the updated property status in the database
            propertyRepository.save(property);

            // Send the deletion email notification to the admin
            sendDeletionEmail(property);

        } catch (UnauthorizedActionException e) {
            // Log and handle unauthorized deletion attempt
            logger.error("Unauthorized deletion attempt by user with role {}: {}", loggedInUserRole, e.getMessage());
            throw e;  // Re-throw the custom exception
        } catch (Exception e) {
            // Log and handle any other exceptions
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

//    @Override
//    public List<Property> searchProperties(String propertyType, Double minPrice, Double maxPrice,
//                                           Integer bedrooms, Integer bathrooms, String location) {
//        try {
//            logger.info("Searching properties with filters: propertyType={}, minPrice={}, maxPrice={}, bedrooms={}, bathrooms={}, location={}",
//                    propertyType, minPrice, maxPrice, bedrooms, bathrooms, location);
//
//            // Use filtering conditions only if a field is provided.
//            List<Property> filteredProperties = propertyRepository.findAll().stream()
//                    .filter(p -> propertyType == null || p.getPropertyType().equalsIgnoreCase(propertyType))  // Filter by property type
//                    .filter(p -> minPrice == null || p.getPrice() >= minPrice)  // Filter by minimum price
//                    .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)  // Filter by maximum price
//                    .filter(p -> bedrooms == null || p.getBedrooms().equals(bedrooms))  // Filter by number of bedrooms
//                    .filter(p -> bathrooms == null || p.getBathrooms().equals(bathrooms))  // Filter by number of bathrooms
//                    .filter(p -> location == null || p.getAddress().contains(location))  // Filter by location (part of address match)
//                    .collect(Collectors.toList());
//
//            // If no properties match the criteria, log and return empty list.
//            if (filteredProperties.isEmpty()) {
//                logger.info("No properties found matching the given filters.");
//            }
//
//            return filteredProperties;
//        } catch (Exception e) {
//            logger.error("Error occurred while searching properties: {}", e.getMessage());
//            throw new RuntimeException("Failed to search properties", e);
//        }
//    }

    @Override
    public List<Property> searchProperties(String propertyType, Double minPrice, Double maxPrice,
                                           Integer bedrooms, Integer bathrooms, String location, Double price) {
        try {
            logger.info("Searching properties with filters: propertyType={}, minPrice={}, maxPrice={}, bedrooms={}, bathrooms={}, location={}, price={} ",
                    propertyType, minPrice, maxPrice, bedrooms, bathrooms, location, price);

            // Use filtering conditions only if a field is provided.
            List<Property> filteredProperties = propertyRepository.findAll().stream()
                    .filter(p -> propertyType == null || p.getPropertyType().equalsIgnoreCase(propertyType))  // Filter by property type
                    .filter(p -> minPrice == null || p.getPrice() >= minPrice)  // Filter by minimum price
                    .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)  // Filter by maximum price
                    .filter(p -> price == null || (p.getPrice() >= minPrice && p.getPrice() <= maxPrice)) // Check if price is within min and max range
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
