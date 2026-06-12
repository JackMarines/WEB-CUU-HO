package com.emergency.service;

import com.emergency.dto.ChatMessage;
import com.emergency.dto.response.ChatResponse;
import com.emergency.model.DistressCall;
import com.emergency.model.DistressCall.CallStatus;
import com.emergency.model.DisasterType;
import com.emergency.model.RescueCenter;
import com.emergency.model.RescueCenter.CenterType;
import com.emergency.model.User;
import com.emergency.repository.DistressCallRepository;
import com.emergency.repository.DisasterTypeRepository;
import com.emergency.repository.RescueCenterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock private RestTemplate restTemplate;
    @Mock private DistressCallRepository callRepository;
    @Mock private DisasterTypeRepository disasterTypeRepository;
    @Mock private RescueCenterRepository centerRepository;

    @Captor private ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor;

    private static final String API_KEY = "test-key";
    private static final String MODEL = "openrouter/free";
    private AiService aiService;
    private AiService fallbackAiService;
    private DisasterType floodType;
    private DistressCall testCall;
    private RescueCenter testCenter;
    private List<ChatMessage> emptyHistory;

    @BeforeEach
    void setUp() {
        var objectMapper = new ObjectMapper();
        aiService = new AiService(restTemplate, API_KEY, MODEL,
            callRepository, disasterTypeRepository, centerRepository, objectMapper);
        fallbackAiService = new AiService(restTemplate, "", MODEL,
            callRepository, disasterTypeRepository, centerRepository, objectMapper);

        emptyHistory = List.of();

        var user = new User();
        user.setId(1L);
        user.setName("Nguyễn Văn A");

        floodType = new DisasterType();
        floodType.setId(1L);
        floodType.setName("Lũ lụt");
        floodType.setSlug("flood");
        floodType.setBaseUrgencyScore(80);

        testCall = new DistressCall();
        testCall.setId(5L);
        testCall.setUser(user);
        testCall.setDisasterType(floodType);
        testCall.setLat(new BigDecimal("16.0"));
        testCall.setLng(new BigDecimal("108.0"));
        testCall.setLocationName("Quận 1, HCM");
        testCall.setDescription("Ngập lụt nghiêm trọng sau bão");
        testCall.setCallerName("Nguyễn Văn A");
        testCall.setStatus(CallStatus.active);
        testCall.setUrgencyScore(80);
        testCall.setCreatedAt(LocalDateTime.now().minusHours(1));

        testCenter = new RescueCenter();
        testCenter.setId(1L);
        testCenter.setName("Điểm cứu trợ Quận 1");
        testCenter.setType(CenterType.supply_distribution);
        testCenter.setAddress("123 Nguyễn Huệ, Quận 1");
    }

    @Test
    void openrouterRespond_SendsCorrectRequest() {
        Map<String, Object> mockResponse = Map.of(
            "choices", List.of(Map.of(
                "message", Map.of("content", "Xin chào, tôi có thể giúp gì?")
            ))
        );
        when(restTemplate.exchange(
            contains("openrouter.ai"),
            eq(HttpMethod.POST),
            any(),
            eq(Map.class)
        )).thenReturn(ResponseEntity.ok(mockResponse));

        List<ChatMessage> history = List.of(
            new ChatMessage("user", "hello"),
            new ChatMessage("assistant", "hi there")
        );
        ChatResponse response = aiService.respond("cần vật tư gì?", null, history);

        verify(restTemplate).exchange(
            contains("openrouter.ai"),
            eq(HttpMethod.POST),
            requestCaptor.capture(),
            eq(Map.class)
        );

        HttpEntity<Map<String, Object>> request = requestCaptor.getValue();
        Map<String, Object> body = request.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKey("model");
        assertThat(body).containsKey("messages");
        assertThat(body).containsKey("temperature");

        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        assertThat(messages).hasSize(4);
        assertThat(messages.get(0).get("role")).isEqualTo("system");
        assertThat(messages.get(1).get("role")).isEqualTo("user");
        assertThat(messages.get(2).get("role")).isEqualTo("assistant");
        assertThat(messages.get(3).get("role")).isEqualTo("user");

        assertThat(response.reply()).contains("Xin chào");
    }

    @Test
    void openrouterRespond_EmptyHistory_SendsSystemAndUser() {
        Map<String, Object> mockResponse = Map.of(
            "choices", List.of(Map.of(
                "message", Map.of("content", "câu trả lời")
            ))
        );
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
            .thenReturn(ResponseEntity.ok(mockResponse));

        ChatResponse response = aiService.respond("test", null, emptyHistory);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), requestCaptor.capture(), eq(Map.class));
        Map<String, Object> body = requestCaptor.getValue().getBody();
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).get("role")).isEqualTo("system");
        assertThat(messages.get(1).get("role")).isEqualTo("user");
    }

    @Test
    void geminiRespond_ApiError_FallsBackToRuleBased() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
            .thenThrow(new RestClientException("Connection refused"));

        ChatResponse response = aiService.respond("test", null, emptyHistory);

        assertThat(response.reply()).contains("Tôi không tìm thấy");
    }

    @Test
    void geminiRespond_Non200Response_ReturnsErrorMessage() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
            .thenReturn(ResponseEntity.status(400).body(null));

        ChatResponse response = aiService.respond("test", null, emptyHistory);

        assertThat(response.reply()).contains("Xin lỗi");
    }

    @Test
    void openrouterRespond_EmptyChoices_ReturnsErrorMessage() {
        Map<String, Object> mockResponse = Map.of("choices", List.of());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
            .thenReturn(ResponseEntity.ok(mockResponse));

        ChatResponse response = aiService.respond("test", null, emptyHistory);

        assertThat(response.reply()).contains("Xin lỗi");
    }

    @Test
    void openrouterRespond_NullHistory_ReturnsReply() {
        Map<String, Object> mockResponse = Map.of(
            "choices", List.of(Map.of(
                "message", Map.of("content", "ok")
            ))
        );
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
            .thenReturn(ResponseEntity.ok(mockResponse));

        ChatResponse response = aiService.respond("test", null, null);

        assertThat(response.reply()).isEqualTo("ok");
    }

    @Test
    void fallback_EmptyApiKey_SuppliesQuery_ReturnsSupplies() {
        ChatResponse response = fallbackAiService.respond("cần vật tư gì cho lũ lụt", null, emptyHistory);

        assertThat(response.reply()).contains("vật tư");
        assertThat(response.reply()).contains("Áo phao");
        assertThat(response.data()).isNotNull();
        assertThat(((Map<String, Object>) response.data()).get("type")).isEqualTo("supplies");
    }

    @Test
    void fallback_EmptyApiKey_SuppliesWithCallContext_UsesDisasterType() {
        when(callRepository.findById(5L)).thenReturn(Optional.of(testCall));

        ChatResponse response = fallbackAiService.respond("cần vật tư gì", 5, emptyHistory);

        assertThat(response.reply()).contains("vật tư");
        assertThat(response.suggestions()).isNotEmpty();
    }

    @Test
    void fallback_EmptyApiKey_CallQueryByNumber_ReturnsCallInfo() {
        when(callRepository.findById(5L)).thenReturn(Optional.of(testCall));

        ChatResponse response = fallbackAiService.respond("thông tin cuộc gọi #5", null, emptyHistory);

        assertThat(response.reply()).contains("#5");
        assertThat(response.reply()).contains("Nguyễn Văn A");
        assertThat(response.reply()).contains("Đang hoạt động");
        assertThat(response.data()).isNotNull();
        assertThat(((Map<String, Object>) response.data()).get("type")).isEqualTo("call");
        assertThat(((Map<String, Object>) response.data()).get("id")).isEqualTo(5L);
    }

    @Test
    void fallback_EmptyApiKey_CallQueryNotFound_ReturnsNotFound() {
        when(callRepository.findById(99L)).thenReturn(Optional.empty());

        ChatResponse response = fallbackAiService.respond("thông tin cuộc gọi #99", null, emptyHistory);

        assertThat(response.reply()).contains("Không tìm thấy");
    }

    @Test
    void fallback_EmptyApiKey_CallQueryWithoutNumber_UsesCallId() {
        when(callRepository.findById(5L)).thenReturn(Optional.of(testCall));

        ChatResponse response = fallbackAiService.respond("thông tin cuộc gọi", 5, emptyHistory);

        assertThat(response.reply()).contains("#5");
    }

    @Test
    void fallback_EmptyApiKey_CenterQuery_ReturnsCenters() {
        when(centerRepository.findAll()).thenReturn(List.of(testCenter));

        ChatResponse response = fallbackAiService.respond("trung tâm cứu hộ gần đây", null, emptyHistory);

        assertThat(response.reply()).contains("Điểm cứu trợ Quận 1");
        assertThat(response.data()).isNotNull();
        assertThat(((Map<String, Object>) response.data()).get("type")).isEqualTo("centers");
        assertThat(((Map<String, Object>) response.data()).get("centers")).asList().hasSize(1);
    }

    @Test
    void fallback_EmptyApiKey_CenterQueryEmpty_ReturnsNoCenters() {
        when(centerRepository.findAll()).thenReturn(List.of());

        ChatResponse response = fallbackAiService.respond("trung tâm cứu hộ", null, emptyHistory);

        assertThat(response.reply()).contains("không có");
    }

    @Test
    void fallback_EmptyApiKey_UnknownQuery_ReturnsFallback() {
        ChatResponse response = fallbackAiService.respond("hôm nay thế nào", null, emptyHistory);

        assertThat(response.reply()).contains("không tìm thấy");
        assertThat(response.suggestions()).hasSize(3);
    }

    @Test
    void fallback_EmptyApiKey_FloodQuery_ReturnsFloodSupplies() {
        ChatResponse response = fallbackAiService.respond("vật tư cho ngập lụt", null, emptyHistory);

        assertThat(response.reply()).contains("Áo phao");
        assertThat(response.reply()).contains("Phao cứu sinh");
    }

    @Test
    void fallback_EmptyApiKey_FireQuery_ReturnsFireSupplies() {
        ChatResponse response = fallbackAiService.respond("cần đồ cho hỏa hoạn", null, emptyHistory);

        assertThat(response.reply()).contains("Bình cứu hỏa");
        assertThat(response.reply()).contains("Mặt nạ phòng độc");
    }
}