package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.Admin;
import java.util.List;

public interface AdminService {
    String registerAdmin(Admin admin);
    String loginAdmin(String username, String password);
    void logoutAdmin(String username);
    void deleteAdmin(Long adminId);
    Admin getAdminById(Long adminId);
    Admin getAdminByUsername(String username);
    List<Admin> getAllAdmins();
    String updateAdmin(Long adminId,Admin admin);
}
