package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Entity.Agent;
import com.RealEstateDevelopment.Entity.Role;
import com.RealEstateDevelopment.Entity.Status;
import com.RealEstateDevelopment.Repository.AgentRepository;
import com.RealEstateDevelopment.Service.AgentService;
import com.RealEstateDevelopment.CommanUtil.ValidationClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AgentServiceImpl implements AgentService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);

    @Override
    public Agent registerAgent(Agent agent, MultipartFile profilePicture) throws Exception {
        try {
            logger.info("Attempting to register agent with username: {}", agent.getUsername());

            // Validate agent data
            validateAgentData(agent);

            if (profilePicture != null && !isValidImageType(profilePicture)) {
                throw new IllegalArgumentException("Invalid profile picture type. Only PNG, JPG, and JPEG are allowed.");
            }

            // Encrypt the password before saving
            agent.setPassword(passwordEncoder.encode(agent.getPassword()));

            agent.setProfilePicture(profilePicture != null ? profilePicture.getBytes() : null);
            agent.setCreatedAt(Timestamp.from(Instant.now()));
            agent.setUpdatedAt(Timestamp.from(Instant.now()));
            agent.setStatus(Status.ACTIVE);
            agent.setRole(Role.AGENT);

            Agent savedAgent = agentRepository.save(agent);
            logger.info("Agent registered successfully with ID: {}", savedAgent.getId());
            return savedAgent;
        } catch (Exception e) {
            logger.error("Error registering agent: {}", e.getMessage(), e);
            throw new Exception("Error registering agent: " + e.getMessage(), e);
        }
    }

    @Override
    public Agent loginAgent(String username, String password) throws Exception {
        try {
            logger.info("Attempting to login agent with username: {}", username);
            Optional<Agent> optionalAgent = agentRepository.findByUsername(username);

            if (optionalAgent.isEmpty()) {
                logger.warn("Invalid login attempt for username: {}", username);
                throw new IllegalArgumentException("Invalid username or password.");
            }

            Agent agent = optionalAgent.get();

            // Compare raw password with the encoded password
            if (!passwordEncoder.matches(password, agent.getPassword())) {
                logger.warn("Invalid login attempt for username: {}", username);
                throw new IllegalArgumentException("Invalid username or password.");
            }

            logger.info("Agent with username: {} logged in successfully.", username);
            return agent;
        } catch (Exception e) {
            logger.error("Error during agent login: {}", e.getMessage(), e);
            throw new Exception("Error during agent login: " + e.getMessage(), e);
        }
    }

    @Override
    public Agent updateAgent(Long id, Agent updatedAgent, MultipartFile profilePicture) throws Exception {
        try {
            logger.info("Attempting to update agent with ID: {}", id);

            // Validate agent data
            validateAgentData(updatedAgent);

            Agent existingAgent = agentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agent not found."));

            if (profilePicture != null && !isValidImageType(profilePicture)) {
                throw new IllegalArgumentException("Invalid profile picture type. Only PNG, JPG, and JPEG are allowed.");
            }

            // Update fields
            existingAgent.setFullname(updatedAgent.getFullname());
            existingAgent.setEmail(updatedAgent.getEmail());
            existingAgent.setMobileNo(updatedAgent.getMobileNo());
            existingAgent.setBio(updatedAgent.getBio());
            existingAgent.setExperience(updatedAgent.getExperience());
            existingAgent.setProfilePicture(profilePicture != null ? profilePicture.getBytes() : existingAgent.getProfilePicture());
            existingAgent.setUpdatedAt(Timestamp.from(Instant.now()));

            // Update password only if a new one is provided
            if (updatedAgent.getPassword() != null && !updatedAgent.getPassword().isEmpty()) {
                logger.info("Updating password for agent with ID: {}", id);
                existingAgent.setPassword(passwordEncoder.encode(updatedAgent.getPassword()));
            }

            Agent savedAgent = agentRepository.save(existingAgent);
            logger.info("Agent with ID: {} updated successfully.", id);
            return savedAgent;
        } catch (Exception e) {
            logger.error("Error updating agent with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error updating agent: " + e.getMessage(), e);
        }
    }


    @Override
    public void deleteAgent(Long id) throws Exception {
        try {
            logger.info("Attempting to delete agent with ID: {}", id);
            Agent agent = agentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agent not found."));
            agentRepository.delete(agent);
            logger.info("Agent with ID: {} deleted successfully.", id);
        } catch (Exception e) {
            logger.error("Error deleting agent with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error deleting agent: " + e.getMessage(), e);
        }
    }

    @Override
    public void logoutAgent(Long id) throws Exception {
        try {
            // Placeholder for logout logic, e.g., invalidating a session
            logger.info("Attempting to log out agent with ID: {}", id);
            // Logout logic goes here (e.g., invalidate session or token)
            logger.info("Agent with ID: {} logged out successfully.", id);
        } catch (Exception e) {
            logger.error("Error logging out agent with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error logging out agent: " + e.getMessage(), e);
        }
    }

    @Override
    public Agent getAgentById(Long id) throws Exception {
        try {
            logger.info("Fetching agent details with ID: {}", id);
            return agentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agent not found."));
        } catch (Exception e) {
            logger.error("Error fetching agent with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error fetching agent: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Agent> getAllAgents() {
        try {
            logger.info("Fetching all agents.");
            return agentRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all agents: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching all agents: " + e.getMessage(), e);
        }
    }

    @Override
    public void changeAgentPassword(Long id, String oldPassword, String newPassword) throws Exception {
        try {
            logger.info("Attempting to change password for agent with ID: {}", id);
            Agent agent = agentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agent not found."));

            // Check if old password matches the stored encoded password
            if (!passwordEncoder.matches(oldPassword, agent.getPassword())) {
                logger.warn("Old password does not match for agent with ID: {}", id);
                throw new IllegalArgumentException("Old password is incorrect.");
            }

            // Encode new password before saving
            agent.setPassword(passwordEncoder.encode(newPassword));
            agent.setUpdatedAt(Timestamp.from(Instant.now()));
            agentRepository.save(agent);
            logger.info("Password changed successfully for agent with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error changing password for agent with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error changing password: " + e.getMessage(), e);
        }
    }

    private boolean isValidImageType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("image/png") || contentType.equals("image/jpeg") || contentType.equals("image/jpg"));
    }

    // Validation method for agent data
    private void validateAgentData(Agent agent) {
        if (agent.getUsername() == null || !ValidationClass.USERNAME_PATTERN.matcher(agent.getUsername()).matches()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (agent.getEmail() == null || !ValidationClass.EMAIL_PATTERN.matcher(agent.getEmail()).matches()) {
            throw new IllegalArgumentException("Email is not valid.");
        }
        if (agent.getMobileNo() == null || !ValidationClass.PHONE_PATTERN.matcher(agent.getMobileNo()).matches()) {
            throw new IllegalArgumentException("Mobile number should be 10 digits.");
        }
    }
}
