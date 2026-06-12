package com.emergency.service;

import com.emergency.dto.ChatMessage;
import com.emergency.dto.response.ChatResponse;
import com.emergency.model.DistressCall;
import com.emergency.model.RescueCenter;
import com.emergency.repository.DistressCallRepository;
import com.emergency.repository.DisasterTypeRepository;
import com.emergency.repository.RescueCenterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final String SYSTEM_PROMPT = "Bạn là trợ lý AI chuyên nghiệp cho hệ thống cứu hộ khẩn cấp."
        + " Trả lời bằng tiếng Việt."
        + " Bạn hỗ trợ thông tin về vật tư cứu trợ, hướng dẫn sơ cứu, trung tâm cứu hộ, cuộc gọi khẩn cấp, và các câu hỏi liên quan."
        + " Khi người dùng hỏi về dữ liệu trong hệ thống (trung tâm, cuộc gọi, loại thiên tai), hãy dùng các công cụ có sẵn để lấy thông tin thực tế từ cơ sở dữ liệu."
        + " Trả lời ngắn gọn, chính xác, hữu ích.";

    private static final String PUBLIC_SYSTEM_PROMPT = SYSTEM_PROMPT
        + " Bạn KHÔNG được phép trả lời các câu hỏi về thông tin cuộc gọi khẩn cấp (distress calls)."
        + " Nếu người dùng hỏi về cuộc gọi, hãy yêu cầu họ đăng nhập.";

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
    private final ObjectMapper objectMapper;

    public AiService(RestTemplate restTemplate,
                     @Value("${app.openrouter.api-key:}") String apiKey,
                     @Value("${app.openrouter.model:deepseek/deepseek-chat:free}") String model,
                     DistressCallRepository callRepository,
                     DisasterTypeRepository disasterTypeRepository,
                     RescueCenterRepository centerRepository,
                     ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.model = model;
        this.callRepository = callRepository;
        this.disasterTypeRepository = disasterTypeRepository;
        this.centerRepository = centerRepository;
        this.objectMapper = objectMapper;
    }

    public ChatResponse respond(String message, Integer callId, List<ChatMessage> history) {
        if (apiKey == null || apiKey.isBlank()) {
            return ruleBasedRespond(message, callId);
        }
        try {
            return openrouterRespond(message, history);
        } catch (RestClientException e) {
            log.warn("OpenRouter connection failed ({}), falling back to rule-based", e.getMessage());
            return ruleBasedRespond(message, callId);
        }
    }

    public ChatResponse respondPublic(String message, List<ChatMessage> history) {
        if (apiKey == null || apiKey.isBlank()) {
            return ruleBasedRespondPublic(message);
        }
        try {
            return openrouterRespondPublic(message, history);
        } catch (RestClientException e) {
            log.warn("OpenRouter connection failed ({}), falling back to rule-based", e.getMessage());
            return ruleBasedRespondPublic(message);
        }
    }

    private ChatResponse openrouterRespond(String message, List<ChatMessage> history) {
        try {
            HttpHeaders headers = buildHeaders();
            Map<String, Object> requestBody = buildOpenRouterRequest(message, history);

            ResponseEntity<Map> response = restTemplate.exchange(
                OPENROUTER_URL, HttpMethod.POST, new HttpEntity<>(requestBody, headers), Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return errorResponse("Xin lỗi, đã xảy ra lỗi khi kết nối đến dịch vụ AI.");
            }

            Map<String, Object> choice = getFirstChoice(response.getBody());
            if (choice == null) {
                return errorResponse("Xin lỗi, tôi không thể tạo câu trả lời lúc này.");
            }

            String finishReason = (String) choice.get("finish_reason");

            if ("tool_calls".equals(finishReason)) {
                return handleToolCalls(choice, message, history, headers);
            }

            String reply = extractOpenRouterText(response.getBody());
            if (reply == null || reply.isBlank()) {
                return errorResponse("Xin lỗi, tôi không thể tạo câu trả lời lúc này.");
            }
            return new ChatResponse(reply, List.of(), null);

        } catch (RestClientException e) {
            log.error("OpenRouter API call failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("OpenRouter unexpected error", e);
            return errorResponse("Xin lỗi, đã xảy ra lỗi xử lý.");
        }
    }

    private ChatResponse openrouterRespondPublic(String message, List<ChatMessage> history) {
        try {
            HttpHeaders headers = buildHeaders();
            Map<String, Object> requestBody = buildOpenRouterRequest(message, history,
                PUBLIC_SYSTEM_PROMPT, getPublicToolDefinitions());

            ResponseEntity<Map> response = restTemplate.exchange(
                OPENROUTER_URL, HttpMethod.POST, new HttpEntity<>(requestBody, headers), Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return errorResponse("Xin lỗi, đã xảy ra lỗi khi kết nối đến dịch vụ AI.");
            }

            Map<String, Object> choice = getFirstChoice(response.getBody());
            if (choice == null) {
                return errorResponse("Xin lỗi, tôi không thể tạo câu trả lời lúc này.");
            }

            String finishReason = (String) choice.get("finish_reason");

            if ("tool_calls".equals(finishReason)) {
                return handleToolCalls(choice, message, history, headers, PUBLIC_SYSTEM_PROMPT);
            }

            String reply = extractOpenRouterText(response.getBody());
            if (reply == null || reply.isBlank()) {
                return errorResponse("Xin lỗi, tôi không thể tạo câu trả lời lúc này.");
            }
            return new ChatResponse(reply, List.of(), null);

        } catch (RestClientException e) {
            log.error("OpenRouter API call failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("OpenRouter unexpected error", e);
            return errorResponse("Xin lỗi, đã xảy ra lỗi xử lý.");
        }
    }

    private ChatResponse handleToolCalls(Map<String, Object> choice, String originalMessage,
                                           List<ChatMessage> history, HttpHeaders headers) {
        return handleToolCalls(choice, originalMessage, history, headers, SYSTEM_PROMPT);
    }

    @SuppressWarnings("unchecked")
    private ChatResponse handleToolCalls(Map<String, Object> choice, String originalMessage,
                                           List<ChatMessage> history, HttpHeaders headers,
                                           String systemPrompt) {
        try {
            Map<String, Object> assistantMsg = (Map<String, Object>) choice.get("message");
            List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) assistantMsg.get("tool_calls");
            if (toolCalls == null || toolCalls.isEmpty()) {
                Object content = assistantMsg.get("content");
                String text = content instanceof String ? (String) content : null;
                if (text != null && !text.isBlank()) {
                    return new ChatResponse(text, List.of(), null);
                }
                return errorResponse("Xin lỗi, tôi không thể xử lý yêu cầu này.");
            }

            List<Map<String, Object>> toolMessages = new ArrayList<>();
            for (Map<String, Object> tc : toolCalls) {
                String toolCallId = (String) tc.get("id");
                Map<String, Object> function = (Map<String, Object>) tc.get("function");
                String toolName = (String) function.get("name");
                String argsStr = (String) function.get("arguments");

                Map<String, Object> args = objectMapper.readValue(argsStr, Map.class);
                Object result = executeTool(toolName, args);
                String content = objectMapper.writeValueAsString(result);

                Map<String, Object> toolMsg = new HashMap<>();
                toolMsg.put("role", "tool");
                toolMsg.put("tool_call_id", toolCallId);
                toolMsg.put("content", content);
                toolMessages.add(toolMsg);
            }

            List<Map<String, Object>> allMessages = new ArrayList<>();
            allMessages.add(Map.of("role", "system", "content", systemPrompt));
            if (history != null) {
                for (ChatMessage msg : history) {
                    allMessages.add(Map.of("role",
                        "user".equals(msg.role()) ? "user" : "assistant",
                        "content", msg.content()));
                }
            }
            allMessages.add(Map.of("role", "user", "content", originalMessage));
            allMessages.add(assistantMsg);
            allMessages.addAll(toolMessages);

            Map<String, Object> followUpBody = new HashMap<>();
            followUpBody.put("model", model);
            followUpBody.put("messages", allMessages);
            followUpBody.put("temperature", 0.7);
            followUpBody.put("max_tokens", 800);

            ResponseEntity<Map> followUpResponse = restTemplate.exchange(
                OPENROUTER_URL, HttpMethod.POST, new HttpEntity<>(followUpBody, headers), Map.class);

            String reply = extractOpenRouterText(followUpResponse.getBody());
            if (reply == null || reply.isBlank()) {
                return errorResponse("Xin lỗi, tôi không thể xử lý yêu cầu này.");
            }
            return new ChatResponse(reply, List.of(), null);

        } catch (Exception e) {
            log.error("Failed to handle tool calls", e);
            return errorResponse("Xin lỗi, đã xảy ra lỗi khi xử lý dữ liệu.");
        }
    }

    private Object executeTool(String toolName, Map<String, Object> args) {
        return switch (toolName) {
            case "get_centers" -> executeGetCenters(args);
            case "get_call_by_id" -> executeGetCallById(args);
            case "get_calls" -> executeGetCalls(args);
            case "get_disaster_types" -> executeGetDisasterTypes();
            case "get_supplies_for_disaster" -> executeGetSupplies(args);
            default -> Map.of("error", "Unknown tool: " + toolName);
        };
    }

    private List<Map<String, Object>> executeGetCenters(Map<String, Object> args) {
        String type = (String) args.get("type");
        List<RescueCenter> centers;

        if (type != null && !type.isBlank()) {
            try {
                RescueCenter.CenterType centerType = RescueCenter.CenterType.valueOf(type);
                centers = centerRepository.findByType(centerType);
            } catch (IllegalArgumentException e) {
                centers = centerRepository.findAllByOrderByNameAsc();
            }
        } else {
            centers = centerRepository.findAllByOrderByNameAsc();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (RescueCenter c : centers) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("type", c.getType().name());
            m.put("address", c.getAddress());
            m.put("phone", c.getPhone());
            m.put("lat", c.getLat());
            m.put("lng", c.getLng());
            result.add(m);
        }
        return result;
    }

    private Map<String, Object> executeGetCallById(Map<String, Object> args) {
        Object idObj = args.get("id");
        if (idObj == null) return Map.of("error", "Missing call id");
        Long id;
        if (idObj instanceof Number n) {
            id = n.longValue();
        } else {
            try {
                id = Long.parseLong(idObj.toString());
            } catch (NumberFormatException e) {
                return Map.of("error", "Invalid call id: " + idObj);
            }
        }

        return callRepository.findById(id)
            .map(call -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", call.getId());
                m.put("description", call.getDescription());
                m.put("status", call.getStatus().name());
                m.put("urgencyScore", call.getUrgencyScore());
                m.put("locationName", call.getLocationName());
                m.put("lat", call.getLat());
                m.put("lng", call.getLng());
                m.put("disasterType", call.getDisasterType().getName());
                if (call.getUser() != null) {
                    m.put("callerName", call.getUser().getName());
                    m.put("callerPhone", call.getUser().getPhone());
                } else {
                    m.put("callerName", call.getCallerName());
                    m.put("callerPhone", call.getCallerPhone());
                }
                m.put("createdAt", call.getCreatedAt() != null ? call.getCreatedAt().toString() : null);
                return m;
            })
            .orElse(Map.of("error", "Không tìm thấy cuộc gọi #" + id));
    }

    private List<Map<String, Object>> executeGetCalls(Map<String, Object> args) {
        String status = (String) args.get("status");
        int limit = args.containsKey("limit") ? ((Number) args.get("limit")).intValue() : 10;

        List<DistressCall> calls;
        if (status != null && !status.isBlank()) {
            try {
                DistressCall.CallStatus callStatus = DistressCall.CallStatus.valueOf(status);
                calls = callRepository.findByFilters(null, callStatus, null, null, null);
            } catch (IllegalArgumentException e) {
                calls = callRepository.findTop10ByOrderByCreatedAtDesc();
            }
        } else {
            calls = callRepository.findTop10ByOrderByCreatedAtDesc();
        }

        return calls.stream()
            .limit(limit)
            .map(call -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", call.getId());
                String desc = call.getDescription();
                m.put("description", desc != null && desc.length() > 100 ? desc.substring(0, 100) + "..." : desc);
                m.put("status", call.getStatus().name());
                m.put("urgencyScore", call.getUrgencyScore());
                m.put("disasterType", call.getDisasterType().getName());
                m.put("createdAt", call.getCreatedAt() != null ? call.getCreatedAt().toString() : null);
                return m;
            })
            .toList();
    }

    private List<Map<String, Object>> executeGetDisasterTypes() {
        return disasterTypeRepository.findAllByOrderByNameAsc().stream()
            .map(dt -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", dt.getId());
                m.put("name", dt.getName());
                m.put("slug", dt.getSlug());
                return m;
            })
            .toList();
    }

    private Map<String, Object> executeGetSupplies(Map<String, Object> args) {
        String disasterType = (String) args.get("type");
        if (disasterType == null || disasterType.isBlank()) {
            return Map.of("items", List.of("Nước uống", "Thực phẩm", "Thuốc men", "Đèn pin", "Chăn màn"));
        }

        String lower = disasterType.toLowerCase();
        for (var entry : SUPPLIES_MAP.entrySet()) {
            if (lower.contains(entry.getKey()) || entry.getKey().contains(lower)) {
                Map<String, Object> result = new HashMap<>();
                result.put("type", entry.getKey());
                result.put("items", entry.getValue());
                return result;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("type", disasterType);
        result.put("items", List.of("Nước uống", "Thực phẩm", "Thuốc men", "Đèn pin", "Chăn màn"));
        return result;
    }

    private Map<String, Object> buildOpenRouterRequest(String message, List<ChatMessage> history) {
        return buildOpenRouterRequest(message, history, SYSTEM_PROMPT, getToolDefinitions());
    }

    private Map<String, Object> buildOpenRouterRequest(String message, List<ChatMessage> history,
                                                        String systemPrompt, List<Map<String, Object>> tools) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        if (history != null) {
            for (ChatMessage msg : history) {
                String role = "user".equals(msg.role()) ? "user" : "assistant";
                messages.add(Map.of("role", role, "content", msg.content()));
            }
        }

        messages.add(Map.of("role", "user", "content", message));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.7);
        body.put("max_tokens", 800);
        body.put("tools", tools);
        body.put("tool_choice", "auto");
        return body;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "http://localhost:3000");
        headers.set("X-Title", "Emergency Response");
        return headers;
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> getFirstChoice(Map<String, Object> body) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            if (choices == null || choices.isEmpty()) return null;
            return choices.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Map<String, Object>> getToolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();

        // get_centers
        tools.add(tool("get_centers",
            "Lấy danh sách trung tâm cứu hộ. Có thể lọc theo loại trung tâm.",
            Map.of(
                "type", Map.of(
                    "type", "string",
                    "enum", List.of("shelter", "supply_distribution", "rescue_team"),
                    "description", "Loại trung tâm (shelter: nơi tạm trú, supply_distribution: điểm phát cứu trợ, rescue_team: đội cứu hộ)"
                )
            ),
            List.of()
        ));

        // get_call_by_id
        tools.add(tool("get_call_by_id",
            "Lấy thông tin chi tiết của một cuộc gọi khẩn cấp theo ID.",
            Map.of(
                "id", Map.of(
                    "type", "number",
                    "description", "ID của cuộc gọi khẩn cấp"
                )
            ),
            List.of("id")
        ));

        // get_calls
        tools.add(tool("get_calls",
            "Lấy danh sách các cuộc gọi khẩn cấp. Có thể lọc theo trạng thái và giới hạn số lượng.",
            Map.of(
                "status", Map.of(
                    "type", "string",
                    "enum", List.of("active", "in_progress", "resolved"),
                    "description", "Lọc theo trạng thái (active: đang hoạt động, in_progress: đang xử lý, resolved: đã giải quyết)"
                ),
                "limit", Map.of(
                    "type", "number",
                    "description", "Số lượng cuộc gọi tối đa trả về (mặc định 10)"
                )
            ),
            List.of()
        ));

        // get_disaster_types
        tools.add(tool("get_disaster_types",
            "Lấy danh sách tất cả loại thiên tai trong hệ thống.",
            Map.of(),
            List.of()
        ));

        // get_supplies_for_disaster
        tools.add(tool("get_supplies_for_disaster",
            "Lấy danh sách vật tư cần thiết cho một loại thiên tai cụ thể.",
            Map.of(
                "type", Map.of(
                    "type", "string",
                    "description", "Loại thiên tai (ví dụ: lũ, lụt, hỏa hoạn, cháy, bão, sạt lở, động đất)"
                )
            ),
            List.of("type")
        ));

        return tools;
    }

    private List<Map<String, Object>> getPublicToolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();

        // get_centers
        tools.add(tool("get_centers",
            "Lấy danh sách trung tâm cứu hộ. Có thể lọc theo loại trung tâm.",
            Map.of(
                "type", Map.of(
                    "type", "string",
                    "enum", List.of("shelter", "supply_distribution", "rescue_team"),
                    "description", "Loại trung tâm (shelter: nơi tạm trú, supply_distribution: điểm phát cứu trợ, rescue_team: đội cứu hộ)"
                )
            ),
            List.of()
        ));

        // get_disaster_types
        tools.add(tool("get_disaster_types",
            "Lấy danh sách tất cả loại thiên tai trong hệ thống.",
            Map.of(),
            List.of()
        ));

        // get_supplies_for_disaster
        tools.add(tool("get_supplies_for_disaster",
            "Lấy danh sách vật tư cần thiết cho một loại thiên tai cụ thể.",
            Map.of(
                "type", Map.of(
                    "type", "string",
                    "description", "Loại thiên tai (ví dụ: lũ, lụt, hỏa hoạn, cháy, bão, sạt lở, động đất)"
                )
            ),
            List.of("type")
        ));

        return tools;
    }

    private Map<String, Object> tool(String name, String description,
                                      Map<String, Object> properties, List<String> required) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        params.put("properties", properties);
        if (!required.isEmpty()) {
            params.put("required", required);
        }
        params.put("additionalProperties", false);

        Map<String, Object> function = new HashMap<>();
        function.put("name", name);
        function.put("description", description);
        function.put("parameters", params);

        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "function");
        tool.put("function", function);
        return tool;
    }

    private ChatResponse errorResponse(String message) {
        return new ChatResponse(message, List.of(), null);
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

    private ChatResponse ruleBasedRespondPublic(String message) {
        String lower = message.toLowerCase().trim();

        if (isCallQuery(lower)) {
            return new ChatResponse(
                "Vui lòng đăng nhập để xem thông tin chi tiết về cuộc gọi.",
                List.of("Vật tư cho lũ lụt", "Trung tâm cứu hộ", "Đăng nhập"),
                null
            );
        }
        if (isSuppliesQuery(lower)) {
            return handleSuppliesQuery(lower, null);
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
                    call.getUser() != null ? call.getUser().getName() : call.getCallerName()
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
