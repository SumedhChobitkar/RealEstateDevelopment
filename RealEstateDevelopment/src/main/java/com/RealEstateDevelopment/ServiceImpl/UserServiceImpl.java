package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.CommanUtil.ValidationClass;
import com.RealEstateDevelopment.Entity.Role;
import com.RealEstateDevelopment.Entity.Status;
import com.RealEstateDevelopment.Entity.User;
import com.RealEstateDevelopment.Repository.UserRepository;
import com.RealEstateDevelopment.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    @Override
    public User registerUser(User user, MultipartFile profilePicture) throws Exception {
        try {
            logger.info("Attempting to register user with username: {}", user.getUsername());

            // Validate user data before saving
            validateUserData(user);

            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                throw new Exception("Username already exists");
            }

            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new Exception("Email already registered");
            }

            if (profilePicture != null && !profilePicture.isEmpty()) {
                String contentType = profilePicture.getContentType();
                if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/jpg"))) {
                    throw new Exception("Invalid profile picture format. Only JPEG, JPG, and PNG are allowed.");
                }
                user.setProfilePicture(profilePicture.getBytes());
            }

            user.setStatus(Status.ACTIVE);
            user.setRole(Role.USER);
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            User savedUser = userRepository.save(user);
            logger.info("User registered successfully with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage(), e);
            throw new Exception("Error registering user: " + e.getMessage(), e);
        }
    }

    @Override
    public User loginUser(String username, String password) throws Exception {
        try {
            logger.info("Attempting to login user with username: {}", username);

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
                logger.info("User with username: {} logged in successfully.", username);
                return userOpt.get();
            }

            logger.warn("Invalid login attempt for username: {}", username);
            throw new Exception("Invalid username or password");
        } catch (Exception e) {
            logger.error("Error during user login: {}", e.getMessage(), e);
            throw new Exception("Error during user login: " + e.getMessage(), e);
        }
    }

    @Override
    public User updateUser(Long id, User updatedUser, MultipartFile profilePicture) throws Exception {
        try {
            logger.info("Attempting to update user with ID: {}", id);

            // Validate updated user data
            validateUserData(updatedUser);

            User existingUser = userRepository.findById(id).orElseThrow(() -> new Exception("User not found"));
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setFullname(updatedUser.getFullname());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setMobileNo(updatedUser.getMobileNo());
            existingUser.setRole(updatedUser.getRole());

            if (profilePicture != null && !profilePicture.isEmpty()) {
                existingUser.setProfilePicture(profilePicture.getBytes());
            }

            existingUser.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            User savedUser = userRepository.save(existingUser);
            logger.info("User with ID: {} updated successfully.", id);
            return savedUser;
        } catch (Exception e) {
            logger.error("Error updating user with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error updating user: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(Long id) throws Exception {
        try {
            logger.info("Attempting to delete user with ID: {}", id);

            User user = userRepository.findById(id).orElseThrow(() -> new Exception("User not found"));
            userRepository.delete(user);
            logger.info("User with ID: {} deleted successfully.", id);
        } catch (Exception e) {
            logger.error("Error deleting user with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error deleting user: " + e.getMessage(), e);
        }
    }

    @Override
    public User getUserById(Long id) throws Exception {
        try {
            logger.info("Fetching user details with ID: {}", id);
            return userRepository.findById(id).orElseThrow(() -> new Exception("User not found"));
        } catch (Exception e) {
            logger.error("Error fetching user with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error fetching user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> getAllUsers() {
        try {
            logger.info("Fetching all users.");
            return userRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching all users: " + e.getMessage(), e);
        }
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) throws Exception {
        try {
            logger.info("Attempting to change password for user with ID: {}", id);

            User user = userRepository.findById(id).orElseThrow(() -> new Exception("User not found"));
            if (!user.getPassword().equals(oldPassword)) {
                logger.warn("Old password does not match for user with ID: {}", id);
                throw new Exception("Old password is incorrect");
            }

            user.setPassword(newPassword);
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            logger.info("Password changed successfully for user with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error changing password for user with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error changing password: " + e.getMessage(), e);
        }
    }

    @Override
    public void logoutUser(String username) throws Exception {
        try {
            logger.info("Attempting to log out user with username: {}", username);
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                logger.warn("User not found for logout with username: {}", username);
                throw new Exception("User not found");
            }

            // Implement any required logout logic, e.g., token invalidation or session handling
            logger.info("User with username: {} logged out successfully.", username);
        } catch (Exception e) {
            logger.error("Error logging out user with username: {}: {}", username, e.getMessage(), e);
            throw new Exception("Error logging out user: " + e.getMessage(), e);
        }
    }

    private void validateUserData(User user) throws IllegalArgumentException {
        if (user.getUsername() == null || !ValidationClass.USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            throw new IllegalArgumentException("Username is required and should be alphanumeric.");
        }
        if (user.getEmail() == null || !ValidationClass.EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Email is not valid.");
        }
        if (user.getGender() == null || !ValidationClass.GENDER_PATTERN.matcher(user.getGender()).matches()) {
            throw new IllegalArgumentException("Gender is required and must be Male, Female, or Other.");
        }
        if (user.getMobileNo() == null || !ValidationClass.PHONE_PATTERN.matcher(user.getMobileNo()).matches()) {
            throw new IllegalArgumentException("Mobile number should be 10 digits.");
        }
        if (user.getAddress() == null || !ValidationClass.ADDRESS_PATTERN.matcher(user.getAddress()).matches()) {
            throw new IllegalArgumentException("Address is required and contains invalid characters.");
        }
        if (user.getPassword() == null || !ValidationClass.PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            throw new IllegalArgumentException("Password should be 6-20 characters, including at least one letter, one number, and one special character.");
        }
    }
}
