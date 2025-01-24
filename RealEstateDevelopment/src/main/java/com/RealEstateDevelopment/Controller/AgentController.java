package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.Agent;
import com.RealEstateDevelopment.Service.AgentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin("*")
public class AgentController {

    @Autowired
    private AgentService agentService;

    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);

    @PostMapping("/registerAgent")
    public ResponseEntity<?> registerAgent(
            @RequestPart("agent") String agentJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            logger.info("Attempting to register a new agent.");
            ObjectMapper objectMapper = new ObjectMapper();
            Agent agent = objectMapper.readValue(agentJson, Agent.class);

            Agent registeredAgent = agentService.registerAgent(agent, profilePicture);
            logger.info("Agent registered successfully with ID: {}", registeredAgent.getId());
            return ResponseEntity.ok(registeredAgent);
        } catch (Exception e) {
            logger.error("Error during agent registration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during agent registration: " + e.getMessage());
        }
    }

    @PostMapping("/loginAgent")
    public ResponseEntity<?> loginAgent(@RequestBody Map<String, String> loginDetails) {
        try {
            logger.info("Attempting to login agent with username: {}", loginDetails.get("username"));
            String username = loginDetails.get("username");
            String password = loginDetails.get("password");

            Agent agent = agentService.loginAgent(username, password);
            logger.info("Agent {} logged in successfully.", username);
            return ResponseEntity.ok(agent);
        } catch (Exception e) {
            logger.error("Error during agent login: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during agent login: " + e.getMessage());
        }
    }

    @PutMapping("/updateAgent/{agentId}")
    public ResponseEntity<?> updateAgent(
            @PathVariable Long agentId,
            @RequestPart("agent") String agentJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
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

    @DeleteMapping("/deleteAgent/{agentId}")
    public ResponseEntity<?> deleteAgent(@PathVariable Long agentId) {
        try {
            logger.info("Attempting to delete agent with ID: {}", agentId);
            agentService.deleteAgent(agentId);
            logger.info("Agent with ID: {} deleted successfully.", agentId);
            return ResponseEntity.ok("Agent deleted successfully.");
        } catch (Exception e) {
            logger.error("Error deleting agent with ID: {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error deleting agent: " + e.getMessage());
        }
    }

    @GetMapping("/getAgentById/{agentId}")
    public ResponseEntity<?> getAgentById(@PathVariable Long agentId) {
        try {
            logger.info("Fetching agent details with ID: {}", agentId);
            Agent agent = agentService.getAgentById(agentId);
            logger.info("Agent with ID: {} retrieved successfully.", agentId);
            return ResponseEntity.ok(agent);
        } catch (Exception e) {
            logger.error("Error fetching agent with ID: {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching agent: " + e.getMessage());
        }
    }

    @GetMapping("/getAllAgents")
    public ResponseEntity<?> getAllAgents() {
        try {
            logger.info("Fetching all agents.");
            List<Agent> agents = agentService.getAllAgents();
            logger.info("Successfully retrieved {} agents.", agents.size());
            return ResponseEntity.ok(agents);
        } catch (Exception e) {
            logger.error("Error fetching all agents: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching all agents: " + e.getMessage());
        }
    }

    @PutMapping("/changeAgentPassword/{agentId}")
    public ResponseEntity<?> changeAgentPassword(
            @PathVariable Long agentId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            logger.info("Attempting to change password for agent with ID: {}", agentId);
            agentService.changeAgentPassword(agentId, oldPassword, newPassword);
            logger.info("Password changed successfully for agent with ID: {}", agentId);
            return ResponseEntity.ok("Password changed successfully.");
        } catch (Exception e) {
            logger.error("Error changing password for agent with ID: {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error changing password: " + e.getMessage());
        }
    }
}
