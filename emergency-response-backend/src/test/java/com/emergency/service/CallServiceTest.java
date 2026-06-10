package com.emergency.service;

import com.emergency.dto.request.DistressCallRequest;
import com.emergency.dto.response.DistressCallResponse;
import com.emergency.exception.ResourceNotFoundException;
import com.emergency.model.DisasterType;
import com.emergency.model.DistressCall;
import com.emergency.model.DistressCall.CallStatus;
import com.emergency.model.User;
import com.emergency.repository.DisasterTypeRepository;
import com.emergency.model.Response;
import com.emergency.repository.DistressCallRepository;
import com.emergency.repository.RescueCenterRepository;
import com.emergency.repository.ResponseRepository;
import com.emergency.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallServiceTest {

    @Mock private DistressCallRepository distressCallRepository;
    @Mock private DisasterTypeRepository disasterTypeRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventEmitter eventEmitter;
    @Mock private RescueCenterRepository rescueCenterRepository;
    @Mock private ResponseRepository responseRepository;

    @InjectMocks private CallService callService;

    @Captor private ArgumentCaptor<DistressCall> callCaptor;

    private User testUser;
    private DisasterType floodType;
    private DistressCall activeCall;
    private DistressCall inProgressCall;
    private DistressCall resolvedCall;
    private DistressCallRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setPassword("pass");
        testUser.setRole(User.Role.user);
        testUser.setActive(true);

        floodType = new DisasterType();
        floodType.setId(1L);
        floodType.setName("Lũ lụt");
        floodType.setSlug("flood");
        floodType.setIcon("🌊");
        floodType.setBaseUrgencyScore(80);
        floodType.setCreatedAt(LocalDateTime.now());

        activeCall = new DistressCall();
        activeCall.setId(1L);
        activeCall.setDisasterType(floodType);
        activeCall.setLat(new BigDecimal("16.0"));
        activeCall.setLng(new BigDecimal("108.0"));
        activeCall.setLocationName("Quận 1, HCM");
        activeCall.setDescription("Ngập lụt sâu 1.5m");
        activeCall.setCallerName("Nguyễn Văn A");
        activeCall.setCallerPhone("0901234567");
        activeCall.setStatus(CallStatus.active);
        activeCall.setUrgencyScore(80);
        activeCall.setPersonCount(3);
        activeCall.setCreatedAt(LocalDateTime.now().minusMinutes(30));

        inProgressCall = new DistressCall();
        inProgressCall.setId(3L);
        inProgressCall.setDisasterType(floodType);
        inProgressCall.setLat(new BigDecimal("16.0"));
        inProgressCall.setLng(new BigDecimal("108.0"));
        inProgressCall.setLocationName("Quận 1, HCM");
        inProgressCall.setDescription("Đang xử lý");
        inProgressCall.setCallerName("Nguyễn Văn A");
        inProgressCall.setCallerPhone("0901234567");
        inProgressCall.setStatus(CallStatus.in_progress);
        inProgressCall.setUrgencyScore(80);
        inProgressCall.setPersonCount(3);
        inProgressCall.setCreatedAt(LocalDateTime.now().minusMinutes(30));

        resolvedCall = new DistressCall();
        resolvedCall.setId(2L);
        resolvedCall.setDisasterType(floodType);
        resolvedCall.setLat(new BigDecimal("16.1"));
        resolvedCall.setLng(new BigDecimal("108.1"));
        resolvedCall.setLocationName("Quận 2, HCM");
        resolvedCall.setDescription("Đã giải quyết");
        resolvedCall.setCallerName("Trần Văn B");
        resolvedCall.setStatus(CallStatus.resolved);
        resolvedCall.setUrgencyScore(80);
        resolvedCall.setPersonCount(2);
        resolvedCall.setCreatedAt(LocalDateTime.now().minusHours(2));
        resolvedCall.setResolvedAt(LocalDateTime.now());

        createRequest = new DistressCallRequest(
            1L, new BigDecimal("16.0"), new BigDecimal("108.0"),
            "Quận 1, HCM", "Cần cứu trợ khẩn", "http://example.com/img.jpg",
            "Nguyễn Văn A", "0901234567", 3
        );
    }

    @Test
    void getAll_NoFilters_ReturnsAllCalls() {
        when(distressCallRepository.findByFilters(null, null, null, null, null))
            .thenReturn(List.of(activeCall, resolvedCall));

        List<DistressCallResponse> result = callService.getAll(null, null, null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void getAll_WithStatusFilter_ReturnsFiltered() {
        when(distressCallRepository.findByFilters(null, CallStatus.active, null, null, null))
            .thenReturn(List.of(activeCall));

        List<DistressCallResponse> result = callService.getAll(null, "active", null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo("active");
    }

    @Test
    void getAll_WithSearchQuery_ReturnsMatchingCalls() {
        when(distressCallRepository.findByFilters(null, null, "ngập", null, null))
            .thenReturn(List.of(activeCall));

        List<DistressCallResponse> result = callService.getAll(null, null, "ngập", null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).containsIgnoringCase("ngập");
    }

    @Test
    void getAll_WithDateRange_ReturnsFiltered() {
        when(distressCallRepository.findByFilters(eq(null), eq(null), eq(null),
            any(java.time.LocalDateTime.class), any(java.time.LocalDateTime.class)))
            .thenReturn(List.of(activeCall));

        List<DistressCallResponse> result = callService.getAll(null, null, null, "2026-06-01", "2026-06-30");

        assertThat(result).hasSize(1);
    }

    @Test
    void getById_Found_ReturnsCall() {
        when(distressCallRepository.findById(1L)).thenReturn(Optional.of(activeCall));

        DistressCallResponse result = callService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.callerName()).isEqualTo("Nguyễn Văn A");
    }

    @Test
    void getById_NotFound_Throws() {
        when(distressCallRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> callService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Distress call not found");
    }

    @Test
    void getMine_ReturnsUserCalls() {
        when(distressCallRepository.findByUserIdOrderByCreatedAtDesc(1L))
            .thenReturn(List.of(activeCall));

        List<DistressCallResponse> result = callService.getMine(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void create_Success_SavesAndEmitsEvent() {
        when(disasterTypeRepository.findById(1L)).thenReturn(Optional.of(floodType));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(distressCallRepository.save(any())).thenAnswer(invocation -> {
            DistressCall saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        DistressCallResponse result = callService.create(createRequest, 1L);

        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.callerName()).isEqualTo("Nguyễn Văn A");
        assertThat(result.urgencyScore()).isEqualTo(80);
        verify(eventEmitter).emitNewCall(any());
    }

    @Test
    void create_DisasterTypeNotFound_Throws() {
        when(disasterTypeRepository.findById(99L)).thenReturn(Optional.empty());

        DistressCallRequest badRequest = new DistressCallRequest(
            99L, new BigDecimal("16.0"), new BigDecimal("108.0"),
            null, "Test", null, "Test", null, null
        );

        assertThatThrownBy(() -> callService.create(badRequest, 1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Disaster type not found");
    }

    @Test
    void updateStatus_ActiveToInProgress_SavesAndReturns() {
        when(distressCallRepository.findById(1L)).thenReturn(Optional.of(activeCall));
        when(distressCallRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DistressCallResponse result = callService.updateStatus(1L, "in_progress");

        assertThat(result.status()).isEqualTo("in_progress");
    }

    @Test
    void updateStatus_InProgressToResolved_SavesAndReturns() {
        when(distressCallRepository.findById(3L)).thenReturn(Optional.of(inProgressCall));
        when(distressCallRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DistressCallResponse result = callService.updateStatus(3L, "resolved");

        assertThat(result.status()).isEqualTo("resolved");
        assertThat(result.resolvedAt()).isNotNull();
    }

    @Test
    void updateStatus_ActiveToResolved_Throws() {
        when(distressCallRepository.findById(1L)).thenReturn(Optional.of(activeCall));

        assertThatThrownBy(() -> callService.updateStatus(1L, "resolved"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("in progress before it can be resolved");
    }

    @Test
    void updateStatus_ActiveToDismissed_SavesAndReturns() {
        when(distressCallRepository.findById(1L)).thenReturn(Optional.of(activeCall));
        when(distressCallRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DistressCallResponse result = callService.updateStatus(1L, "dismissed");

        assertThat(result.status()).isEqualTo("dismissed");
    }

    @Test
    void updateStatus_ResolvedToActive_Throws() {
        when(distressCallRepository.findById(2L)).thenReturn(Optional.of(resolvedCall));

        assertThatThrownBy(() -> callService.updateStatus(2L, "active"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot update status of a resolved call");
    }

    @Test
    void updateStatus_ResolvedToDismissed_SavesAndReturns() {
        when(distressCallRepository.findById(2L)).thenReturn(Optional.of(resolvedCall));
        when(distressCallRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DistressCallResponse result = callService.updateStatus(2L, "dismissed");

        assertThat(result.status()).isEqualTo("dismissed");
    }

    @Test
    void updateStatus_InProgressToResolved_NoCenterRequired_SavesAndReturns() {
        when(distressCallRepository.findById(3L)).thenReturn(Optional.of(inProgressCall));
        when(distressCallRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DistressCallResponse result = callService.updateStatus(3L, "resolved");

        assertThat(result.status()).isEqualTo("resolved");
        assertThat(result.resolvedAt()).isNotNull();
    }

    @Test
    void updateStatus_InvalidStatus_Throws() {
        when(distressCallRepository.findById(1L)).thenReturn(Optional.of(activeCall));

        assertThatThrownBy(() -> callService.updateStatus(1L, "bogus"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid status");
    }

    @Test
    void updateStatus_CallNotFound_Throws() {
        when(distressCallRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> callService.updateStatus(99L, "resolved"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getDashboardStats_ReturnsCorrectCounts() {
        when(distressCallRepository.countByStatus(CallStatus.active)).thenReturn(5L);
        when(distressCallRepository.countByStatus(CallStatus.in_progress)).thenReturn(3L);
        when(distressCallRepository.countByStatus(CallStatus.resolved)).thenReturn(10L);
        when(distressCallRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(activeCall, resolvedCall));
        when(rescueCenterRepository.count()).thenReturn(8L);
        when(distressCallRepository.countByDisasterType()).thenReturn(List.<Object[]>of(
            new Object[]{"flood", 12L}
        ));

        Map<String, Object> stats = callService.getDashboardStats();

        assertThat(stats.get("activeCalls")).isEqualTo(5L);
        assertThat(stats.get("inProgressCalls")).isEqualTo(3L);
        assertThat(stats.get("resolvedCalls")).isEqualTo(10L);
        assertThat(stats.get("totalCalls")).isEqualTo(18L);
        assertThat(stats.get("totalCenters")).isEqualTo(8L);
        assertThat(stats.get("callsByType")).asList().hasSize(1);
        assertThat(stats.get("recentCalls")).asList().hasSize(2);
    }

    @Test
    void create_SetsCorrectFields() {
        when(disasterTypeRepository.findById(1L)).thenReturn(Optional.of(floodType));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(distressCallRepository.save(callCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        callService.create(createRequest, 1L);

        DistressCall saved = callCaptor.getValue();
        assertThat(saved.getLat()).isEqualByComparingTo("16.0");
        assertThat(saved.getLng()).isEqualByComparingTo("108.0");
        assertThat(saved.getDescription()).isEqualTo("Cần cứu trợ khẩn");
        assertThat(saved.getCallerName()).isEqualTo("Nguyễn Văn A");
        assertThat(saved.getUrgencyScore()).isEqualTo(80);
        assertThat(saved.getStatus()).isEqualTo(CallStatus.active);
    }
}
