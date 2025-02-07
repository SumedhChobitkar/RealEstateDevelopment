package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.Property;
import com.RealEstateDevelopment.Entity.TemporaryProperty;
import com.RealEstateDevelopment.Service.PEmailService;
import com.RealEstateDevelopment.Service.PropertyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin("*")
public class PropertyController {

    private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);

    @Autowired
    private PropertyService propertyService;
    @Autowired
    private PEmailService PEmailService;


    @PostMapping("/saveProperty/{agentId}")
    public ResponseEntity<TemporaryProperty> saveProperty(
            @PathVariable Long agentId,
            @RequestParam(value = "profilePictures", required = false) MultipartFile[] files,
            @RequestParam("recipientEmail") String recipientEmail, // Add recipient email as a request parameter
            @RequestPart("property") String propertyJson,@RequestParam("video")MultipartFile video) { // Accept Property object directly
        try {
            logger.info("Request to save property with multiple images received");


            // Deserialize the JSON string to Property object
            ObjectMapper objectMapper = new ObjectMapper();
            Property property = objectMapper.readValue(propertyJson, Property.class);
            TemporaryProperty savedProperty = propertyService.saveProperty(property, files, agentId,video);

            // Send email notification after property is saved
            String subject = "New Property Added";
            String body = "A new property has been successfully added to the database. Property details: " + savedProperty.toString();

            PEmailService.sendEmail(recipientEmail, subject, body); // Use the recipient email from the request

            return ResponseEntity.ok(savedProperty);

        } catch (Exception e) {
            logger.error("Error while saving property", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/getAllProperties")
    public ResponseEntity<List<Property>> getAllProperties() {
        try {
            logger.info("Fetching all properties");
            List<Property> properties = propertyService.getAllProperties();
            return new ResponseEntity<>(properties, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while fetching properties: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/getPropertyById/{id}")
    public ResponseEntity<Property> getPropertyById(@PathVariable Long id) {
        try {
            logger.info("Fetching property by ID: {}", id);
            Property property = propertyService.getPropertyById(id);
            return new ResponseEntity<>(property, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while fetching property by ID: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping(value = "/updateProperty/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProperty(
            @PathVariable Long id,
            @RequestPart(required = false, value = "files") List<MultipartFile> files,
            @RequestPart("property") String propertyJson) {
        try {
            // Parse the JSON string into a Property object
            ObjectMapper objectMapper = new ObjectMapper();
            Property property = objectMapper.readValue(propertyJson, Property.class);

            // Call the service to update the property and handle files
            Property updatedProperty = propertyService.updateProperty(id, property, files);

            // Return the updated property in the response
            return new ResponseEntity<>(updatedProperty, HttpStatus.OK);

        } catch (JsonProcessingException e) {
            // Handle JSON parsing exceptions
            return new ResponseEntity<>("Invalid property JSON format", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Handle other exceptions
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @DeleteMapping("/deleteProperty/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        try {
            logger.info("Request for deleting property with ID: {}", id);
            propertyService.deleteProperty(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error while deleting property: {}", e.getMessage());
            throw e;
        }
    }

@GetMapping("/search")
public ResponseEntity<List<Property>> searchProperties(
        @RequestParam(value = "propertyType", required = false) String propertyType,
        @RequestParam(value = "minPrice", required = false) Double minPrice,
        @RequestParam(value = "maxPrice", required = false) Double maxPrice,
        @RequestParam(value = "bedrooms", required = false) Integer bedrooms,
        @RequestParam(value = "bathrooms", required = false) Integer bathrooms,
        @RequestParam(value = "location", required = false) String location,
        @RequestParam(value = "price", required = false) Double price){

        try {
            logger.info("Request received for searching properties with filters: propertyType={}, minPrice={}, maxPrice={}, bedrooms={}, bathrooms={}, location={}",
                    propertyType, minPrice, maxPrice, bedrooms, bathrooms, location);


            List<Property> properties = propertyService.searchProperties(propertyType, minPrice, maxPrice, bedrooms, bathrooms, location, price);

            // If no properties are found, return 204 No Content.
            if (properties.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            // If properties are found, return them with 200 OK status.
            return new ResponseEntity<>(properties, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error occurred while searching properties: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
