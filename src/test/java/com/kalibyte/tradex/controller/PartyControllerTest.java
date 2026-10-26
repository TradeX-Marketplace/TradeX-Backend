package com.kalibyte.tradex.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalibyte.tradex.party.dto.PartyDTO;
import com.kalibyte.tradex.party.dto.RoleDTO;
import com.kalibyte.tradex.party.dto.request.AddPartyRequest;
import com.kalibyte.tradex.common.exception.BadRequestException;
import com.kalibyte.tradex.common.exception.GlobalExceptionHandler;
import com.kalibyte.tradex.common.exception.ResourceNotFoundException;
import com.kalibyte.tradex.party.controller.PartyController;
import com.kalibyte.tradex.party.service.PartyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PartyControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private PartyService partyService;

    @InjectMocks
    private PartyController partyController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(partyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/parties - Add Party successfully")
    void testAddPartySuccess() throws Exception {
        AddPartyRequest request = AddPartyRequest.builder()
                .companyName("ABC Feed Ltd")
                .email("john@abcfeed.com")
                .phone("+91-9876543210")
                .contactPerson("John Doe")
                .roleIds(Set.of(2L, 3L))
                .build();

        RoleDTO role1 = RoleDTO.builder().id(2L).name("MANUFACTURER").build();
        RoleDTO role2 = RoleDTO.builder().id(3L).name("TRADER").build();

        PartyDTO responseDto = PartyDTO.builder()
                .id(1L)
                .partyCode("PTY001")
                .companyName("ABC Feed Ltd")
                .email("john@abcfeed.com")
                .phone("+91-9876543210")
                .contactPerson("John Doe")
                .roles(Set.of(role1, role2))
                .active(true)
                .build();

        when(partyService.addParty(any(AddPartyRequest.class), eq("admin-001"))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/parties")
                        .header("X-User-Id", "admin-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Party created successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.partyCode").value("PTY001"))
                .andExpect(jsonPath("$.data.companyName").value("ABC Feed Ltd"));
    }

    @Test
    @DisplayName("GET /api/v1/parties/{id} - Get Party by ID")
    void testGetPartyById() throws Exception {
        PartyDTO responseDto = PartyDTO.builder()
                .id(1L)
                .partyCode("PTY001")
                .companyName("ABC Feed Ltd")
                .email("john@abcfeed.com")
                .active(true)
                .build();

        when(partyService.getPartyById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/parties/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.companyName").value("ABC Feed Ltd"));
    }

    @Test
    @DisplayName("GET /api/v1/parties/code/{code} - Get Party by Code")
    void testGetPartyByCode() throws Exception {
        PartyDTO responseDto = PartyDTO.builder()
                .id(1L)
                .partyCode("PTY001")
                .companyName("ABC Feed Ltd")
                .email("john@abcfeed.com")
                .active(true)
                .build();

        when(partyService.getPartyByCode("PTY001")).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/parties/code/PTY001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.partyCode").value("PTY001"));
    }

    @Test
    @DisplayName("GET /api/v1/parties - Get All Parties paginated")
    void testGetAllParties() throws Exception {
        PartyDTO responseDto = PartyDTO.builder()
                .id(1L)
                .partyCode("PTY001")
                .companyName("ABC Feed Ltd")
                .email("john@abcfeed.com")
                .active(true)
                .build();

        Page<PartyDTO> page = new PageImpl<>(List.of(responseDto), PageRequest.of(0, 20), 1);
        when(partyService.getAllParties(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/parties?page=0&size=20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].companyName").value("ABC Feed Ltd"));
    }

    @Test
    @DisplayName("PUT /api/v1/parties/{id} - Update Party")
    void testUpdateParty() throws Exception {
        AddPartyRequest request = AddPartyRequest.builder()
                .companyName("ABC Feed Global Ltd")
                .email("john@abcfeed.com")
                .phone("+91-9876543210")
                .roleIds(Set.of(2L))
                .build();

        PartyDTO responseDto = PartyDTO.builder()
                .id(1L)
                .partyCode("PTY001")
                .companyName("ABC Feed Global Ltd")
                .email("john@abcfeed.com")
                .active(true)
                .build();

        when(partyService.updateParty(eq(1L), any(AddPartyRequest.class), eq("admin-001")))
                .thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/parties/1")
                        .header("X-User-Id", "admin-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Party updated successfully"))
                .andExpect(jsonPath("$.data.companyName").value("ABC Feed Global Ltd"));
    }

    @Test
    @DisplayName("PATCH /api/v1/parties/{id}/deactivate - Deactivate Party")
    void testDeactivateParty() throws Exception {
        PartyDTO responseDto = PartyDTO.builder()
                .id(1L)
                .partyCode("PTY001")
                .companyName("ABC Feed Ltd")
                .active(false)
                .build();

        when(partyService.deactivateParty(1L, "admin-001")).thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/parties/1/deactivate")
                        .header("X-User-Id", "admin-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Party deactivated successfully"))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    @DisplayName("PATCH /api/v1/parties/{id}/activate - Activate Party")
    void testActivateParty() throws Exception {
        PartyDTO responseDto = PartyDTO.builder()
                .id(1L)
                .partyCode("PTY001")
                .companyName("ABC Feed Ltd")
                .active(true)
                .build();

        when(partyService.activateParty(1L, "admin-001")).thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/parties/1/activate")
                        .header("X-User-Id", "admin-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Party activated successfully"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/parties/{id} - Not Found Handling")
    void testGetPartyNotFound() throws Exception {
        when(partyService.getPartyById(999L))
                .thenThrow(new ResourceNotFoundException("Party not found with id: 999"));

        mockMvc.perform(get("/api/v1/parties/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/parties - Duplicate Email Error Handling")
    void testAddPartyDuplicateEmail() throws Exception {
        AddPartyRequest request = AddPartyRequest.builder()
                .companyName("Duplicate Company")
                .email("existing@abcfeed.com")
                .phone("+91-9876543210")
                .roleIds(Set.of(2L))
                .build();

        when(partyService.addParty(any(AddPartyRequest.class), eq("admin-001")))
                .thenThrow(new BadRequestException("Party with email existing@abcfeed.com already exists"));

        mockMvc.perform(post("/api/v1/parties")
                        .header("X-User-Id", "admin-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }
}
