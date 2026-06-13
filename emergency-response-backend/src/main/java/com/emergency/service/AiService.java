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
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private static final String SYSTEM_PROMPT =
        "Bạn là trợ lý AI của hệ thống Cứu Trợ Khẩn Cấp — nền tảng điều phối cứu hộ thiên tai tại Việt Nam. " +
        "Trả lời bằng tiếng Việt, giọng điệu chuyên nghiệp, đồng cảm, chính xác, ngắn gọn.\n\n" +

        "=== KIẾN THỨC NỀN TẢNG ===\n" +
        "Hệ thống cho phép:\n" +
        "- Người dân gửi yêu cầu cứu trợ kèm vị trí, loại thiên tai, mô tả\n" +
        "- Xem bản đồ tương tác hiển thị các cuộc gọi và trung tâm cứu hộ\n" +
        "- Admin quản lý cuộc gọi, trung tâm, phân công cứu hộ\n" +
        "- Chatbot AI hỗ trợ thông tin\n\n" +

        "=== HƯỚNG DẪN CHO NGƯỜI DÙNG ===\n" +
        "Khi được hỏi cách gửi yêu cầu cứu trợ, hãy hướng dẫn:\n" +
        "1. Nhấn 'Gửi yêu cầu' trên trang chủ hoặc thanh bên\n" +
        "2. Chọn vị trí trên bản đồ (có thể dùng nút định vị GPS)\n" +
        "3. Chọn loại thiên tai\n" +
        "4. Mô tả tình hình, số người cần hỗ trợ\n" +
        "5. Nhấn gửi — hệ thống sẽ tính điểm khẩn cấp và thông báo cho admin\n\n" +

        "Khi được hỏi về kỹ năng ứng phó thiên tai:\n" +
        "- Lũ lụt: Di chuyển lên cao, tránh xa nước ngập, ngắt điện, chuẩn bị áo phao, thực phẩm khô, nước uống đóng chai, đèn pin, thuốc men. Liên hệ cứu hộ khẩn cấp.\n" +
        "- Hỏa hoạn: Gọi 114 ngay. Thoát hiểm bằng cầu thang bộ (không dùng thang máy). Dùng khăn ẩm che mũi miệng. Nếu lửa nhỏ, dùng bình cứu hỏa.\n" +
        "- Bão: Gia cố nhà cửa, dự trữ lương thực và nước ít nhất 3 ngày, tránh xa cửa sổ, theo dõi tin tức.\n" +
        "- Sạt lở đất: Di tản ngay nếu có dấu hiệu nguy hiểm. Không ở lại sau mưa lớn kéo dài.\n" +
        "- Động đất: Núp dưới bàn chắc chắn, tránh xa kính và vật treo. Ở trong nhà nếu an toàn, không chạy ra ngoài.\n\n" +

        "=== HƯỚNG DẪN CHO ADMIN ===\n" +
        "Khi admin hỏi về phân công cuộc gọi đến trung tâm:\n" +
        "- Dùng get_call_by_id để xem mức độ khẩn cấp và loại thiên tai\n" +
        "- Dùng get_centers tìm trung tâm phù hợp: shelter cho sơ tán, rescue_team cho cứu hộ, supply_distribution cho nhu yếu phẩm\n" +
        "- Dùng get_recommended_centers_for_call để nhận gợi ý tự động\n" +
        "- Lưu ý khoảng cách địa lý và công suất của trung tâm\n\n" +

        "Khi admin hỏi về vật tư cho trung tâm:\n" +
        "- Shelter (nơi tạm trú): giường, chăn màn, thực phẩm, nước uống, thuốc men, đồ vệ sinh\n" +
        "- Supply_distribution (điểm phát cứu trợ): lương thực, nước, thuốc, dụng cụ vệ sinh, quần áo\n" +
        "- Rescue_team (đội cứu hộ): dụng cụ cứu nạn, bộ sơ cứu, đèn pin, bình cứu hỏa, dây thừng\n\n" +

        "=== CÁCH SỬ DỤNG CÔNG CỤ ===\n" +
        "Khi người dùng hỏi về dữ liệu thực tế, hãy dùng công cụ phù hợp:\n" +
        "- get_centers: danh sách trung tâm cứu hộ (có thể lọc theo loại)\n" +
        "- get_centers_by_location: tìm trung tâm gần một địa điểm\n" +
        "- get_call_by_id: chi tiết một cuộc gọi (cần mã số)\n" +
        "- get_calls: danh sách cuộc gọi (lọc theo trạng thái)\n" +
        "- get_calls_by_disaster_type: lọc cuộc gọi theo loại thiên tai\n" +
        "- get_calls_by_location: tìm cuộc gọi tại khu vực\n" +
        "- get_disaster_types: các loại thiên tai trong hệ thống\n" +
        "- get_supplies_for_disaster: vật tư cho loại thiên tai\n" +
        "- get_emergency_guide: dùng cho câu hỏi an toàn (đi đâu, làm gì, cách ứng phó)\n" +
        "- get_supplies_for_disaster: CHỈ dùng cho vật tư, nhu yếu phẩm\n" +
        "- get_recommended_centers_for_call: gợi ý trung tâm cho một cuộc gọi\n\n" +

        "Phân biệt rõ: hỏi về an toàn → get_emergency_guide. Hỏi về vật tư → get_supplies_for_disaster. " +
        "Luôn sử dụng công cụ trước khi trả lời nếu cần dữ liệu thực tế. " +
        "Tổng hợp kết quả từ công cụ thành câu trả lời tự nhiên, dễ hiểu. " +
        "Nếu không chắc chắn, hãy nói thật và đề xuất hướng khác.";

    private static final String PUBLIC_SYSTEM_PROMPT = SYSTEM_PROMPT
        + "\n\n=== GIỚI HẠN QUAN TRỌNG ===\n"
        + "Bạn KHÔNG được phép trả lời các câu hỏi về thông tin cuộc gọi khẩn cấp (distress calls) "
        + "— bao gồm: thông tin chi tiết cuộc gọi, trạng thái cuộc gọi, người gọi, vị trí cuộc gọi. "
        + "Nếu người dùng hỏi về cuộc gọi hoặc thông tin liên quan, hãy lịch sự yêu cầu họ đăng nhập. "
        + "Tuy nhiên bạn vẫn có thể trả lời câu hỏi chung về quy trình gửi yêu cầu cứu trợ, cách sử dụng hệ thống, "
        + "kỹ năng ứng phó thiên tai, thông tin trung tâm cứu hộ, và vật tư.";

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

    private static final Map<String, List<String>> GUIDANCE_QUERIES = new HashMap<>();

    static {
        GUIDANCE_QUERIES.put("lũ", List.of("lũ", "lụt", "ngập"));
        GUIDANCE_QUERIES.put("hỏa hoạn", List.of("hỏa hoạn", "cháy"));
        GUIDANCE_QUERIES.put("bão", List.of("bão"));
        GUIDANCE_QUERIES.put("sạt lở", List.of("sạt lở", "lở đất"));
        GUIDANCE_QUERIES.put("động đất", List.of("động đất"));
    }

    private static final Map<String, Map<String, Object>> EMERGENCY_GUIDES = new HashMap<>();

    static {
        EMERGENCY_GUIDES.put("lũ", Map.of(
            "title", "Hướng dẫn ứng phó lũ lụt",
            "before", List.of("Theo dõi dự báo thời tiết", "Chuẩn bị áo phao, thực phẩm khô, nước uống", "Gia cố nhà cửa", "Lên kế hoạch sơ tán"),
            "during", List.of("Di chuyển lên cao ngay lập tức", "Ngắt điện, gas", "Không đi qua vùng ngập nước", "Dùng áo phao nếu cần"),
            "after", List.of("Kiểm tra thiệt hại", "Vệ sinh môi trường", "Liên hệ cứu hộ nếu cần hỗ trợ", "Không ăn thực phẩm ngập nước"),
            "emergency_contacts", "Gọi cứu hộ: 112 | Cấp cứu: 115"
        ));
        EMERGENCY_GUIDES.put("hỏa hoạn", Map.of(
            "title", "Hướng dẫn ứng phó hỏa hoạn",
            "before", List.of("Lắp đặt báo cháy", "Trang bị bình cứu hỏa", "Biết lối thoát hiểm", "Tập duyệt thoát hiểm"),
            "during", List.of("Gọi 114 ngay", "Thoát hiểm bằng cầu thang bộ", "Không dùng thang máy", "Dùng khăn ẩm che mũi miệng", "Cúi thấp người khi di chuyển"),
            "after", List.of("Không quay vào nhà khi chưa an toàn", "Kiểm tra người bị thương", "Sơ cứu vết bỏng bằng nước mát"),
            "emergency_contacts", "Cứu hỏa: 114 | Cấp cứu: 115"
        ));
        EMERGENCY_GUIDES.put("bão", Map.of(
            "title", "Hướng dẫn ứng phó bão",
            "before", List.of("Gia cố nhà cửa, chằng chống cửa sổ", "Dự trữ lương thực, nước uống ít nhất 3 ngày", "Sạc đầy pin điện thoại", "Theo dõi tin tức liên tục"),
            "during", List.of("Ở trong nhà, tránh xa cửa sổ", "Không ra ngoài", "Tắt các thiết bị điện không cần thiết", "Tránh xa các vật dễ đổ"),
            "after", List.of("Kiểm tra thiệt hại", "Tránh dây điện đứt", "Không vào khu vực nguy hiểm", "Liên hệ cứu hộ nếu cần"),
            "emergency_contacts", "Trung tâm dự báo khí tượng | Cứu hộ: 112"
        ));
        EMERGENCY_GUIDES.put("sạt lở", Map.of(
            "title", "Hướng dẫn ứng phó sạt lở đất",
            "before", List.of("Theo dõi cảnh báo sạt lở", "Không xây nhà ở sườn dốc", "Trồng cây chống xói mòn", "Di tản khi có cảnh báo"),
            "during", List.of("Di tản ngay lập tức", "Chạy xa khỏi chân đồi/dốc", "Báo cho hàng xóm", "Không cố lấy đồ đạc"),
            "after", List.of("Không quay lại cho đến khi an toàn", "Kiểm tra người bị thương", "Báo cho chính quyền địa phương"),
            "emergency_contacts", "Cứu hộ: 112 | Cấp cứu: 115"
        ));
        EMERGENCY_GUIDES.put("động đất", Map.of(
            "title", "Hướng dẫn ứng phó động đất",
            "before", List.of("Xác định vị trí an toàn trong nhà", "Chuẩn bị túi cứu hộ", "Cố định đồ đạc cao, nặng"),
            "during", List.of("Núp dưới bàn chắc chắn", "Tránh xa cửa sổ, kính", "Bảo vệ đầu và cổ", "Nếu ở ngoài, tránh xa tòa nhà"),
            "after", List.of("Cẩn thận với dư chấn", "Kiểm tra rò rỉ gas", "Sơ cứu người bị thương", "Nghe tin tức qua radio"),
            "emergency_contacts", "Cứu hộ: 112 | Cấp cứu: 115"
        ));
    }

    private static final String PHONE_MAP = 
        "🚨 Số điện thoại khẩn cấp:\n" +
        "• Cứu hỏa: 114\n" +
        "• Cấp cứu y tế: 115\n" +
        "• Cảnh sát: 113\n" +
        "• Cứu hộ cứu nạn: 112\n" +
        "• Số khẩn cấp châu Âu: 112 (có thể gọi từ điện thoại di động)\n\n" +
        "Hãy gọi ngay khi gặp tình huống nguy hiểm!";

    private static final List<String> ALL_DISASTER_NAMES = List.of(
        "lũ", "lụt", "ngập", "hỏa hoạn", "cháy", "bão",
        "sạt lở", "lở đất", "động đất", "thiên tai"
    );

    private static final Map<String, Map<String, Object>> GENERAL_INFO_MAP = new HashMap<>();

    static {
        GENERAL_INFO_MAP.put("lũ", Map.of(
            "definition", "Lũ lụt là hiện tượng nước dâng cao do mưa lớn, bão hoặc vỡ đê, gây ngập úng trên diện rộng. Lũ lụt là một trong những thiên tai phổ biến và nguy hiểm nhất tại Việt Nam.",
            "causes", List.of("Mưa lớn kéo dài", "Bão và áp thấp nhiệt đới", "Vỡ đê, tràn hồ chứa", "Nước biển dâng do triều cường", "Sạt lở bờ sông, bờ biển"),
            "effects", List.of("Thiệt hại về người và tài sản", "Ô nhiễm nguồn nước", "Dịch bệnh sau lũ", "Mất mùa, thiếu lương thực", "Hư hỏng cơ sở hạ tầng"),
            "prevention", List.of("Theo dõi dự báo thời tiết", "Xây dựng nhà kiên cố, nhà nổi", "Trồng cây chống xói mòn", "Nâng cao ý thức cộng đồng", "Chuẩn bị kế hoạch sơ tán")
        ));
        GENERAL_INFO_MAP.put("hỏa hoạn", Map.of(
            "definition", "Hỏa hoạn là đám cháy không kiểm soát, gây thiệt hại về người và tài sản. Có thể xảy ra do nhiều nguyên nhân như chập điện, rò rỉ gas, hoặc sơ suất trong sinh hoạt.",
            "causes", List.of("Chập điện, quá tải", "Rò rỉ khí gas", "Bất cẩn khi đun nấu", "Đốt rác, đốt rừng", "Xăng dầu, hóa chất dễ cháy"),
            "effects", List.of("Bỏng, ngạt khói, tử vong", "Thiêu rụi tài sản", "Cháy lan sang nhà lân cận", "Ô nhiễm không khí", "Sập đổ công trình"),
            "prevention", List.of("Lắp đặt báo cháy, bình cứu hỏa", "Kiểm tra hệ thống điện định kỳ", "Không để gas rò rỉ", "Tắt thiết bị điện khi không dùng", "Biết lối thoát hiểm")
        ));
        GENERAL_INFO_MAP.put("bão", Map.of(
            "definition", "Bão là xoáy thuận nhiệt đới hình thành trên biển, với sức gió từ cấp 8 trở lên. Bão thường kèm mưa lớn, gây ngập lụt và thiệt hại nghiêm trọng cho các tỉnh ven biển Việt Nam.",
            "causes", List.of("Nhiệt độ nước biển cao", "Biến đổi khí hậu toàn cầu", "Áp suất không khí thấp", "Tương tác giữa các khối khí"),
            "effects", List.of("Tốc mái, sập nhà", "Ngập lụt ven biển", "Gãy đổ cây cối", "Mất điện diện rộng", "Thiệt hại nông nghiệp"),
            "prevention", List.of("Gia cố nhà cửa trước mùa bão", "Dự trữ lương thực, nước uống", "Sơ tán khi có lệnh", "Tránh xa cửa sổ khi bão", "Theo dõi tin tức liên tục")
        ));
        GENERAL_INFO_MAP.put("sạt lở", Map.of(
            "definition", "Sạt lở đất là hiện tượng đất đá trên sườn dốc bị trượt xuống do mưa lớn, xói mòn hoặc tác động của con người. Thường xảy ra ở vùng đồi núi và ven sông.",
            "causes", List.of("Mưa lớn kéo dài", "Phá rừng, mất thảm thực vật", "Đào bới sườn dốc", "Xói mòn chân đồi", "Động đất"),
            "effects", List.of("Vùi lấp nhà cửa, người", "Tắc nghẽn giao thông", "Chia cắt cộng đồng", "Thiệt hại nông nghiệp", "Ô nhiễm nguồn nước"),
            "prevention", List.of("Trồng cây gây rừng", "Không xây nhà ở sườn dốc", "Xây kè chống xói lở", "Di dời khỏi vùng nguy hiểm", "Theo dõi cảnh báo sạt lở")
        ));
        GENERAL_INFO_MAP.put("động đất", Map.of(
            "definition", "Động đất là rung chuyển của mặt đất do sự giải phóng năng lượng đột ngột trong vỏ Trái Đất. Việt Nam nằm trong vùng có nguy cơ động đất thấp nhưng không hoàn toàn tránh được.",
            "causes", List.of("Hoạt động của các mảng kiến tạo", "Đứt gãy địa chất", "Núi lửa phun trào", "Hoạt động khai thác khoáng sản"),
            "effects", List.of("Sập nhà, công trình", "Sóng thần (nếu ở biển)", "Đứt đường dây điện, gas", "Gây hoảng loạn", "Thương vong về người"),
            "prevention", List.of("Xây dựng nhà chống động đất", "Xác định vị trí an toàn trong nhà", "Chuẩn bị túi cứu hộ", "Tập huấn ứng phó khẩn cấp", "Cố định đồ đạc cao, nặng")
        ));
    }

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;
    private final DistressCallRepository callRepository;
    private final DisasterTypeRepository disasterTypeRepository;
    private final RescueCenterRepository centerRepository;
    private final ObjectMapper objectMapper;

    public AiService(RestTemplate restTemplate,
                     @Value("${gemini.api-key:}") String apiKey,
                     @Value("${gemini.model:gemini-2.0-flash}") String model,
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
            return geminiRespond(message, history, SYSTEM_PROMPT, getToolDefinitions());
        } catch (RestClientException e) {
            log.warn("Gemini API call failed ({}), falling back to rule-based", e.getMessage());
            return ruleBasedRespond(message, callId);
        }
    }

    public ChatResponse respondPublic(String message, List<ChatMessage> history) {
        if (apiKey == null || apiKey.isBlank()) {
            return ruleBasedRespondPublic(message);
        }
        try {
            return geminiRespond(message, history, PUBLIC_SYSTEM_PROMPT, getPublicToolDefinitions());
        } catch (RestClientException e) {
            log.warn("Gemini API call failed ({}), falling back to rule-based", e.getMessage());
            return ruleBasedRespondPublic(message);
        }
    }

    @SuppressWarnings("unchecked")
    private ChatResponse geminiRespond(String message, List<ChatMessage> history,
                                        String systemPrompt, List<Map<String, Object>> tools) {
        try {
            String url = GEMINI_BASE_URL + model + ":generateContent?key=" + apiKey;

            List<Map<String, Object>> contents = new ArrayList<>();

            if (history != null) {
                for (ChatMessage msg : history) {
                    String role = "user".equals(msg.role()) ? "user" : "model";
                    contents.add(Map.of("role", role, "parts", List.of(Map.of("text", msg.content()))));
                }
            }
            contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", message))));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", contents);
            requestBody.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));
            requestBody.put("tools", List.of(Map.of("functionDeclarations", tools)));
            requestBody.put("toolConfig", Map.of("functionCallingConfig", Map.of("mode", "AUTO")));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 1000);
            requestBody.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(requestBody, headers), Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return errorResponse("Xin lỗi, đã xảy ra lỗi khi kết nối đến dịch vụ AI.");
            }

            Map<String, Object> functionCall = extractFunctionCall(response.getBody());
            if (functionCall != null) {
                return handleGeminiFunctionCall(functionCall, message, history, systemPrompt, tools);
            }

            String reply = extractGeminiText(response.getBody());
            if (reply == null || reply.isBlank()) {
                return errorResponse("Xin lỗi, tôi không thể tạo câu trả lời lúc này.");
            }
            return new ChatResponse(reply, List.of(), null);

        } catch (RestClientException e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Gemini unexpected error", e);
            return errorResponse("Xin lỗi, đã xảy ra lỗi xử lý.");
        }
    }

    @SuppressWarnings("unchecked")
    private ChatResponse handleGeminiFunctionCall(Map<String, Object> functionCall,
                                                   String originalMessage,
                                                   List<ChatMessage> history,
                                                   String systemPrompt,
                                                   List<Map<String, Object>> tools) {
        try {
            String toolName = (String) functionCall.get("name");
            Map<String, Object> args = (Map<String, Object>) functionCall.get("args");

            if (toolName == null || args == null) {
                return errorResponse("Xin lỗi, tôi không thể xử lý yêu cầu này.");
            }

            Object result = executeTool(toolName, args);
            String content = objectMapper.writeValueAsString(result);

            List<Map<String, Object>> contents = new ArrayList<>();

            if (history != null) {
                for (ChatMessage msg : history) {
                    String role = "user".equals(msg.role()) ? "user" : "model";
                    contents.add(Map.of("role", role, "parts", List.of(Map.of("text", msg.content()))));
                }
            }
            contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", originalMessage))));
            contents.add(Map.of("role", "model", "parts", List.of(Map.of("functionCall", functionCall))));
            contents.add(Map.of("role", "function", "parts", List.of(Map.of("functionResponse", Map.of(
                "name", toolName,
                "response", Map.of("result", content)
            )))));

            String url = GEMINI_BASE_URL + model + ":generateContent?key=" + apiKey;

            Map<String, Object> followUpBody = new HashMap<>();
            followUpBody.put("contents", contents);
            followUpBody.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));
            followUpBody.put("tools", List.of(Map.of("functionDeclarations", tools)));
            followUpBody.put("toolConfig", Map.of("functionCallingConfig", Map.of("mode", "AUTO")));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 1000);
            followUpBody.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> followUpResponse = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(followUpBody, headers), Map.class);

            Map<String, Object> nestedCall = extractFunctionCall(followUpResponse.getBody());
            if (nestedCall != null) {
                return handleGeminiFunctionCall(nestedCall, originalMessage, history, systemPrompt, tools);
            }

            String reply = extractGeminiText(followUpResponse.getBody());
            if (reply == null || reply.isBlank()) {
                return errorResponse("Xin lỗi, tôi không thể xử lý yêu cầu này.");
            }
            return new ChatResponse(reply, List.of(), null);

        } catch (Exception e) {
            log.error("Failed to handle Gemini function call", e);
            return errorResponse("Xin lỗi, đã xảy ra lỗi khi xử lý dữ liệu.");
        }
    }

    @SuppressWarnings("unchecked")
    private String extractGeminiText(Map<String, Object> body) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                Map<String, Object> promptFeedback = (Map<String, Object>) body.get("promptFeedback");
                if (promptFeedback != null) {
                    log.warn("Gemini prompt blocked: {}", promptFeedback);
                }
                return null;
            }
            Map<String, Object> first = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) first.get("content");
            if (content == null) return null;
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) return null;

            StringBuilder text = new StringBuilder();
            for (Map<String, Object> part : parts) {
                if (part.containsKey("text")) {
                    text.append(part.get("text"));
                }
            }
            return text.length() > 0 ? text.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to extract Gemini text", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractFunctionCall(Map<String, Object> body) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
            if (candidates == null || candidates.isEmpty()) return null;
            Map<String, Object> first = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) first.get("content");
            if (content == null) return null;
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) return null;
            for (Map<String, Object> part : parts) {
                if (part.containsKey("functionCall")) {
                    return (Map<String, Object>) part.get("functionCall");
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to extract function call", e);
            return null;
        }
    }

    private Object executeTool(String toolName, Map<String, Object> args) {
        return switch (toolName) {
            case "get_centers" -> executeGetCenters(args);
            case "get_centers_by_location" -> executeGetCentersByLocation(args);
            case "get_call_by_id" -> executeGetCallById(args);
            case "get_calls" -> executeGetCalls(args);
            case "get_calls_by_disaster_type" -> executeGetCallsByDisasterType(args);
            case "get_calls_by_location" -> executeGetCallsByLocation(args);
            case "get_disaster_types" -> executeGetDisasterTypes();
            case "get_supplies_for_disaster" -> executeGetSupplies(args);
            case "get_emergency_guide" -> executeGetEmergencyGuide(args);
            case "get_recommended_centers_for_call" -> executeGetRecommendedCentersForCall(args);
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
            m.put("supplies", c.getSupplies());
            m.put("capacity", c.getCapacity());
            result.add(m);
        }
        return result;
    }

    private List<Map<String, Object>> executeGetCentersByLocation(Map<String, Object> args) {
        String location = (String) args.get("location");
        if (location == null || location.isBlank()) {
            return List.of();
        }

        String lower = location.toLowerCase().trim();
        List<RescueCenter> allCenters = centerRepository.findAll();

        return allCenters.stream()
            .filter(c -> c.getAddress() != null && c.getAddress().toLowerCase().contains(lower))
            .limit(10)
            .map(c -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", c.getId());
                m.put("name", c.getName());
                m.put("type", c.getType().name());
                m.put("address", c.getAddress());
                m.put("phone", c.getPhone());
                m.put("lat", c.getLat());
                m.put("lng", c.getLng());
                return m;
            })
            .collect(Collectors.toList());
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
                m.put("personCount", call.getPersonCount());
                m.put("suggestedSupplies", call.getSuggestedSupplies());
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
                m.put("locationName", call.getLocationName());
                m.put("createdAt", call.getCreatedAt() != null ? call.getCreatedAt().toString() : null);
                return m;
            })
            .toList();
    }

    private List<Map<String, Object>> executeGetCallsByDisasterType(Map<String, Object> args) {
        String typeName = (String) args.get("type");
        if (typeName == null || typeName.isBlank()) return List.of();

        String lower = typeName.toLowerCase().trim();
        List<DistressCall> allCalls = callRepository.findAll();

        return allCalls.stream()
            .filter(c -> c.getDisasterType().getName().toLowerCase().contains(lower))
            .limit(20)
            .map(call -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", call.getId());
                String desc = call.getDescription();
                m.put("description", desc != null && desc.length() > 100 ? desc.substring(0, 100) + "..." : desc);
                m.put("status", call.getStatus().name());
                m.put("urgencyScore", call.getUrgencyScore());
                m.put("locationName", call.getLocationName());
                m.put("createdAt", call.getCreatedAt() != null ? call.getCreatedAt().toString() : null);
                return m;
            })
            .toList();
    }

    private List<Map<String, Object>> executeGetCallsByLocation(Map<String, Object> args) {
        String location = (String) args.get("location");
        if (location == null || location.isBlank()) return List.of();

        String lower = location.toLowerCase().trim();
        List<DistressCall> allCalls = callRepository.findAll();

        return allCalls.stream()
            .filter(c -> {
                if (c.getLocationName() != null && c.getLocationName().toLowerCase().contains(lower)) return true;
                if (c.getDescription() != null && c.getDescription().toLowerCase().contains(lower)) return true;
                return false;
            })
            .limit(20)
            .map(call -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", call.getId());
                String desc = call.getDescription();
                m.put("description", desc != null && desc.length() > 100 ? desc.substring(0, 100) + "..." : desc);
                m.put("status", call.getStatus().name());
                m.put("urgencyScore", call.getUrgencyScore());
                m.put("disasterType", call.getDisasterType().getName());
                m.put("locationName", call.getLocationName());
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

    private Map<String, Object> executeGetEmergencyGuide(Map<String, Object> args) {
        String disasterType = (String) args.get("type");
        if (disasterType == null || disasterType.isBlank()) {
            return Map.of("error", "Vui lòng cung cấp loại thiên tai");
        }

        String lower = disasterType.toLowerCase();
        for (var entry : EMERGENCY_GUIDES.entrySet()) {
            if (lower.contains(entry.getKey()) || entry.getKey().contains(lower)) {
                Map<String, Object> result = new HashMap<>();
                result.put("guide", entry.getValue());
                return result;
            }
        }

        return Map.of("error", "Không tìm thấy hướng dẫn cho loại thiên tai này");
    }

    private Map<String, Object> executeGetRecommendedCentersForCall(Map<String, Object> args) {
        Object idObj = args.get("callId");
        if (idObj == null) return Map.of("error", "Missing callId");
        Long callId;
        if (idObj instanceof Number n) {
            callId = n.longValue();
        } else {
            try {
                callId = Long.parseLong(idObj.toString());
            } catch (NumberFormatException e) {
                return Map.of("error", "Invalid callId: " + idObj);
            }
        }

        var callOpt = callRepository.findById(callId);
        if (callOpt.isEmpty()) {
            return Map.of("error", "Không tìm thấy cuộc gọi #" + callId);
        }

        DistressCall call = callOpt.get();
        String disasterName = call.getDisasterType().getName().toLowerCase();

        RescueCenter.CenterType recommendedType;
        if (disasterName.contains("hỏa hoạn") || disasterName.contains("cháy")) {
            recommendedType = RescueCenter.CenterType.rescue_team;
        } else if (disasterName.contains("sạt lở") || disasterName.contains("lở đất")) {
            recommendedType = RescueCenter.CenterType.rescue_team;
        } else if (disasterName.contains("bão") || disasterName.contains("động đất")) {
            recommendedType = RescueCenter.CenterType.shelter;
        } else {
            recommendedType = RescueCenter.CenterType.supply_distribution;
        }

        List<RescueCenter> recommended = centerRepository.findByType(recommendedType);

        Map<String, Object> result = new HashMap<>();
        result.put("callId", callId);
        result.put("disasterType", call.getDisasterType().getName());
        result.put("urgencyScore", call.getUrgencyScore());
        result.put("recommendedCenterType", recommendedType.name());
        result.put("centers", recommended.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("type", c.getType().name());
            m.put("address", c.getAddress());
            m.put("phone", c.getPhone());
            m.put("capacity", c.getCapacity());
            return m;
        }).collect(Collectors.toList()));

        return result;
    }

    private List<Map<String, Object>> getToolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();

        // get_centers
        tools.add(functionDeclaration("get_centers",
            "Lấy danh sách trung tâm cứu hộ. Có thể lọc theo loại trung tâm.",
            Map.of("type", Map.of(
                "type", "string",
                "enum", List.of("shelter", "supply_distribution", "rescue_team"),
                "description", "Loại trung tâm (shelter: nơi tạm trú, supply_distribution: điểm phát cứu trợ, rescue_team: đội cứu hộ)"
            )),
            List.of()
        ));

        // get_centers_by_location
        tools.add(functionDeclaration("get_centers_by_location",
            "Tìm trung tâm cứu hộ gần một địa điểm hoặc trong một khu vực (tỉnh/thành phố).",
            Map.of("location", Map.of(
                "type", "string",
                "description", "Địa điểm cần tìm (ví dụ: Đà Nẵng, Huế, Quảng Nam)"
            )),
            List.of("location")
        ));

        // get_call_by_id
        tools.add(functionDeclaration("get_call_by_id",
            "Lấy thông tin chi tiết của một cuộc gọi khẩn cấp theo ID. Chỉ dùng khi có số cuộc gọi cụ thể.",
            Map.of("id", Map.of(
                "type", "number",
                "description", "ID của cuộc gọi khẩn cấp"
            )),
            List.of("id")
        ));

        // get_calls
        tools.add(functionDeclaration("get_calls",
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

        // get_calls_by_disaster_type
        tools.add(functionDeclaration("get_calls_by_disaster_type",
            "Lọc danh sách cuộc gọi khẩn cấp theo loại thiên tai. Ví dụ: lũ lụt, hỏa hoạn, bão, sạt lở đất, động đất.",
            Map.of("type", Map.of(
                "type", "string",
                "description", "Loại thiên tai cần lọc (ví dụ: lũ, hỏa hoạn, bão)"
            )),
            List.of("type")
        ));

        // get_calls_by_location
        tools.add(functionDeclaration("get_calls_by_location",
            "Tìm các cuộc gọi khẩn cấp tại một khu vực hoặc thành phố cụ thể.",
            Map.of("location", Map.of(
                "type", "string",
                "description", "Địa điểm cần tìm (ví dụ: Đà Nẵng, Huế, Hà Nội)"
            )),
            List.of("location")
        ));

        // get_disaster_types
        tools.add(functionDeclaration("get_disaster_types",
            "Lấy danh sách tất cả loại thiên tai trong hệ thống.",
            Map.of(),
            List.of()
        ));

        // get_supplies_for_disaster
        tools.add(functionDeclaration("get_supplies_for_disaster",
            "CHỈ DÙNG để lấy danh sách vật tư/nhu yếu phẩm cần chuẩn bị cho một loại thiên tai. KHÔNG dùng cho câu hỏi về an toàn, cách ứng phó.",
            Map.of("type", Map.of(
                "type", "string",
                "description", "Loại thiên tai (ví dụ: lũ, lụt, hỏa hoạn, cháy, bão, sạt lở, động đất)"
            )),
            List.of("type")
        ));

        // get_emergency_guide
        tools.add(functionDeclaration("get_emergency_guide",
            "DÙNG cho câu hỏi về an toàn: nên đi đâu, làm gì, cách ứng phó, xử lý, thoát hiểm khi gặp thiên tai. KHÔNG dùng cho vật tư.",
            Map.of("type", Map.of(
                "type", "string",
                "description", "Loại thiên tai (ví dụ: lũ, hỏa hoạn, bão, sạt lở, động đất)"
            )),
            List.of("type")
        ));

        // get_recommended_centers_for_call
        tools.add(functionDeclaration("get_recommended_centers_for_call",
            "Đề xuất trung tâm cứu hộ phù hợp nhất cho một cuộc gọi khẩn cấp cụ thể, dựa trên loại thiên tai và mức độ khẩn cấp. Dành cho admin.",
            Map.of("callId", Map.of(
                "type", "number",
                "description", "ID của cuộc gọi khẩn cấp cần đề xuất"
            )),
            List.of("callId")
        ));

        return tools;
    }

    private List<Map<String, Object>> getPublicToolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();

        tools.add(functionDeclaration("get_centers",
            "Lấy danh sách trung tâm cứu hộ. Có thể lọc theo loại trung tâm.",
            Map.of("type", Map.of(
                "type", "string",
                "enum", List.of("shelter", "supply_distribution", "rescue_team"),
                "description", "Loại trung tâm (shelter: nơi tạm trú, supply_distribution: điểm phát cứu trợ, rescue_team: đội cứu hộ)"
            )),
            List.of()
        ));

        tools.add(functionDeclaration("get_centers_by_location",
            "Tìm trung tâm cứu hộ gần một địa điểm hoặc trong một khu vực.",
            Map.of("location", Map.of(
                "type", "string",
                "description", "Địa điểm cần tìm"
            )),
            List.of("location")
        ));

        tools.add(functionDeclaration("get_disaster_types",
            "Lấy danh sách tất cả loại thiên tai trong hệ thống.",
            Map.of(),
            List.of()
        ));

        tools.add(functionDeclaration("get_supplies_for_disaster",
            "CHỈ DÙNG để lấy danh sách vật tư/nhu yếu phẩm. KHÔNG dùng cho câu hỏi an toàn.",
            Map.of("type", Map.of(
                "type", "string",
                "description", "Loại thiên tai (ví dụ: lũ, lụt, hỏa hoạn, cháy, bão, sạt lở, động đất)"
            )),
            List.of("type")
        ));

        tools.add(functionDeclaration("get_emergency_guide",
            "DÙNG cho câu hỏi an toàn: nên đi đâu, làm gì, cách ứng phó, thoát hiểm. KHÔNG dùng cho vật tư.",
            Map.of("type", Map.of(
                "type", "string",
                "description", "Loại thiên tai (ví dụ: lũ, hỏa hoạn, bão, sạt lở, động đất)"
            )),
            List.of("type")
        ));

        return tools;
    }

    private Map<String, Object> functionDeclaration(String name, String description,
                                                     Map<String, Object> properties, List<String> required) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        if (!required.isEmpty()) {
            parameters.put("required", required);
        }

        Map<String, Object> declaration = new HashMap<>();
        declaration.put("name", name);
        declaration.put("description", description);
        declaration.put("parameters", parameters);
        return declaration;
    }

    private ChatResponse errorResponse(String message) {
        return new ChatResponse(message, List.of(), null);
    }

    private ChatResponse ruleBasedRespond(String message, Integer callId) {
        String lower = message.toLowerCase().trim();

        if (isGuidanceQuery(lower)) {
            return handleGuidanceQuery(lower);
        }
        if (isGeneralInfoQuery(lower)) {
            return handleGeneralInfoQuery(lower);
        }
        if (isPhoneQuery(lower)) {
            return handlePhoneQuery();
        }
        if (isSuppliesQuery(lower)) {
            return handleSuppliesQuery(lower, callId);
        }
        if (isCallQuery(lower)) {
            return handleCallQuery(lower, callId);
        }
        if (isCenterQuery(lower)) {
            return handleCenterQuery();
        }
        if (isAccountQuery(lower)) {
            return handleAccountQuery();
        }
        return fallback();
    }

    private ChatResponse ruleBasedRespondPublic(String message) {
        String lower = message.toLowerCase().trim();

        if (isGuidanceQuery(lower)) {
            return handleGuidanceQuery(lower);
        }
        if (isGeneralInfoQuery(lower)) {
            return handleGeneralInfoQuery(lower);
        }
        if (isPhoneQuery(lower)) {
            return handlePhoneQuery();
        }
        if (isSuppliesQuery(lower)) {
            return handleSuppliesQuery(lower, null);
        }
        if (isCallQuery(lower)) {
            return new ChatResponse(
                "Vui lòng đăng nhập để xem thông tin chi tiết về cuộc gọi.",
                List.of("Vật tư cho lũ lụt", "Trung tâm cứu hộ", "Đăng nhập"),
                null
            );
        }
        if (isCenterQuery(lower)) {
            return handleCenterQuery();
        }
        if (isAccountQuery(lower)) {
            return handleAccountQuery();
        }
        return fallback();
    }

    private boolean isGuidanceQuery(String text) {
        return text.contains("đi đâu") || text.contains("làm gì") || text.contains("ứng phó")
            || text.contains("xử lý") || text.contains("làm thế nào") || text.contains("phải làm sao")
            || text.contains("nên") || text.contains("cách") || text.contains("hướng dẫn")
            || text.contains("an toàn") || text.contains("thoát hiểm") || text.contains("phòng chống")
            || text.contains("sơ tán") || text.contains("thông tin về");
    }

    private boolean isSuppliesQuery(String text) {
        return text.contains("vật tư") || text.contains("nhu yếu phẩm")
            || text.contains("cần gì") || text.contains("supplies");
    }

    private boolean isCallQuery(String text) {
        return text.contains("cuộc gọi") || text.contains("call")
            || text.contains("trạng thái") || text.contains("tình trạng")
            || Pattern.compile("#\\d+").matcher(text).find();
    }

    private boolean isCenterQuery(String text) {
        return text.contains("trung tâm") || text.contains("cứu hộ") || text.contains("cứu trợ")
            || text.contains("nơi trú ẩn") || text.contains("shelter")
            || text.contains("gần") || text.contains("ở đâu");
    }

    private boolean isGeneralInfoQuery(String text) {
        return text.contains("nguyên nhân") || text.contains("tác hại") || text.contains("hậu quả")
            || text.contains("dấu hiệu") || text.contains("triệu chứng") || text.contains("tác động")
            || text.contains("ảnh hưởng") || text.contains("biểu hiện")
            || text.contains("là gì") || text.contains("thế nào");
    }

    private boolean isPhoneQuery(String text) {
        return text.contains("số điện thoại") || text.contains("số khẩn cấp")
            || text.contains("gọi số") || text.contains("11") || text.contains("số cứu")
            || text.contains("liên hệ") || text.contains("gọi cấp cứu")
            || text.contains("gọi cứu hỏa") || text.contains("gọi cảnh sát")
            || text.contains("điện thoại khẩn cấp") || text.contains("hotline");
    }

    private boolean isAccountQuery(String text) {
        return text.contains("đăng ký") || text.contains("tạo tài khoản")
            || text.contains("quên mật khẩu") || text.contains("reset")
            || text.contains("đăng nhập") || text.contains("login")
            || text.contains("đăng xuất") || text.contains("logout")
            || text.contains("tài khoản") || text.contains("mật khẩu")
            || text.contains("không vào được") || text.contains("không đăng nhập được");
    }

    private ChatResponse handleGuidanceQuery(String text) {
        String lower = text.toLowerCase().trim();
        for (var entry : GUIDANCE_QUERIES.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) {
                    var guide = EMERGENCY_GUIDES.get(entry.getKey());
                    if (guide != null) {
                        String guideType = entry.getKey();
                        String title = (String) guide.get("title");
                        List<String> during = (List<String>) guide.get("during");
                        String contacts = (String) guide.get("emergency_contacts");
                        StringBuilder sb = new StringBuilder("=== " + title + " ===\n\n");
                        sb.append("Khi xảy ra ").append(guideType).append(", bạn cần:\n");
                        for (String step : during) {
                            sb.append("• ").append(step).append("\n");
                        }
                        sb.append("\nLiên hệ khẩn cấp: ").append(contacts);

                        Map<String, Object> data = new HashMap<>();
                        data.put("type", "guidance");
                        data.put("disasterType", guideType);
                        data.put("guide", guide);

                        return new ChatResponse(sb.toString(),
                            List.of("Vật tư cho " + guideType, "Trung tâm cứu hộ gần đây"),
                            data);
                    }
                }
            }
        }
        return new ChatResponse(
            "Bạn cần hỗ trợ về an toàn khi gặp thiên tai. Hãy cho tôi biết loại thiên tai (lũ, hỏa hoạn, bão, sạt lở, động đất).",
            List.of("Ứng phó lũ lụt", "Ứng phó hỏa hoạn", "Trung tâm cứu hộ"),
            null
        );
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

    private ChatResponse handleGeneralInfoQuery(String text) {
        String lower = text.toLowerCase();
        for (var entry : ALL_DISASTER_NAMES) {
            if (lower.contains(entry)) {
                var info = GENERAL_INFO_MAP.get(entry);
                if (info != null) {
                    String definition = (String) info.get("definition");
                    List<String> causes = (List<String>) info.get("causes");
                    List<String> effects = (List<String>) info.get("effects");
                    List<String> prevention = (List<String>) info.get("prevention");

                    StringBuilder sb = new StringBuilder("=== THÔNG TIN VỀ " + entry.toUpperCase() + " ===\n\n");
                    sb.append(definition).append("\n\n");
                    sb.append("Nguyên nhân:\n");
                    for (String c : causes) sb.append("• ").append(c).append("\n");
                    sb.append("\nTác hại:\n");
                    for (String e : effects) sb.append("• ").append(e).append("\n");
                    sb.append("\nCách phòng tránh:\n");
                    for (String p : prevention) sb.append("• ").append(p).append("\n");

                    return new ChatResponse(sb.toString(),
                        List.of("Ứng phó " + entry, "Vật tư cho " + entry, "Trung tâm cứu hộ gần đây"),
                        null);
                }
            }
        }
        return new ChatResponse(
            "Tôi có thông tin về các loại thiên tai: lũ lụt, hỏa hoạn, bão, sạt lở đất, động đất. Bạn muốn tìm hiểu về loại nào?",
            List.of("Thông tin về lũ lụt", "Thông tin về hỏa hoạn", "Trung tâm cứu hộ"),
            null
        );
    }

    private ChatResponse handlePhoneQuery() {
        return new ChatResponse(PHONE_MAP,
            List.of("Thông tin về lũ lụt", "Trung tâm cứu hộ gần đây"),
            null);
    }

    private ChatResponse handleAccountQuery() {
        return new ChatResponse(
            "Để tạo tài khoản, nhấn 'Đăng ký' trên trang chủ và điền thông tin. "
            + "Nếu quên mật khẩu, liên hệ admin để được cấp lại. "
            + "Bạn cũng có thể đăng nhập bằng tài khoản Google hoặc GitHub.",
            List.of("Đăng ký", "Đăng nhập", "Thông tin về lũ lụt"),
            null
        );
    }

    private ChatResponse fallback() {
        return new ChatResponse(
            "Xin lỗi, tôi chưa hiểu ý bạn. Bạn có thể hỏi về:\n"
            + "• Hướng dẫn ứng phó thiên tai (đi đâu, làm gì khi có lũ/cháy/bão...)\n"
            + "• Thông tin về các loại thiên tai (nguyên nhân, tác hại, phòng tránh)\n"
            + "• Số điện thoại khẩn cấp (114, 115, 113)\n"
            + "• Vật tư cứu trợ cần thiết\n"
            + "• Thông tin cuộc gọi cứu hộ\n"
            + "• Trung tâm cứu hộ gần đây\n"
            + "• Tài khoản và đăng nhập",
            List.of("Hướng dẫn ứng phó lũ lụt", "Thông tin về lũ lụt", "Số điện thoại khẩn cấp"),
            null
        );
    }
}
