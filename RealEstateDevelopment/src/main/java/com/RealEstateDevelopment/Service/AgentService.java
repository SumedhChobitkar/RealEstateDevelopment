package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.Agent;
import com.RealEstateDevelopment.Entity.TemporaryAgent;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface AgentService {
    TemporaryAgent registerTemporaryAgent(TemporaryAgent agent, MultipartFile profilePicture) throws Exception;

    Map<String, Object> loginAgent(String username, String password) throws Exception;

    Agent updateAgent(Long id, Agent updatedAgent, MultipartFile profilePicture) throws Exception;

    void deleteAgent(Long id) throws Exception;

    void logoutAgent(Long id) throws Exception;

    Agent getAgentById(Long id) throws Exception;

    List<Agent> getAllAgents();

    void changeAgentPassword(Long id, String oldPassword, String newPassword) throws Exception;

    // new added methods

    Agent approveAgent(Long tempAgentId) throws Exception;
    void rejectAgent(Long tempAgentId) throws Exception;
    List<TemporaryAgent> getAllPendingAgents();
}
