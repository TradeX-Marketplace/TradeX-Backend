package com.kalibyte.tradex.audit.repository;

import com.kalibyte.tradex.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Find by party ID
    List<AuditLog> findByPartyIdOrderByCreatedAtDesc(Long partyId);
    
    Page<AuditLog> findByPartyIdOrderByCreatedAtDesc(Long partyId, Pageable pageable);
    
    // Find by action type
    List<AuditLog> findByActionTypeOrderByCreatedAtDesc(AuditLog.AuditActionType actionType);
    
    // Find by date range
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by actor ID
    List<AuditLog> findByActorIdOrderByCreatedAtDesc(String actorId);
    
    // Find by actor type
    List<AuditLog> findByActorTypeOrderByCreatedAtDesc(AuditLog.ActorType actorType);
    
    // Find by status
    List<AuditLog> findByStatusOrderByCreatedAtDesc(AuditLog.AuditStatus status);
    
    // Find by action types (for approval timeline)
    List<AuditLog> findByPartyIdAndActionTypeInOrderByCreatedAtDesc(Long partyId, List<AuditLog.AuditActionType> actionTypes);
    
    // Combined queries
    @Query("SELECT al FROM AuditLog al WHERE al.partyId = :partyId AND al.actionType = :actionType ORDER BY al.createdAt DESC")
    List<AuditLog> findByPartyIdAndActionType(@Param("partyId") Long partyId, @Param("actionType") AuditLog.AuditActionType actionType);
    
    @Query("SELECT al FROM AuditLog al WHERE al.partyId = :partyId AND al.status = :status ORDER BY al.createdAt DESC")
    List<AuditLog> findByPartyIdAndStatus(@Param("partyId") Long partyId, @Param("status") AuditLog.AuditStatus status);
    
    // Count queries
    long countByPartyId(Long partyId);
    
    long countByActionType(AuditLog.AuditActionType actionType);
    
    long countByStatus(AuditLog.AuditStatus status);
}
