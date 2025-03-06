package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.User;
import com.RealEstateDevelopment.Exception.UserNotFoundException;
import com.RealEstateDevelopment.Repository.ForgotPasswordOtpRepository;
import com.RealEstateDevelopment.Security.JwtUtil;
import com.RealEstateDevelopment.Service.EmailService;
import com.RealEstateDevelopment.Service.OTPService;
import com.RealEstateDevelopment.Service.UserService;
import com.RealEstateDevelopment.ServiceImpl.ForgotPasswordService;
import com.RealEstateDevelopment.ServiceImpl.LogoutService;
import com.RealEstateDevelopment.ServiceImpl.OTPServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ForgotPasswordOtpRepository otpRepository;

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LogoutService logoutService;

    @Autowired
    private OTPService otpService;


    @PostMapping("/registerTemporaryUser")
    public ResponseEntity<Map<String, String>> registerTemporaryUser(@RequestPart("userData") String userData,
                                                                     @RequestPart("profilePicture") MultipartFile multipartFile) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> userDataMap = objectMapper.readValue(userData, Map.class);
        User user = objectMapper.readValue(userData, User.class);
        try {    if (multipartFile != null && !multipartFile.isEmpty())
        {      String contentType = multipartFile.getContentType();
            if (contentType == null || !isValidImageType(contentType))
            {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid profile picture format. Only JPEG and PNG are supported."));
            }      user.setProfilePicture(multipartFile.getBytes());    } else {      user.setProfilePicture(null);    }
            String message = userService.registerTemporaryUser(user);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/verifyOtpToRegisterUser")
    public ResponseEntity<String> verifyUserOtpToRegisterUser(@RequestParam String email, @RequestParam String otp) {
        try {
            String message = userService.verifyOtpToRegister(email, otp);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/loginUser")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginDetails, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            logger.info("User login attempt...");
            String username = loginDetails.get("username");
            String password = loginDetails.get("password");

            if (username == null || password == null) {
                logger.warn("Username or password is missing.");
                responseBody.put("status", 400);
                responseBody.put("message", "Username and password are required.");
                return ResponseEntity.badRequest().body(responseBody);
            }

            // Authenticate and get user details
            Map<String, Object> loginResponse = userService.loginUser(username, password);

            //  Generate JWT token
            String token = jwtUtil.generateToken((Long) loginResponse.get("userId"), username, "USER");

            // Generate OTP
            ResponseEntity<String> stringResponseEntity = otpService.sendOTP(username);
            //  Return user details
            responseBody.put("status", 200);
            responseBody.put("data", loginResponse);
            responseBody.put("message", "User logged in successfully");

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            logger.error("Error during user login: {}", e.getMessage());
            responseBody.put("status", 400);
            responseBody.put("message", "Error during login: " + e.getMessage());
            return ResponseEntity.badRequest().body(responseBody);
        }
    }


    @PreAuthorize("hasRole('USER')")
    @PutMapping("/update/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId,
                                           @RequestPart("userData") String userData,
                                           @RequestPart(value = "profilePicture", required = false) MultipartFile multipartFile,
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

            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(userData, User.class);
            // If a profile picture is provided, validate and set it
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String contentType = multipartFile.getContentType();
                if (contentType == null || isValidImageType(contentType)) {
                    return ResponseEntity.badRequest().body(null);
                }
                user.setProfilePicture(multipartFile.getBytes());
            } else {
                User existingUser = userService.getUserById(userId);
                // If no new profile picture is provided, keep the existing one
                user.setProfilePicture(existingUser.getProfilePicture());
            }
            // Update user details
            User updatedUser = userService.updateUserDetails(userId, user);
            return ResponseEntity.ok(updatedUser);

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse user data: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("An error occurred while updating user with ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId,
                                                          @RequestHeader("Authorization") String authorizationHeader) {
        Map<String, Object> response = new HashMap<>();
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

            logger.info("Attempting to delete user with ID: {}", userId);
            userService.deleteUser(userId);
            logger.info("User with ID: {} deleted successfully.", userId);

            response.put("status", 200);
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during user deletion for ID {}: {}", userId, e.getMessage(), e);

            response.put("status", 400);
            response.put("message", "Error during user deletion: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/getUserById/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId,
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

            if (userId == null) {
                logger.warn("User ID cannot be null");
                return ResponseEntity.badRequest().body("User ID cannot be null");
            }
            // Retrieve the user
            User user = userService.getUserById(userId);
            // Return the retrieved user
            return ResponseEntity.ok(user);

        } catch (UserNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + userId);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while retrieving user with ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authorizationHeader) {
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

            logger.info("Fetching all users.");
            List<User> users = userService.getAllUsers();  // This should return List<User>
            logger.info("Retrieved {} users.", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching users: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/changePassword/{userId}")
    public ResponseEntity<?> changePassword(@PathVariable Long userId,
                                            @RequestParam String oldPassword,
                                            @RequestParam String newPassword,
                                            @RequestParam String confirmPassword,
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

            logger.info("Changing password for user ID: {}", userId);
            userService.changePassword(userId, oldPassword, newPassword, confirmPassword);
            logger.info("Password updated successfully for user ID: {}", userId);
            return ResponseEntity.ok("Password updated successfully");
        } catch (Exception e) {
            logger.error("Error changing password for user ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error changing password: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/user/profilePicture/delete/{userId}")
    public ResponseEntity<String> deleteProfilePicture(@PathVariable Long userId,
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

            logger.info("Received request to delete profile picture for user ID: {}", userId);
            // Call the service method to delete the profile picture
            String message = userService.deleteProfilePicture(userId);
            // Return a success response
            return ResponseEntity.ok(message);

        } catch (UserNotFoundException e) {
            logger.warn("Profile picture deletion failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }


        private boolean isValidImageType(String contentType) {
        return contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/png");
    }


    @PostMapping("/logoutUser")
    public ResponseEntity<Map<String, String>> logoutUser(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            logoutService.logout(request);
            response.put("message", "User logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error during user logout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
@PostMapping("/verifyOtp")
    public ResponseEntity<String> verifyOtp(@RequestParam String username, @RequestParam String otp) {
        try {
            return otpService.verifyOTP(username, otp);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
