package com.kalibyte.tradex.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalibyte.tradex.commodity.dto.CommodityDTO;
import com.kalibyte.tradex.commodity.dto.QualityGradeDTO;
import com.kalibyte.tradex.commodity.dto.request.AddCommodityRequest;
import com.kalibyte.tradex.commodity.dto.request.AddQualityGradeRequest;
import com.kalibyte.tradex.commodity.entity.Commodity;
import com.kalibyte.tradex.common.exception.GlobalExceptionHandler;
import com.kalibyte.tradex.common.exception.ResourceNotFoundException;
import com.kalibyte.tradex.commodity.controller.CommodityController;
import com.kalibyte.tradex.commodity.service.CommodityService;
import java.math.BigDecimal;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CommodityControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CommodityService commodityService;

    @InjectMocks
    private CommodityController commodityController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(commodityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/commodities - Add Commodity successfully")
    void testAddCommoditySuccess() throws Exception {
        AddCommodityRequest request = AddCommodityRequest.builder()
                .commodityName("Maize")
                .category("Grains")
                .subCategory("Feed Grains")
                .defaultUnit("MT")
                .description("Raw material for animal feed")
                .build();

        CommodityDTO responseDto = CommodityDTO.builder()
                .id(1L)
                .commodityCode("CMD001")
                .commodityName("Maize")
                .category("Grains")
                .subCategory("Feed Grains")
                .defaultUnit("MT")
                .status(Commodity.CommodityStatus.ACTIVE)
                .build();

        when(commodityService.addCommodity(any(AddCommodityRequest.class), eq("admin-001")))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/commodities")
                        .header("X-User-Id", "admin-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Commodity created successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.commodityCode").value("CMD001"))
                .andExpect(jsonPath("$.data.commodityName").value("Maize"));
    }

    @Test
    @DisplayName("GET /api/v1/commodities/{id} - Get Commodity by ID")
    void testGetCommodityById() throws Exception {
        CommodityDTO responseDto = CommodityDTO.builder()
                .id(1L)
                .commodityCode("CMD001")
                .commodityName("Maize")
                .category("Grains")
                .status(Commodity.CommodityStatus.ACTIVE)
                .build();

        when(commodityService.getCommodityById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/commodities/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.commodityName").value("Maize"));
    }

    @Test
    @DisplayName("GET /api/v1/commodities/code/{code} - Get Commodity by Code")
    void testGetCommodityByCode() throws Exception {
        CommodityDTO responseDto = CommodityDTO.builder()
                .id(1L)
                .commodityCode("CMD001")
                .commodityName("Maize")
                .build();

        when(commodityService.getCommodityByCode("CMD001")).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/commodities/code/CMD001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commodityCode").value("CMD001"));
    }

    @Test
    @DisplayName("GET /api/v1/commodities - Get All Commodities paginated")
    void testGetAllCommodities() throws Exception {
        CommodityDTO responseDto = CommodityDTO.builder()
                .id(1L)
                .commodityCode("CMD001")
                .commodityName("Maize")
                .build();

        Page<CommodityDTO> page = new PageImpl<>(List.of(responseDto), PageRequest.of(0, 20), 1);
        when(commodityService.getAllCommodities(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/commodities?page=0&size=20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].commodityName").value("Maize"));
    }

    @Test
    @DisplayName("PUT /api/v1/commodities/{id} - Update Commodity")
    void testUpdateCommodity() throws Exception {
        AddCommodityRequest request = AddCommodityRequest.builder()
                .commodityName("Maize Premium")
                .category("Grains")
                .subCategory("Feed Grains")
                .defaultUnit("MT")
                .build();

        CommodityDTO responseDto = CommodityDTO.builder()
                .id(1L)
                .commodityCode("CMD001")
                .commodityName("Maize Premium")
                .build();

        when(commodityService.updateCommodity(eq(1L), any(AddCommodityRequest.class), eq("admin-001")))
                .thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/commodities/1")
                        .header("X-User-Id", "admin-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Commodity updated successfully"))
                .andExpect(jsonPath("$.data.commodityName").value("Maize Premium"));
    }

    @Test
    @DisplayName("POST /api/v1/commodities/{id}/quality-grades - Add Quality Grade")
    void testAddQualityGrade() throws Exception {
        AddQualityGradeRequest request = AddQualityGradeRequest.builder()
                .gradeName("Grade A")
                .gradeCode("GA")
                .description("Top Grade")
                .build();

        QualityGradeDTO gradeDTO = QualityGradeDTO.builder()
                .id(10L)
                .gradeName("Grade A")
                .gradeCode("GA")
                .description("Top Grade")
                .active(true)
                .build();

        when(commodityService.addQualityGrade(eq(1L), any(AddQualityGradeRequest.class)))
                .thenReturn(gradeDTO);

        mockMvc.perform(post("/api/v1/commodities/1/quality-grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Quality grade added successfully"))
                .andExpect(jsonPath("$.data.gradeCode").value("GA"));
    }

    @Test
    @DisplayName("PATCH /api/v1/commodities/{id}/deactivate - Deactivate Commodity")
    void testDeactivateCommodity() throws Exception {
        CommodityDTO responseDto = CommodityDTO.builder()
                .id(1L)
                .commodityCode("CMD001")
                .commodityName("Maize")
                .status(Commodity.CommodityStatus.INACTIVE)
                .build();

        when(commodityService.deactivateCommodity(1L, "admin-001")).thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/commodities/1/deactivate")
                        .header("X-User-Id", "admin-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Commodity deactivated successfully"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("GET /api/v1/commodities/{id} - Not Found Handling")
    void testGetCommodityNotFound() throws Exception {
        when(commodityService.getCommodityById(999L))
                .thenThrow(new ResourceNotFoundException("Commodity not found with id: 999"));

        mockMvc.perform(get("/api/v1/commodities/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }
}
