package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.Agent;
import com.RealEstateDevelopment.Entity.TemporaryAgent;
import com.RealEstateDevelopment.Exception.AgentNotFoundException;
import com.RealEstateDevelopment.Security.JwtUtil;
import com.RealEstateDevelopment.Service.AgentService;
import com.RealEstateDevelopment.ServiceImpl.LogoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin("*")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LogoutService logoutService;

    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);


    @PostMapping("/registerTemporaryAgent")
    public ResponseEntity<Map<String, String>> registerTemporaryAgent(
            @RequestPart("agent") String agentJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

            Map<String, String> response = new HashMap<>();

        try {
            logger.info("Registering a new temporary agent.");
            ObjectMapper objectMapper = new ObjectMapper();
            TemporaryAgent agent = objectMapper.readValue(agentJson, TemporaryAgent.class);

            TemporaryAgent registeredAgent = agentService.registerTemporaryAgent(agent, profilePicture);
            logger.info("Temporary agent registered successfully with ID: {}", registeredAgent.getId());

            response.put("message", "Temporary agent registered successfully.");
            response.put("agentId", String.valueOf(registeredAgent.getId()));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during agent registration: {}", e.getMessage(), e);
            response.put("message", "Error during agent registration: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Approve an agent by admin.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approveAgent/{tempAgentId}")
    public ResponseEntity<Map<String, String>> approveAgent(@PathVariable Long tempAgentId, @RequestHeader("Authorization") String authorizationHeader) {

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

            logger.info("Approving agent with temporary ID: {}", tempAgentId);
            Agent approvedAgent = agentService.approveAgent(tempAgentId);
            logger.info("Agent approved successfully with ID: {}", approvedAgent.getId());
            response.put("message", "Agent approved successfully.");
            response.put("agentId", String.valueOf(approvedAgent.getId()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error approving agent: {}", e.getMessage(), e);
            response.put("message", "Error approving agent: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    /**
     * Reject an agent by admin.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/rejectAgent/{tempAgentId}")
    public ResponseEntity<Map<String, Object>> rejectAgent(@PathVariable Long tempAgentId, @RequestHeader("Authorization") String authorizationHeader) {

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

            logger.info("Rejecting agent with temporary ID: {}", tempAgentId);
            agentService.rejectAgent(tempAgentId);
            logger.info("Agent rejected successfully.");
            response.put("status", "success");
            response.put("message", "Agent rejected successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error rejecting agent: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Error rejecting agent: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all pending agent approvals.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAllPendingAgents")
    public ResponseEntity<List<TemporaryAgent>> getPendingAgents(@RequestHeader("Authorization") String authorizationHeader) {

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

        logger.info("Fetching all pending agents.");
        return ResponseEntity.ok(agentService.getAllPendingAgents());
    }

    @PostMapping("/loginAgent")
    public ResponseEntity<Map<String, Object>> loginAgent(@RequestBody Map<String, String> loginDetails, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            logger.info("Agent login attempt...");
            String username = loginDetails.get("username");
            String password = loginDetails.get("password");

            if (username == null || password == null) {
                logger.warn("Username or password is missing.");
                responseBody.put("status", 400);
                responseBody.put("message", "Username and password are required.");
                return ResponseEntity.badRequest().body(responseBody);
            }

            // Authenticate and get agent details
            Map<String, Object> loginResponse = agentService.loginAgent(username, password);

            // 🔹 Generate JWT token
            String token = jwtUtil.generateToken((Long) loginResponse.get("agentId"), username, (String) loginResponse.get("role"));

            // 🔹 Store token in an HttpOnly cookie
            Cookie jwtCookie = new Cookie("jwt_token", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true); // Enable only for HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60 * 24); // 1 day expiration
            response.addCookie(jwtCookie); // ✅ Store token in cookies

            // 🔹 Return agent details (without token in response body)
            responseBody.put("status", 200);
            responseBody.put("data", loginResponse);
            responseBody.put("message", "Agent logged in successfully");

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            logger.error("Error during agent login: {}", e.getMessage());
            responseBody.put("status", 400);
            responseBody.put("message", "Error during agent login: " + e.getMessage());
            return ResponseEntity.badRequest().body(responseBody);
        }
    }


    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/updateAgent/{agentId}")
    public ResponseEntity<?> updateAgent(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long agentId,
            @RequestPart("agent") String agentJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
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

            logger.info("Attempting to update agent with ID: {}", agentId);
            ObjectMapper objectMapper = new ObjectMapper();
            Agent updatedAgent = objectMapper.readValue(agentJson, Agent.class);

            Agent agent = agentService.updateAgent(agentId, updatedAgent, profilePicture);
            logger.info("Agent with ID: {} updated successfully.", agentId);
            return ResponseEntity.ok(agent);
        } catch (Exception e) {
            logger.error("Error updating agent with ID: {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating agent: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteAgent/{agentId}")
    public ResponseEntity<Map<String, Object>> deleteAgent(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Long agentId) {

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

            logger.info("Attempting to delete agent with ID: {}", agentId);
            agentService.deleteAgent(agentId);
            logger.info("Agent with ID: {} deleted successfully.", agentId);
            response.put("status", "success");
            response.put("message", "Agent deleted successfully.");
            response.put("agentId", agentId);
            return ResponseEntity.ok(response);
        } catch (AgentNotFoundException e) {
            logger.error("Agent not found with ID: {}", agentId);
            response.put("status", "error");
            response.put("message", "Agent not found.");
            response.put("agentId", agentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error deleting agent with ID: {}: {}", agentId, e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Error deleting agent: " + e.getMessage());
            response.put("agentId", agentId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @GetMapping("/getAgentById/{agentId}")
    public ResponseEntity<?> getAgentById(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Long agentId) {
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

            logger.info("Fetching agent details with ID: {}", agentId);
            Agent agent = agentService.getAgentById(agentId);
            logger.info("Agent with ID: {} retrieved successfully.", agentId);
            return ResponseEntity.ok(agent);
        } catch (Exception e) {
            logger.error("Error fetching agent with ID: {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching agent: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAllAgents")
    public ResponseEntity<?> getAllAgents(@RequestHeader("Authorization") String authorizationHeader) {
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

            logger.info("Fetching all agents.");
            List<Agent> agents = agentService.getAllAgents();
            logger.info("Successfully retrieved {} agents.", agents.size());
            return ResponseEntity.ok(agents);
        } catch (Exception e) {
            logger.error("Error fetching all agents: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching all agents: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/changeAgentPassword/{agentId}")
    public ResponseEntity<?> changeAgentPassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long agentId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
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

            logger.info("Attempting to change password for agent with ID: {}", agentId);
            agentService.changeAgentPassword(agentId, oldPassword, newPassword);
            logger.info("Password changed successfully for agent with ID: {}", agentId);
            return ResponseEntity.ok("Password changed successfully.");
        } catch (Exception e) {
            logger.error("Error changing password for agent with ID: {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error changing password: " + e.getMessage());
        }
    }


    @PostMapping("/logoutAgent")
    public ResponseEntity<Map<String, String>> logoutAgent(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            logoutService.logout(request);
            response.put("message", "Agent logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error during agent logout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
