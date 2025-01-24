package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.User;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user, MultipartFile profilePicture) throws Exception;
    User loginUser(String username, String password) throws Exception;

    User updateUser(Long id, User updatedUser, MultipartFile profilePicture) throws Exception;

    void deleteUser(Long id) throws Exception;

    User getUserById(Long id) throws Exception;

    List<User> getAllUsers();

    void changePassword(Long id, String oldPassword, String newPassword) throws Exception;

    void logoutUser(String username) throws Exception;

}
