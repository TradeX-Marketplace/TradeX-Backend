package com.kalibyte.tradex.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalibyte.tradex.trading.controller.*;
import com.kalibyte.tradex.trading.dto.*;
import com.kalibyte.tradex.trading.dto.request.*;
import com.kalibyte.tradex.trading.entity.Contract;
import com.kalibyte.tradex.trading.entity.Deal;
import com.kalibyte.tradex.trading.repository.ContractRepository;
import com.kalibyte.tradex.trading.service.InventoryLockEngine;
import com.kalibyte.tradex.trading.service.NegotiationEngine;
import com.kalibyte.tradex.trading.service.TradingService;
import com.kalibyte.tradex.trading.service.DoubleBlindMasker;
import com.kalibyte.tradex.trading.stream.MarketStreamService;
import com.kalibyte.tradex.common.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TradingControllersTest {

    private ObjectMapper objectMapper;

    @Mock
    private TradingService tradingService;

    @Mock
    private InventoryLockEngine inventoryLockEngine;

    @Mock
    private NegotiationEngine negotiationEngine;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private com.kalibyte.tradex.notification.service.WhatsAppService whatsAppService;

    private DoubleBlindMasker masker = new DoubleBlindMasker();

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private MarketStreamService marketStreamService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private MockMvc supplyMockMvc;
    private MockMvc demandMockMvc;
    private MockMvc biddingMockMvc;
    private MockMvc dealMockMvc;
    private MockMvc contractMockMvc;
    private MockMvc negotiationMockMvc;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();

        org.mockito.Mockito.lenient().when(securityUtils.resolvePartyId(any(), any())).thenReturn(1L);
        org.mockito.Mockito.lenient().when(securityUtils.resolvePartyRole(any(), any())).thenReturn("SELLER");
        org.mockito.Mockito.lenient().when(securityUtils.resolveAdminName(any(), any())).thenReturn("Admin User");
        org.mockito.Mockito.lenient().when(securityUtils.isAdmin(any())).thenReturn(true);

        SupplyController supplyController = new SupplyController(tradingService, masker, securityUtils);
        supplyMockMvc = MockMvcBuilders.standaloneSetup(supplyController).build();

        DemandController demandController = new DemandController(tradingService, masker, securityUtils);
        demandMockMvc = MockMvcBuilders.standaloneSetup(demandController).build();

        BiddingController biddingController = new BiddingController(tradingService, securityUtils, masker);
        biddingMockMvc = MockMvcBuilders.standaloneSetup(biddingController).build();

        DealController dealController = new DealController(tradingService, inventoryLockEngine, securityUtils);
        dealMockMvc = MockMvcBuilders.standaloneSetup(dealController).build();

        ContractController contractController = new ContractController(contractRepository, whatsAppService, securityUtils, eventPublisher);
        contractMockMvc = MockMvcBuilders.standaloneSetup(contractController).build();

        NegotiationController negotiationController = new NegotiationController(negotiationEngine, securityUtils, marketStreamService);
        negotiationMockMvc = MockMvcBuilders.standaloneSetup(negotiationController).build();
    }

    @Test
    @DisplayName("POST /api/v1/supplies - Create Supply")
    void testCreateSupply() throws Exception {
        PostSupplyRequest request = PostSupplyRequest.builder()
                .commodityId(1L)
                .totalQuantity(new BigDecimal("100.00"))
                .askingRate(new BigDecimal("2500.00"))
                .location("Indore Warehouse")
                .originZone("Central")
                .validTill(LocalDate.now().plusDays(10))
                .build();

        SupplyDTO dto = SupplyDTO.builder()
                .id(1L)
                .supplyNumber("SUP-001")
                .commodityName("Soybean")
                .totalQuantity(new BigDecimal("100.00"))
                .askingRate(new BigDecimal("2500.00"))
                .status("ACTIVE")
                .build();

        when(tradingService.createSupply(any(PostSupplyRequest.class), anyLong())).thenReturn(dto);

        supplyMockMvc.perform(post("/api/v1/supplies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.supplyNumber").value("SUP-001"));
    }

    @Test
    @DisplayName("GET /api/v1/supplies - Search Available Supplies")
    void testGetSupplies() throws Exception {
        SupplyDTO dto = SupplyDTO.builder()
                .id(1L)
                .supplyNumber("SUP-001")
                .commodityName("Soybean")
                .totalQuantity(new BigDecimal("100.00"))
                .askingRate(new BigDecimal("2500.00"))
                .status("ACTIVE")
                .build();

        when(tradingService.getAvailableSupplies(1L)).thenReturn(List.of(dto));

        supplyMockMvc.perform(get("/api/v1/supplies")
                        .param("commodityId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].supplyNumber").value("SUP-001"));
    }

    @Test
    @DisplayName("POST /api/v1/demands - Create Demand")
    void testCreateDemand() throws Exception {
        PostDemandRequest request = PostDemandRequest.builder()
                .commodityId(1L)
                .quantity(new BigDecimal("50.00"))
                .targetRate(new BigDecimal("2400.00"))
                .destination("Mumbai Port")
                .destinationZone("West")
                .validTill(LocalDate.now().plusDays(15))
                .build();

        DemandDTO dto = DemandDTO.builder()
                .id(1L)
                .demandNumber("DEM-001")
                .commodityName("Soybean")
                .quantity(new BigDecimal("50.00"))
                .targetRate(new BigDecimal("2400.00"))
                .status("ACTIVE")
                .build();

        when(tradingService.createDemand(any(PostDemandRequest.class), anyLong())).thenReturn(dto);

        demandMockMvc.perform(post("/api/v1/demands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.demandNumber").value("DEM-001"));
    }

    @Test
    @DisplayName("POST /api/v1/bids - Place Bid")
    void testPlaceBid() throws Exception {
        PlaceBidRequest request = PlaceBidRequest.builder()
                .commodityId(1L)
                .supplyId(1L)
                .bidRate(new BigDecimal("2450.00"))
                .quantity(new BigDecimal("50.00"))
                .destination("Mumbai Port")
                .remarks("Fast delivery requested")
                .build();

        BidDTO dto = BidDTO.builder()
                .id(1L)
                .bidNumber("BID-001")
                .commodityName("Soybean")
                .bidRate(new BigDecimal("2450.00"))
                .quantity(new BigDecimal("50.00"))
                .status("PENDING")
                .build();

        when(tradingService.placeBid(any(PlaceBidRequest.class), anyLong())).thenReturn(dto);

        biddingMockMvc.perform(post("/api/v1/bids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bidNumber").value("BID-001"));
    }

    @Test
    @DisplayName("POST /api/v1/deals/{id}/lock - Lock Deal")
    void testLockDeal() throws Exception {
        LockDealRequest request = new LockDealRequest();
        request.setNotes("Pessimistic lock test");

        DealDTO dto = DealDTO.builder()
                .id(1L)
                .dealNumber("DEA-001")
                .commodityName("Soybean")
                .quantity(new BigDecimal("50.00"))
                .finalRate(new BigDecimal("2450.00"))
                .lockStatus("LOCKED")
                .status("LOCKED")
                .build();

        when(inventoryLockEngine.lockDealAndIssueContract(eq(1L), anyString())).thenReturn(dto);

        dealMockMvc.perform(post("/api/v1/deals/1/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lockStatus").value("LOCKED"));
    }

    @Test
    @DisplayName("GET /api/v1/contracts/{id} - Get Contract")
    void testGetContract() throws Exception {
        Deal deal = Deal.builder().id(1L).dealNumber("DEA-001").build();
        Contract contract = Contract.builder()
                .id(1L)
                .deal(deal)
                .contractNumber("CTR-001")
                .sellerName("Seller Corp")
                .sellerGstin("27AAAAA0000A1Z5")
                .buyerName("Buyer Corp")
                .buyerGstin("27BBBBB0000B1Z5")
                .commodityName("Soybean")
                .quantity(new BigDecimal("50.00"))
                .rate(new BigDecimal("2450.00"))
                .totalAmount(new BigDecimal("122500.00"))
                .status("ISSUED")
                .issuedAt(LocalDateTime.now())
                .createdByPartyId(1L)
                .build();

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        contractMockMvc.perform(get("/api/v1/contracts/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contractNumber").value("CTR-001"));
    }

    @Test
    @DisplayName("POST /api/v1/negotiations/{id}/offer - Submit Offer")
    void testSubmitOffer() throws Exception {
        SubmitNegotiationOfferRequest request = new SubmitNegotiationOfferRequest();
        request.setRate(new BigDecimal("2460.00"));
        request.setRemarks("Counter offer round 2");

        NegotiationDTO dto = NegotiationDTO.builder()
                .id(1L)
                .negotiationCode("NEG-001")
                .commodityName("Soybean")
                .currentOffer(new BigDecimal("2460.00"))
                .status("ACTIVE")
                .build();

        when(negotiationEngine.submitOffer(eq(1L), any(SubmitNegotiationOfferRequest.class), anyString())).thenReturn(dto);

        negotiationMockMvc.perform(post("/api/v1/negotiations/1/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.negotiationCode").value("NEG-001"));
    }
}
