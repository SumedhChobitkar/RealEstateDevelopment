package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.Admin;
import com.RealEstateDevelopment.Entity.PropertyNew;
import com.RealEstateDevelopment.Exception.UserNotFoundException;
import com.RealEstateDevelopment.Repository.ForgotPasswordOtpRepository;
import com.RealEstateDevelopment.Security.JwtUtil;
import com.RealEstateDevelopment.Service.AdminService;
import com.RealEstateDevelopment.Service.AgentService;
import com.RealEstateDevelopment.Service.EmailService;
import com.RealEstateDevelopment.Service.PropertyNewService;
import com.RealEstateDevelopment.ServiceImpl.ForgotPasswordService;
import com.RealEstateDevelopment.ServiceImpl.LogoutService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private PropertyNewService propertyNewService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LogoutService logoutService;

    @PostMapping("/registerAdmin")
    public ResponseEntity<Map<String, String>> registerAdmin(
            @RequestParam("adminData") String adminData,
            @RequestParam(value = "profilePicture", required = false) MultipartFile multipartFile) {

        Map<String, String> response = new HashMap<>();
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
                    response.put("message", "Invalid profile picture format. Only JPEG and PNG are supported.");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                admin.setProfilePicture(null); // No profile picture, setting to null
            }

            // Register the admin
            adminService.registerAdmin(admin);
            response.put("message", "Successfully registered Admin");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (JsonProcessingException e) {
            logger.error("Error parsing admin data: {}", e.getMessage());
            response.put("message", "Invalid admin data format.");
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            logger.error("Error processing profile picture: {}", e.getMessage());
            response.put("message", "Error processing the profile picture.");
            return ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error during registration: {}", e.getMessage());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // Helper method to validate image types
    private boolean isValidImageType(String contentType) {
        return contentType.equalsIgnoreCase("image/jpeg") ||
                contentType.equalsIgnoreCase("image/png") ||
                contentType.equalsIgnoreCase("image/jpg");
    }

    @PostMapping("/loginAdmin")
    public ResponseEntity<Map<String, Object>> loginAdmin(@RequestBody Map<String, String> loginDetails, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            logger.info("Admin login attempt...");
            String username = loginDetails.get("username");
            String password = loginDetails.get("password");

            if (username == null || password == null) {
                logger.warn("Username or password is missing.");
                responseBody.put("status", 400);
                responseBody.put("message", "Username and password are required.");
                return ResponseEntity.badRequest().body(responseBody);
            }

            // Authenticate and get user details
            Map<String, Object> loginResponse = adminService.loginAdmin(username, password);

            // Generate JWT Token
            String token = jwtUtil.generateToken((Long) loginResponse.get("adminId"), username, "ADMIN");

            // Return user details
            responseBody.put("status", 200);
            responseBody.put("data", loginResponse);
            responseBody.put("message", "Admin logged in successfully");

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            logger.error("Error during admin login: {}", e.getMessage());
            responseBody.put("status", 400);
            responseBody.put("message", "Error during admin login: " + e.getMessage());
            return ResponseEntity.badRequest().body(responseBody);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteAdmin/{adminId}")
    public ResponseEntity<Map<String, String>> deleteAdmin(@PathVariable Long adminId,
                                                           @RequestHeader("Authorization") String authorizationHeader) {

        Map<String, String> response = new HashMap<>();

        try {

            // Extract token from the Authorization header
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new SecurityException("Missing or invalid Authorization header");
            }
            String token = authorizationHeader.substring(7); // Remove "Bearer "

            // Validate token
            boolean isValidToken = jwtUtil.validateToken(token);
            if (!isValidToken) {
                throw new SecurityException("Invalid or expired token");
            }

            // Get the adminId and check if it is not null
            if (adminId == null) {
                throw new UserNotFoundException("Admin ID cannot be null");
            }
            adminService.deleteAdmin(adminId);
            response.put("message", "Successfully deleted admin");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (UserNotFoundException e) {
            response.put("message", "Admin not found with ID: " + adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAdminById/{adminId}")
    public ResponseEntity<?> getAdminById(@PathVariable Long adminId,
                                          @RequestHeader("Authorization") String authorizationHeader) {
        try {

            // Extract token from the Authorization header
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new SecurityException("Missing or invalid Authorization header");
            }
            String token = authorizationHeader.substring(7); // Remove "Bearer "

            // Validate token
            boolean isValidToken = jwtUtil.validateToken(token);
            if (!isValidToken) {
                throw new SecurityException("Invalid or expired token");
            }

            // Get the adminId and check if it is not null
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAdminByUsername/{username}")
    public ResponseEntity<?> getAdminByUsername(@PathVariable String username,
                                                @RequestHeader("Authorization") String authorizationHeader) {
        try {

            // Extract token from the Authorization header
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new SecurityException("Missing or invalid Authorization header");
            }
            String token = authorizationHeader.substring(7); // Remove "Bearer "

            // Validate token
            boolean isValidToken = jwtUtil.validateToken(token);
            if (!isValidToken) {
                throw new SecurityException("Invalid or expired token");
            }

            // Get the admin Username and check if it is not null
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAllAdmins")
    public ResponseEntity<?> getAllAdmins(@RequestHeader("Authorization") String authorizationHeader) {
        try {

            // Extract token from the Authorization header
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new SecurityException("Missing or invalid Authorization header");
            }
            String token = authorizationHeader.substring(7); // Remove "Bearer "

            // Validate token
            boolean isValidToken = jwtUtil.validateToken(token);
            if (!isValidToken) {
                throw new SecurityException("Invalid or expired token");
            }

            return ResponseEntity.ok(adminService.getAllAdmins());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateAdmin/{adminId}")
    public ResponseEntity<String> updateAdmin(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long adminId,
            @RequestParam(value = "adminData") String adminData,
            @RequestParam(value = "profilePicture", required = false) MultipartFile multipartFile) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {

            // Extract token from the Authorization header
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new SecurityException("Missing or invalid Authorization header");
            }
            String token = authorizationHeader.substring(7); // Remove "Bearer "

            // Validate token
            boolean isValidToken = jwtUtil.validateToken(token);
            if (!isValidToken) {
                throw new SecurityException("Invalid or expired token");
            }

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

    // Update Property with agent and properties
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateAgentAndProperty/{propertyId}")
    public ResponseEntity<?> updateAgentAndProperty(
                                            @RequestHeader("Authorization") String authorizationHeader,
                                            @PathVariable Long propertyId,
                                            @RequestPart("property") PropertyNew updatedProperty,
                                            @RequestPart(value = "images", required = false) List<MultipartFile> newImages) {
        try {

            // Extract token from the Authorization header
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new SecurityException("Missing or invalid Authorization header");
            }
            String token = authorizationHeader.substring(7); // Remove "Bearer "

            // Validate token
            boolean isValidToken = jwtUtil.validateToken(token);
            if (!isValidToken) {
                throw new SecurityException("Invalid or expired token");
            }

            logger.info("Updating property with ID: {}", propertyId);
            PropertyNew updated = adminService.updateAgentAndProperty(propertyId, updatedProperty, newImages);
            logger.info("Property with ID {} updated successfully", propertyId);
            return ResponseEntity.ok(updated);  // Return updated property
        } catch (Exception e) {
            logger.error("Error updating property with ID {}: {}", propertyId, e.getMessage());
            return ResponseEntity.status(500).body("Error updating property: " + e.getMessage());
        }
    }


    @PostMapping("/logoutAdmin")
    public ResponseEntity<Map<String, String>> logoutAdmin(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            logoutService.logout(request);
            response.put("message", "Admin logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error during admin logout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
