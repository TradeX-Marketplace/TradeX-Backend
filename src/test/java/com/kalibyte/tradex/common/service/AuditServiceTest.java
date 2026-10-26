package com.kalibyte.tradex.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalibyte.tradex.audit.entity.AuditLog;
import com.kalibyte.tradex.audit.repository.AuditLogRepository;
import com.kalibyte.tradex.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private ObjectMapper objectMapper;
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        auditService = new AuditService(auditLogRepository, objectMapper);
    }

    @Test
    @DisplayName("Log successful action")
    void testLogActionSuccess() {
        auditService.logAction(
                1L,
                AuditLog.AuditActionType.PARTY_REGISTERED,
                "Party registered successfully",
                "admin-001",
                AuditLog.ActorType.ADMIN,
                "127.0.0.1"
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals(1L, savedLog.getPartyId());
        assertEquals(AuditLog.AuditActionType.PARTY_REGISTERED, savedLog.getActionType());
        assertEquals("Party registered successfully", savedLog.getActionDescription());
        assertEquals("admin-001", savedLog.getActorId());
        assertEquals(AuditLog.ActorType.ADMIN, savedLog.getActorType());
        assertEquals(AuditLog.AuditStatus.SUCCESS, savedLog.getStatus());
        assertEquals("127.0.0.1", savedLog.getIpAddress());
    }

    @Test
    @DisplayName("Log action with old and new values")
    void testLogActionWithValues() {
        Map<String, Object> oldVal = Map.of("active", false);
        Map<String, Object> newVal = Map.of("active", true);

        auditService.logAction(
                1L,
                AuditLog.AuditActionType.PARTY_APPROVED,
                "Party approved by admin",
                "admin-001",
                AuditLog.ActorType.ADMIN,
                oldVal,
                newVal,
                "192.168.1.1"
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals(AuditLog.AuditActionType.PARTY_APPROVED, savedLog.getActionType());
        assertNotNull(savedLog.getOldValues());
        assertNotNull(savedLog.getNewValues());
        assertTrue(savedLog.getNewValues().contains("true"));
    }

    @Test
    @DisplayName("Log failed action")
    void testLogFailedAction() {
        auditService.logFailedAction(
                1L,
                AuditLog.AuditActionType.LOGIN_FAILED,
                "Invalid credentials",
                "user@example.com",
                AuditLog.ActorType.PARTY,
                "127.0.0.1",
                "Bad credentials"
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals(AuditLog.AuditStatus.FAILURE, savedLog.getStatus());
        assertEquals("Bad credentials", savedLog.getErrorMessage());
    }

    @Test
    @DisplayName("Get approval timeline for party")
    void testGetApprovalTimeline() {
        AuditLog log1 = AuditLog.builder()
                .partyId(1L)
                .actionType(AuditLog.AuditActionType.PARTY_REGISTERED)
                .build();
        AuditLog log2 = AuditLog.builder()
                .partyId(1L)
                .actionType(AuditLog.AuditActionType.PARTY_APPROVED)
                .build();

        when(auditLogRepository.findByPartyIdAndActionTypeInOrderByCreatedAtDesc(eq(1L), anyList()))
                .thenReturn(List.of(log2, log1));

        List<AuditLog> timeline = auditService.getApprovalTimeline(1L);

        assertEquals(2, timeline.size());
        assertEquals(AuditLog.AuditActionType.PARTY_APPROVED, timeline.get(0).getActionType());
        assertEquals(AuditLog.AuditActionType.PARTY_REGISTERED, timeline.get(1).getActionType());
    }
}
