package com.emergency.controller;

import com.emergency.dto.response.DistressCallResponse;
import com.emergency.dto.response.DisasterTypeResponse;
import com.emergency.security.JwtAuthenticationFilter;
import com.emergency.security.JwtTokenProvider;
import com.emergency.security.OAuth2SuccessHandler;
import com.emergency.service.CallService;
import com.emergency.service.ResponseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CallController.class)
@AutoConfigureMockMvc(addFilters = false)
class CallControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CallService callService;
    @MockBean private ResponseService responseService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private OAuth2SuccessHandler oAuth2SuccessHandler;

    private final DistressCallResponse sampleResponse = new DistressCallResponse(
        1L, null, null, "Nguyễn Văn A", "0901234567",
        new DisasterTypeResponse(1L, "Lũ lụt", "flood", "🌊", 80, "2026-01-01T00:00:00"),
        new BigDecimal("16.0"), new BigDecimal("108.0"),
        "Quận 1, HCM", "Ngập lụt sâu", "active", 80, 3,
        null, List.of("Áo phao", "Nước uống"),
        "2026-06-09T10:00:00", null
    );

    @BeforeEach
    void setUpAuth() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
            new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_user")))
        );
        SecurityContextHolder.setContext(context);
    }

    @Test
    void getAll_Returns200() throws Exception {
        when(callService.getAll(any(), any(), any(), any(), any()))
            .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/calls"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].callerName").value("Nguyễn Văn A"))
            .andExpect(jsonPath("$[0].status").value("active"));
    }

    @Test
    void getAll_WithQueryParams_ReturnsFiltered() throws Exception {
        when(callService.getAll(any(), any(), any(), any(), any()))
            .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/calls")
                .param("type", "flood")
                .param("status", "active")
                .param("q", "ngập"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getById_Returns200() throws Exception {
        when(callService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/calls/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_NotFound_Returns404() throws Exception {
        when(callService.getById(99L))
            .thenThrow(new com.emergency.exception.ResourceNotFoundException("Distress call not found"));

        mockMvc.perform(get("/api/calls/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void create_ValidBody_Returns200() throws Exception {
        when(callService.create(any(), any())).thenReturn(sampleResponse);

        String body = objectMapper.writeValueAsString(Map.of(
            "disasterTypeId", 1,
            "lat", 16.0,
            "lng", 108.0,
            "locationName", "Quận 1, HCM",
            "description", "Ngập lụt sâu",
            "callerName", "Nguyễn Văn A",
            "callerPhone", "0901234567"
        ));

        mockMvc.perform(post("/api/calls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_MissingRequiredFields_Returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "disasterTypeId", 1
        ));

        mockMvc.perform(post("/api/calls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void create_EmptyBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/calls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_Returns200() throws Exception {
        DistressCallResponse resolved = new DistressCallResponse(
            1L, null, null, "Nguyễn Văn A", null,
            new DisasterTypeResponse(1L, "Lũ lụt", "flood", "🌊", 80, "2026-01-01T00:00:00"),
            new BigDecimal("16.0"), new BigDecimal("108.0"),
            "Quận 1, HCM", "Ngập lụt sâu", "resolved", 80, 2,
            null, List.of(), "2026-06-09T10:00:00", "2026-06-09T12:00:00"
        );
        when(callService.updateStatus(any(), any())).thenReturn(resolved);

        mockMvc.perform(put("/api/calls/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"resolved\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("resolved"));
    }

    @Test
    void getStats_Returns200() throws Exception {
        Map<String, Object> stats = Map.of(
            "activeCalls", 5L,
            "inProgressCalls", 3L,
            "resolvedCalls", 10L,
            "totalCalls", 18L,
            "totalCenters", 8L,
            "callsByType", List.of(Map.of("type", "flood", "count", 12L)),
            "recentCalls", List.of()
        );
        when(callService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/calls/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCalls").value(18))
            .andExpect(jsonPath("$.totalCenters").value(8));
    }

}
