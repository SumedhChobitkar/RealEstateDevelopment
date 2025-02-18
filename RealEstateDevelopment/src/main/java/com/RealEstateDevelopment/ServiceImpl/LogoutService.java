package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Entity.BlacklistedToken;
import com.RealEstateDevelopment.Repository.BlacklistedTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class LogoutService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public LogoutService(BlacklistedTokenRepository blacklistedTokenRepository) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authHeader.substring(7).trim();

        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setExpiryDate(new Date()); // Store current time

        blacklistedTokenRepository.save(blacklistedToken);
    }
}
