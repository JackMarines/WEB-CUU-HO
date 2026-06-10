package com.emergency.service;

import com.emergency.dto.ChatMessage;
import com.emergency.dto.response.ChatResponse;
import com.emergency.model.DistressCall;
import com.emergency.model.RescueCenter;
import com.emergency.repository.DistressCallRepository;
import com.emergency.repository.DisasterTypeRepository;
import com.emergency.repository.RescueCenterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String SYSTEM_PROMPT = "Bạn là trợ lý AI chuyên nghiệp cho hệ thống cứu hộ khẩn cấp. "
        + "Trả lời bằng tiếng Việt. "
        + "Bạn hỗ trợ thông tin về vật tư cứu trợ, hướng dẫn sơ cứu, trung tâm cứu hộ, và các câu hỏi liên quan đến tình huống khẩn cấp. "
        + "Trả lời ngắn gọn, chính xác, hữu ích. "
        + "Nếu người dùng hỏi về cuộc gọi khẩn cấp cụ thể, hãy yêu cầu họ cung cấp số cuộc gọi.";

    private static final Map<String, List<String>> SUPPLIES_MAP = new HashMap<>();

    static {
        SUPPLIES_MAP.put("lũ", List.of("Áo phao", "Thực phẩm khô", "Nước uống đóng chai", "Thuốc sát trùng", "Đèn pin"));
        SUPPLIES_MAP.put("lụt", List.of("Áo phao", "Thực phẩm khô", "Nước uống đóng chai", "Thuốc sát trùng", "Đèn pin"));
        SUPPLIES_MAP.put("ngập", List.of("Áo phao", "Thực phẩm khô", "Nước uống đóng chai", "Thuốc sát trùng", "Phao cứu sinh"));
        SUPPLIES_MAP.put("hỏa hoạn", List.of("Bình cứu hỏa", "Mặt nạ phòng độc", "Nước", "Băng gạc", "Thuốc bỏng"));
        SUPPLIES_MAP.put("cháy", List.of("Bình cứu hỏa", "Mặt nạ phòng độc", "Nước", "Băng gạc", "Thuốc bỏng"));
        SUPPLIES_MAP.put("bão", List.of("Thực phẩm", "Nước uống", "Đèn pin", "Pin dự phòng", "Thuốc men"));
        SUPPLIES_MAP.put("sạt lở", List.of("Xẻng", "Cuốc chim", "Thực phẩm", "Nước uống", "Băng ca"));
        SUPPLIES_MAP.put("lở đất", List.of("Xẻng", "Cuốc chim", "Thực phẩm", "Nước uống", "Băng ca"));
        SUPPLIES_MAP.put("động đất", List.of("Mũ bảo hiểm", "Nước uống", "Thực phẩm", "Bộ sơ cứu", "Đèn pin"));
    }

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;
    private final DistressCallRepository callRepository;
    private final DisasterTypeRepository disasterTypeRepository;
    private final RescueCenterRepository centerRepository;

    public AiService(RestTemplate restTemplate,
                     @Value("${app.openrouter.api-key:}") String apiKey,
                     @Value("${app.openrouter.model:deepseek/deepseek-chat:free}") String model,
                     DistressCallRepository callRepository,
                     DisasterTypeRepository disasterTypeRepository,
                     RescueCenterRepository centerRepository) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.model = model;
        this.callRepository = callRepository;
        this.disasterTypeRepository = disasterTypeRepository;
        this.centerRepository = centerRepository;
    }

    public ChatResponse respond(String message, Integer callId, List<ChatMessage> history) {
        if (apiKey == null || apiKey.isBlank()) {
            return ruleBasedRespond(message, callId);
        }
        return openrouterRespond(message, history);
    }

    private ChatResponse openrouterRespond(String message, List<ChatMessage> history) {
        try {
            Map<String, Object> requestBody = buildOpenRouterRequest(message, history);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("HTTP-Referer", "http://localhost:3000");
            headers.set("X-Title", "Emergency Response");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                OPENROUTER_URL, HttpMethod.POST, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return new ChatResponse("Xin lỗi, đã xảy ra lỗi khi kết nối đến dịch vụ AI. Vui lòng thử lại sau.", List.of(), null);
            }

            String reply = extractOpenRouterText(response.getBody());
            if (reply == null || reply.isBlank()) {
                return new ChatResponse("Xin lỗi, tôi không thể tạo câu trả lời lúc này. Vui lòng thử lại.", List.of(), null);
            }

            return new ChatResponse(reply, List.of(), null);

        } catch (RestClientException e) {
            log.error("OpenRouter API call failed: {}", e.getMessage());
            return new ChatResponse("Xin lỗi, không thể kết nối đến dịch vụ AI. Vui lòng kiểm tra kết nối mạng và thử lại.", List.of(), null);
        } catch (Exception e) {
            log.error("OpenRouter unexpected error", e);
            return new ChatResponse("Xin lỗi, đã xảy ra lỗi xử lý. Vui lòng thử lại.", List.of(), null);
        }
    }

    private Map<String, Object> buildOpenRouterRequest(String message, List<ChatMessage> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

        if (history != null) {
            for (ChatMessage msg : history) {
                String role = "user".equals(msg.role()) ? "user" : "assistant";
                messages.add(Map.of("role", role, "content", msg.content()));
            }
        }

        messages.add(Map.of("role", "user", "content", message));

        return Map.of(
            "model", model,
            "messages", messages,
            "temperature", 0.7,
            "max_tokens", 800
        );
    }

    @SuppressWarnings("unchecked")
    private String extractOpenRouterText(Map<String, Object> body) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            if (choices == null || choices.isEmpty()) return null;
            Map<String, Object> first = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) first.get("message");
            if (message == null) return null;
            Object text = message.get("content");
            return text instanceof String ? (String) text : null;
        } catch (Exception e) {
            return null;
        }
    }

    private ChatResponse ruleBasedRespond(String message, Integer callId) {
        String lower = message.toLowerCase().trim();

        if (isSuppliesQuery(lower)) {
            return handleSuppliesQuery(lower, callId);
        }
        if (isCallQuery(lower)) {
            return handleCallQuery(lower, callId);
        }
        if (isCenterQuery(lower)) {
            return handleCenterQuery();
        }
        return fallback();
    }

    private boolean isSuppliesQuery(String text) {
        return text.contains("vật tư") || text.contains("cứu trợ") || text.contains("nhu yếu phẩm")
            || text.contains("cần gì") || text.contains("supplies") || text.contains("đồ")
            || SUPPLIES_MAP.keySet().stream().anyMatch(text::contains);
    }

    private boolean isCallQuery(String text) {
        return text.contains("cuộc gọi") || text.contains("call") || text.contains("thông tin")
            || text.contains("trạng thái") || text.contains("tình trạng")
            || Pattern.compile("#?\\d+").matcher(text).find();
    }

    private boolean isCenterQuery(String text) {
        return text.contains("trung tâm") || text.contains("cứu hộ") || text.contains("nơi trú ẩn")
            || text.contains("shelter") || text.contains("gần") || text.contains("ở đâu");
    }

    private ChatResponse handleSuppliesQuery(String text, Integer callId) {
        String disasterTypeName = null;

        for (var entry : SUPPLIES_MAP.entrySet()) {
            if (text.contains(entry.getKey())) {
                disasterTypeName = entry.getKey();
                break;
            }
        }

        if (disasterTypeName == null && callId != null) {
            var callOpt = callRepository.findById(Long.valueOf(callId));
            if (callOpt.isPresent()) {
                disasterTypeName = callOpt.get().getDisasterType().getName().toLowerCase();
            }
        }

        if (disasterTypeName != null) {
            String finalName = disasterTypeName;
            var match = SUPPLIES_MAP.entrySet().stream()
                .filter(e -> finalName.contains(e.getKey()) || e.getKey().contains(finalName))
                .findFirst();

            if (match.isPresent()) {
                var entry = match.get();
                return suppliesResponse(entry.getKey(), entry.getValue());
            }
        }

        return suppliesResponse("thiên tai", List.of("Nước uống", "Thực phẩm", "Thuốc men", "Đèn pin", "Chăn màn"));
    }

    private ChatResponse suppliesResponse(String type, List<String> supplies) {
        String reply = "Các vật tư cần thiết cho " + type + ": " + String.join(", ", supplies) + ".";
        Map<String, Object> data = new HashMap<>();
        data.put("type", "supplies");
        data.put("items", supplies.stream().map(s -> Map.of("name", s)).toList());
        return new ChatResponse(reply, List.of("Vật tư cho hỏa hoạn", "Trung tâm cứu hộ gần đây"), data);
    }

    private ChatResponse handleCallQuery(String text, Integer callId) {
        Long id = null;
        Matcher matcher = Pattern.compile("#?(\\d+)").matcher(text);
        if (matcher.find()) {
            id = Long.parseLong(matcher.group(1));
        } else if (callId != null) {
            id = Long.valueOf(callId);
        }

        if (id != null) {
            var opt = callRepository.findById(id);
            if (opt.isPresent()) {
                DistressCall call = opt.get();
                String reply = String.format(
                    "Cuộc gọi #%d — %s\nTrạng thái: %s\nLoại: %s\nVị trí: %s\nMức độ khẩn cấp: %d/100\nNgười gọi: %s",
                    call.getId(),
                    call.getDescription().length() > 100 ? call.getDescription().substring(0, 100) + "..." : call.getDescription(),
                    switch (call.getStatus().name()) {
                        case "active" -> "Đang hoạt động";
                        case "in_progress" -> "Đang xử lý";
                        case "resolved" -> "Đã giải quyết";
                        default -> call.getStatus().name();
                    },
                    call.getDisasterType().getName(),
                    call.getLocationName() != null ? call.getLocationName() : call.getLat() + ", " + call.getLng(),
                    call.getUrgencyScore(),
                    call.getUser().getName()
                );

                Map<String, Object> data = new HashMap<>();
                data.put("type", "call");
                data.put("id", call.getId());
                data.put("status", call.getStatus().name());
                data.put("urgencyScore", call.getUrgencyScore());

                return new ChatResponse(reply, List.of("Vật tư cho " + call.getDisasterType().getName(), "Trung tâm gần đây"), data);
            }
        }

        return new ChatResponse(
            "Không tìm thấy cuộc gọi nào. Vui lòng cung cấp số cuộc gọi (ví dụ: 'call #5').",
            List.of("Xem cuộc gọi #1", "Trung tâm cứu hộ"),
            null
        );
    }

    private ChatResponse handleCenterQuery() {
        List<RescueCenter> centers = centerRepository.findAll();
        if (centers.isEmpty()) {
            return new ChatResponse(
                "Hiện không có trung tâm cứu hộ nào trong hệ thống.",
                List.of("Vật tư cho lũ lụt", "Thông tin cuộc gọi"),
                null
            );
        }

        var topCenters = centers.stream().limit(5).toList();
        StringBuilder sb = new StringBuilder("Các trung tâm cứu hộ:\n");
        for (int i = 0; i < topCenters.size(); i++) {
            RescueCenter c = topCenters.get(i);
            String typeLabel = switch (c.getType().name()) {
                case "shelter" -> "Nhà tạm trú";
                case "supply_distribution" -> "Điểm phát cứu trợ";
                case "rescue_team" -> "Đội cứu hộ";
                default -> c.getType().name();
            };
            sb.append(String.format("%d. %s (%s) — %s\n", i + 1, c.getName(), typeLabel, c.getAddress()));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("type", "centers");
        data.put("centers", topCenters.stream().map(c -> Map.of(
            "id", c.getId(),
            "name", c.getName(),
            "type", c.getType().name(),
            "address", c.getAddress()
        )).toList());

        return new ChatResponse(sb.toString(), List.of("Vật tư cho lũ lụt", "Thông tin cuộc gọi #1"), data);
    }

    private ChatResponse fallback() {
        return new ChatResponse(
            "Tôi không tìm thấy thông tin đó. Bạn có thể hỏi về vật tư cứu trợ, thông tin cuộc gọi, hoặc trung tâm cứu hộ.",
            List.of("Vật tư cho lũ lụt", "Thông tin cuộc gọi", "Trung tâm cứu hộ gần đây"),
            null
        );
    }
}