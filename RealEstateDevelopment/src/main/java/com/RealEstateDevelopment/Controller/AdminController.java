package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.Admin;
import com.RealEstateDevelopment.Entity.TemporaryProperty;
import com.RealEstateDevelopment.Exception.UserNotFoundException;
import com.RealEstateDevelopment.Repository.ForgotPasswordOtpRepository;
import com.RealEstateDevelopment.Repository.TempPropertyRepository;
import com.RealEstateDevelopment.Service.AdminService;
import com.RealEstateDevelopment.Service.EmailService;
import com.RealEstateDevelopment.Service.PEmailService;
import com.RealEstateDevelopment.Service.PropertyService;
import com.RealEstateDevelopment.ServiceImpl.ForgotPasswordService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private ForgotPasswordOtpRepository otpRepository;  // Repository for OTPs

    @Autowired
    private EmailService emailService;

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @Autowired
    private PropertyService propertyService;
    @Autowired
    private TempPropertyRepository tempPropertyRepository;
    @Autowired
    private com.RealEstateDevelopment.Service.PEmailService PEmailService;

    @PostMapping("/registerAdmin")
    public ResponseEntity<String> registerAdmin(
            @RequestParam("adminData") String adminData,
            @RequestParam(value = "profilePicture", required = false) MultipartFile multipartFile) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parse the admin data from JSON
            Admin admin = objectMapper.readValue(adminData, Admin.class);

            // Handle profile picture upload if provided
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String contentType = multipartFile.getContentType();
                if (contentType != null && isValidImageType(contentType)) {
                    admin.setProfilePicture(multipartFile.getBytes());
                } else {
                    return ResponseEntity.badRequest().body("Invalid profile picture format. Only JPEG and PNG are supported.");
                }
            } else {
                admin.setProfilePicture(null);  // No profile picture, setting to null
            }

            // Register the admin
            adminService.registerAdmin(admin);
            return ResponseEntity.status(HttpStatus.OK).body("Successfully registered Admin");
        } catch (JsonProcessingException e) {
            logger.error("Error parsing admin data: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid admin data format.");
        } catch (IOException e) {
            logger.error("Error processing profile picture: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing the profile picture.");
        } catch (IllegalArgumentException e) {
            logger.error("Error during registration: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // Helper method to validate image types
    private boolean isValidImageType(String contentType) {
        return contentType.equalsIgnoreCase("image/jpeg") ||
                contentType.equalsIgnoreCase("image/png") ||
                contentType.equalsIgnoreCase("image/jpg");
    }

    @PostMapping("/loginAdmin")
    public ResponseEntity<String> loginAdmin(@RequestParam String username, @RequestParam String password) {
        try {
            logger.info("Login attempt for username: {}", username);
            String result = adminService.loginAdmin(username, password);
            return ResponseEntity.status(HttpStatus.OK).body("Admin Login Successfully");
        } catch (UserNotFoundException e) {
            logger.error("Error occurred during login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found with username: " + username);
        } catch (RuntimeException e) {
            logger.error("Invalid credentials for username: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @DeleteMapping("/deleteAdmin/{adminId}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long adminId) {
        try {
            if (adminId == null) {
                throw new UserNotFoundException("Admin id cannot be null");
            }
            adminService.deleteAdmin(adminId);
            return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted admin");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found with id: " + adminId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/logoutAdmin")
    public ResponseEntity<String> logoutAdmin(@RequestParam String username) {
        try {
            logger.info("Logout attempt for username: {}", username);
            adminService.logoutAdmin(username);
            return ResponseEntity.status(HttpStatus.OK).body("Admin logged out successfully.");
        } catch (UserNotFoundException e) {
            logger.error("Error occurred during logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found with username: " + username);
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


    @GetMapping("/getAdminById/{adminId}")
    public ResponseEntity<?> getAdminById(@PathVariable Long adminId) {
        try {
            if (adminId == null) {
                throw new IllegalArgumentException("Admin ID cannot be null.");
            }
            Admin admin = adminService.getAdminById(adminId);
            return ResponseEntity.ok(admin);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found with ID: " + adminId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/getAdminByUsername/{username}")
    public ResponseEntity<?> getAdminByUsername(@PathVariable String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("Username cannot be null or empty.");
            }
            Admin admin = adminService.getAdminByUsername(username);
            return ResponseEntity.ok(admin);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found with username: " + username);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/getAllAdmins")
    public ResponseEntity<?> getAllAdmins() {
        try {
            return ResponseEntity.ok(adminService.getAllAdmins());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/updateAdmin/{adminId}")
    public ResponseEntity<String> updateAdmin(
            @PathVariable Long adminId,
            @RequestParam(value = "adminData") String adminData,
            @RequestParam(value = "profilePicture", required = false) MultipartFile multipartFile) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parse the admin data from JSON
            Admin updatedAdmin = objectMapper.readValue(adminData, Admin.class);

            // Set the admin ID to ensure the correct admin is updated
            updatedAdmin.setAdminId(adminId);

            // If profile picture is uploaded, set it in the Admin entity
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String contentType = multipartFile.getContentType();
                if (contentType != null && isValidImageType(contentType)) {
                    updatedAdmin.setProfilePicture(multipartFile.getBytes());
                } else {
                    return ResponseEntity.badRequest().body("Invalid profile picture format. Only JPEG and PNG are supported.");
                }
            }

            // Call the service to update the admin data
            String result = adminService.updateAdmin(adminId, updatedAdmin);

            // Return the result of the update operation
            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (JsonProcessingException e) {
            logger.error("Error parsing admin data: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid admin data format.");
        } catch (IOException e) {
            logger.error("Error processing profile picture: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing the profile picture.");
        } catch (IllegalArgumentException e) {
            logger.error("Error during update: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/approve/{tempPropertyId}/{adminId}")
    public String approveProperty(@PathVariable Long tempPropertyId, @PathVariable Long adminId) {
        return propertyService.approveProperty(tempPropertyId, adminId);
    }
    @PostMapping("/reject/{tempPropertyId}/{adminId}")
    public String rejectProperty(@PathVariable Long tempPropertyId, @PathVariable Long adminId) {
        return propertyService.rejectProperty(tempPropertyId, adminId);
    }

    @GetMapping("/pending")
    public List<TemporaryProperty> getAllPendingProperties() {
        List<TemporaryProperty> pendingProperties = propertyService.getAllPendingProperties();
        logger.info("Fetched {} pending properties.", pendingProperties.size());
        return pendingProperties;
    }

}
