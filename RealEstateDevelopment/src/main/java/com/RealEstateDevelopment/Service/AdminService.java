package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.Admin;
import com.RealEstateDevelopment.Entity.PropertyNew;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AdminService {
    String registerAdmin(Admin admin);
    Map<String, Object> loginAdmin(String username, String password) throws Exception;
    void logoutAdmin(String username);
    void deleteAdmin(Long adminId);
    Admin getAdminById(Long adminId);
    Admin getAdminByUsername(String username);
    List<Admin> getAllAdmins();
    String updateAdmin(Long adminId,Admin admin);
    PropertyNew updateAgentAndProperty(Long propertyId, PropertyNew updatedProperty, List<MultipartFile> newImages);
}
