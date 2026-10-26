package com.kalibyte.tradex.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalibyte.tradex.auth.controller.AdminAuthController;
import com.kalibyte.tradex.auth.dto.request.AdminLoginRequest;
import com.kalibyte.tradex.auth.dto.response.AdminLoginResponse;
import com.kalibyte.tradex.auth.service.AdminAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AdminAuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AdminAuthService adminAuthService;

    @InjectMocks
    private AdminAuthController adminAuthController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(adminAuthController)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/admin/auth/login - Admin Login Success")
    void testAdminLoginSuccess() throws Exception {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setEmail("admin@123gmail.com");
        request.setPassword("Admin@123");

        AdminLoginResponse response = AdminLoginResponse.builder()
                .token("jwt-mock-token-xyz")
                .adminId(1L)
                .adminCode("ADMIN-0001")
                .name("Super Admin")
                .email("admin@123gmail.com")
                .role("SUPER_ADMIN")
                .build();

        when(adminAuthService.loginAdmin(any(AdminLoginRequest.class), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Admin login successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-mock-token-xyz"))
                .andExpect(jsonPath("$.data.email").value("admin@123gmail.com"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/auth/logout - Admin Logout Success")
    void testAdminLogoutSuccess() throws Exception {
        doNothing().when(adminAuthService).logoutAdmin(anyLong(), anyString());

        mockMvc.perform(post("/api/v1/admin/auth/logout")
                        .header("X-Admin-Id", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Admin logout successful"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/auth/change-password - Change Password Success")
    void testChangePasswordSuccess() throws Exception {
        doNothing().when(adminAuthService).changePassword(anyLong(), anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/v1/admin/auth/change-password")
                        .header("X-Admin-Id", 1L)
                        .param("oldPassword", "OldPass@123")
                        .param("newPassword", "NewPass@123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password updated"));
    }
}
