package com.kalibyte.tradex.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalibyte.tradex.audit.entity.AuditLog;
import com.kalibyte.tradex.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Log a successful action
     */
    public void logAction(Long partyId, AuditLog.AuditActionType actionType, 
                         String actionDescription, String actorId, 
                         AuditLog.ActorType actorType, String ipAddress) {
        logAction(partyId, actionType, actionDescription, actorId, actorType, 
                 null, null, ipAddress, AuditLog.AuditStatus.SUCCESS, null);
    }
    
    /**
     * Log action with old and new values
     */
    public void logAction(Long partyId, AuditLog.AuditActionType actionType,
                         String actionDescription, String actorId, 
                         AuditLog.ActorType actorType, Object oldValues, 
                         Object newValues, String ipAddress) {
        logAction(partyId, actionType, actionDescription, actorId, actorType,
                 oldValues, newValues, ipAddress, AuditLog.AuditStatus.SUCCESS, null);
    }
    
    /**
     * Log failed action
     */
    public void logFailedAction(Long partyId, AuditLog.AuditActionType actionType,
                               String actionDescription, String actorId,
                               AuditLog.ActorType actorType, String ipAddress, 
                               String errorMessage) {
        logAction(partyId, actionType, actionDescription, actorId, actorType,
                 null, null, ipAddress, AuditLog.AuditStatus.FAILURE, errorMessage);
    }
    
    /**
     * Main logging method
     */
    private void logAction(Long partyId, AuditLog.AuditActionType actionType,
                          String actionDescription, String actorId,
                          AuditLog.ActorType actorType, Object oldValues,
                          Object newValues, String ipAddress,
                          AuditLog.AuditStatus status, String errorMessage) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .partyId(partyId)
                    .actionType(actionType)
                    .actionDescription(actionDescription)
                    .actorId(actorId)
                    .actorType(actorType)
                    .oldValues(convertToJson(oldValues))
                    .newValues(convertToJson(newValues))
                    .ipAddress(ipAddress)
                    .status(status)
                    .errorMessage(errorMessage)
                    .build();
            
            auditLogRepository.save(auditLog);
            log.info("Audit log created: {} for party: {} by: {}", actionType, partyId, actorId);
        } catch (Exception e) {
            log.error("Error creating audit log", e);
        }
    }
    
    /**
     * Get audit logs for a party
     */
    public List<AuditLog> getPartyAuditLogs(Long partyId) {
        return auditLogRepository.findByPartyIdOrderByCreatedAtDesc(partyId);
    }
    
    /**
     * Get audit logs by action type
     */
    public List<AuditLog> getAuditLogsByAction(AuditLog.AuditActionType actionType) {
        return auditLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType);
    }
    
    /**
     * Get audit logs within date range
     */
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }
    
    /**
     * Get recent actions by actor
     */
    public List<AuditLog> getRecentActionsByActor(String actorId, int limit) {
        return auditLogRepository.findByActorIdOrderByCreatedAtDesc(actorId);
    }
    
    /**
     * Convert object to JSON string
     */
    private String convertToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error converting object to JSON", e);
            return obj.toString();
        }
    }
    
    /**
     * Get approval timeline for party
     */
    public List<AuditLog> getApprovalTimeline(Long partyId) {
        return auditLogRepository.findByPartyIdAndActionTypeInOrderByCreatedAtDesc(
                partyId,
                List.of(
                    AuditLog.AuditActionType.PARTY_REGISTERED,
                    AuditLog.AuditActionType.PARTY_CREATED_BY_ADMIN,
                    AuditLog.AuditActionType.PARTY_APPROVED,
                    AuditLog.AuditActionType.PARTY_REJECTED,
                    AuditLog.AuditActionType.PARTY_CHANGES_REQUESTED,
                    AuditLog.AuditActionType.PARTY_RESUBMITTED
                )
        );
    }
}
