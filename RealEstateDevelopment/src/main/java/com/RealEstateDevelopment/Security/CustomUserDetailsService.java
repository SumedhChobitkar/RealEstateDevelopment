package com.RealEstateDevelopment.Security;

import com.RealEstateDevelopment.Entity.User;
import com.RealEstateDevelopment.Entity.Admin;
import com.RealEstateDevelopment.Entity.Agent;
import com.RealEstateDevelopment.Repository.UserRepository;
import com.RealEstateDevelopment.Repository.AdminRepository;
import com.RealEstateDevelopment.Repository.AgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final AgentRepository agentRepository;

    public CustomUserDetailsService(UserRepository userRepository, AdminRepository adminRepository, AgentRepository agentRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.agentRepository = agentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user details for username: {}", username);

        // Check if the user is an Admin
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            logger.info("Admin '{}' found with role: ADMIN", username);
            return new org.springframework.security.core.userdetails.User(
                    admin.getUsername(),
                    admin.getPassword(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        // Check if the user is an Agent
        Optional<Agent> agentOpt = agentRepository.findByUserName(username);
        if (agentOpt.isPresent()) {
            Agent agent = agentOpt.get();
            logger.info("Agent '{}' found with role: AGENT", username);
            return new org.springframework.security.core.userdetails.User(
                    agent.getUserName(),
                    agent.getPassword(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_AGENT"))
            );
        }

        // Check if the user is a normal User
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found");
                });

        String role = (user.getRole() != null) ? user.getRole().name() : "USER"; // Default role if null

        logger.info("User '{}' found with role: {}", username, role);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}
