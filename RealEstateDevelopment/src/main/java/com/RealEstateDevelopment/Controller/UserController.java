package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.User;
import com.RealEstateDevelopment.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class
UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/registerUser")
    public ResponseEntity<?> registerUser(
            @RequestPart("user") String userJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            logger.info("Starting user registration process...");
            // Parse the JSON String into a User object
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(userJson, User.class);
            logger.info("Parsed user object from request.");

            User registeredUser = userService.registerUser(user, profilePicture);
            logger.info("User registered successfully with ID: {}", registeredUser.getId());
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during registration: " + e.getMessage());
        }
    }

    @PostMapping("/loginUser")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginDetails) {
        try {
            logger.info("User login attempt...");
            String username = loginDetails.get("username");
            String password = loginDetails.get("password");

            if (username == null || password == null) {
                logger.warn("Username or password is missing.");
                return ResponseEntity.badRequest().body("Username and password are required.");
            }

            User user = userService.loginUser(username, password);
            logger.info("User logged in successfully: {}", username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error during login: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during login: " + e.getMessage());
        }
    }

    @PutMapping("/updateUser/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestPart("user") String updatedUserJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            logger.info("Starting user update process for ID: {}", userId);
            // Parse the JSON string into a User object
            ObjectMapper objectMapper = new ObjectMapper();
            User updatedUser = objectMapper.readValue(updatedUserJson, User.class);
            logger.info("Parsed updated user object from request.");

            User user = userService.updateUser(userId, updatedUser, profilePicture);
            logger.info("User updated successfully with ID: {}", user.getId());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error during user update for ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during user update: " + e.getMessage());
        }
    }

    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            logger.info("Attempting to delete user with ID: {}", userId);
            userService.deleteUser(userId);
            logger.info("User with ID: {} deleted successfully.", userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            logger.error("Error during user deletion for ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during user deletion: " + e.getMessage());
        }
    }

    @GetMapping("/getUserById/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            logger.info("Fetching user with ID: {}", userId);
            User user = userService.getUserById(userId);
            logger.info("User retrieved successfully with ID: {}", userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error fetching user with ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching user: " + e.getMessage());
        }
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers() {
        try {
            logger.info("Fetching all users.");
            List<User> users = userService.getAllUsers();  // This should return List<User>
            logger.info("Retrieved {} users.", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching users: " + e.getMessage());
        }
    }


    @PutMapping("/changePassword/{userId}")
    public ResponseEntity<?> changePassword(@PathVariable Long userId,
                                            @RequestParam String oldPassword,
                                            @RequestParam String newPassword) {
        try {
            logger.info("Changing password for user ID: {}", userId);
            userService.changePassword(userId, oldPassword, newPassword);
            logger.info("Password updated successfully for user ID: {}", userId);
            return ResponseEntity.ok("Password updated successfully");
        } catch (Exception e) {
            logger.error("Error changing password for user ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error changing password: " + e.getMessage());
        }
    }

    @PostMapping("/logoutUser")
    public ResponseEntity<?> logoutUser(@RequestParam String username) {
        try {
            logger.info("User logout attempt for username: {}", username);
            userService.logoutUser(username);
            logger.info("User {} logged out successfully.", username);
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            logger.error("Error during logout for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during logout: " + e.getMessage());
        }
    }
}
