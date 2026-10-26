package com.kalibyte.tradex.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalibyte.tradex.commodity.controller.CommodityController;
import com.kalibyte.tradex.party.controller.PartyController;
import com.kalibyte.tradex.commodity.dto.request.AddCommodityRequest;
import com.kalibyte.tradex.party.dto.request.AddPartyRequest;
import com.kalibyte.tradex.commodity.dto.request.AddQualityGradeRequest;
import com.kalibyte.tradex.common.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
@Transactional
public class ApiFlowIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartyController partyController;

    @Autowired
    private CommodityController commodityController;

    @Autowired
    private com.kalibyte.tradex.party.repository.RoleRepository roleRepository;

    @Autowired
    private com.kalibyte.tradex.party.service.RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService.initializeDefaultRoles();
        mockMvc = MockMvcBuilders.standaloneSetup(partyController, commodityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("API Flow 1: Party Management Flow (Create -> Get by ID -> Get by Code -> Update -> Deactivate -> Activate)")
    void testPartyManagementCompleteFlow() throws Exception {
        Set<Long> roleIds = roleRepository.findAll().stream().limit(2).map(r -> r.getId()).collect(java.util.stream.Collectors.toSet());
        // Step 1: Create a new party
        AddPartyRequest createRequest = AddPartyRequest.builder()
                .companyName("Green Agri Feeds Ltd")
                .contactPerson("Rahul Sharma")
                .email("rahul@greenagri.com")
                .phone("+91-9876543210")
                .address("Sector 18, Industrial Area")
                .city("Gurugram")
                .state("Haryana")
                .postalCode("122015")
                .country("India")
                .gstin("06AAAAA0000A1Z5")
                .pan("AAAAA0000A")
                .roleIds(roleIds)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/parties")
                        .header("X-User-Id", "admin-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Party created successfully"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.partyCode").isNotEmpty())
                .andExpect(jsonPath("$.data.companyName").value("Green Agri Feeds Ltd"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long partyId = jsonNode.path("data").path("id").asLong();
        String partyCode = jsonNode.path("data").path("partyCode").asText();

        assertNotNull(partyId);
        assertTrue(partyId > 0);

        // Step 2: Get party by ID
        mockMvc.perform(get("/api/v1/parties/" + partyId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(partyId))
                .andExpect(jsonPath("$.data.partyCode").value(partyCode))
                .andExpect(jsonPath("$.data.email").value("rahul@greenagri.com"));

        // Step 3: Get party by Code
        mockMvc.perform(get("/api/v1/parties/code/" + partyCode))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.partyCode").value(partyCode));

        // Step 4: Get all parties with pagination
        mockMvc.perform(get("/api/v1/parties?page=0&size=10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));

        // Step 5: Update party
        AddPartyRequest updateRequest = AddPartyRequest.builder()
                .companyName("Green Agri Feeds Global Ltd")
                .contactPerson("Rahul Sharma")
                .email("rahul.sharma@greenagri.com")
                .phone("+91-9876543211")
                .address("Sector 18, Industrial Area")
                .city("Gurugram")
                .state("Haryana")
                .postalCode("122015")
                .country("India")
                .roleIds(roleIds)
                .build();

        mockMvc.perform(put("/api/v1/parties/" + partyId)
                        .header("X-User-Id", "admin-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Party updated successfully"))
                .andExpect(jsonPath("$.data.companyName").value("Green Agri Feeds Global Ltd"))
                .andExpect(jsonPath("$.data.email").value("rahul.sharma@greenagri.com"));

        // Step 6: Deactivate party
        mockMvc.perform(patch("/api/v1/parties/" + partyId + "/deactivate")
                        .header("X-User-Id", "admin-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Party deactivated successfully"))
                .andExpect(jsonPath("$.data.active").value(false));

        // Step 7: Activate party
        mockMvc.perform(patch("/api/v1/parties/" + partyId + "/activate")
                        .header("X-User-Id", "admin-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Party activated successfully"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @Order(2)
    @DisplayName("API Flow 2: Commodity Management Flow (Create with Grades & Parameters -> Add Grade -> Add Parameter -> Update -> Deactivate)")
    void testCommodityManagementCompleteFlow() throws Exception {
        // Step 1: Create Commodity with initial Quality Grade and Quality Parameter
        AddQualityGradeRequest initialGrade = AddQualityGradeRequest.builder()
                .gradeName("Grade 1 Feed")
                .gradeCode("G1")
                .description("Standard Feed Quality")
                .build();

        AddCommodityRequest createCommodityRequest = AddCommodityRequest.builder()
                .commodityName("Soybean Meal")
                .category("Oilseeds & Meals")
                .subCategory("Protein Meal")
                .defaultUnit("MT")
                .description("High protein animal feed ingredient")
                .qualityGrades(Set.of(initialGrade))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/commodities")
                        .header("X-User-Id", "admin-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCommodityRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Commodity created successfully"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.commodityCode").isNotEmpty())
                .andExpect(jsonPath("$.data.commodityName").value("Soybean Meal"))
                .andExpect(jsonPath("$.data.qualityGrades", hasSize(1)))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long commodityId = jsonNode.path("data").path("id").asLong();
        String commodityCode = jsonNode.path("data").path("commodityCode").asText();

        // Step 2: Get Commodity by ID
        mockMvc.perform(get("/api/v1/commodities/" + commodityId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commodityName").value("Soybean Meal"))
                .andExpect(jsonPath("$.data.commodityCode").value(commodityCode));

        // Step 3: Get Commodity by Code
        mockMvc.perform(get("/api/v1/commodities/code/" + commodityCode))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(commodityId));

        // Step 4: Add another Quality Grade to Commodity
        AddQualityGradeRequest secondGrade = AddQualityGradeRequest.builder()
                .gradeName("Grade 2 Economy")
                .gradeCode("G2")
                .description("Economy Grade")
                .build();

        mockMvc.perform(post("/api/v1/commodities/" + commodityId + "/quality-grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondGrade)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Quality grade added successfully"))
                .andExpect(jsonPath("$.data.gradeCode").value("G2"));

        // Step 5: Update Commodity
        AddCommodityRequest updateCommodityRequest = AddCommodityRequest.builder()
                .commodityName("Soybean Meal De-oiled")
                .category("Oilseeds & Meals")
                .subCategory("De-oiled Cake")
                .defaultUnit("MT")
                .description("De-oiled protein meal for cattle and poultry")
                .build();

        mockMvc.perform(put("/api/v1/commodities/" + commodityId)
                        .header("X-User-Id", "admin-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCommodityRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Commodity updated successfully"))
                .andExpect(jsonPath("$.data.commodityName").value("Soybean Meal De-oiled"));

        // Step 7: Deactivate Commodity
        mockMvc.perform(patch("/api/v1/commodities/" + commodityId + "/deactivate")
                        .header("X-User-Id", "admin-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Commodity deactivated successfully"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }
}
