package com.emergency.service;

import com.emergency.dto.request.CenterRequest;
import com.emergency.dto.response.CenterResponse;
import com.emergency.exception.ResourceNotFoundException;
import com.emergency.model.RescueCenter;
import com.emergency.model.RescueCenter.CenterType;
import com.emergency.repository.RescueCenterRepository;
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
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CenterServiceTest {

    @Mock private RescueCenterRepository repository;
    @InjectMocks private CenterService centerService;
    @Captor private ArgumentCaptor<RescueCenter> centerCaptor;

    private RescueCenter shelterCenter;
    private CenterRequest createRequest;

    @BeforeEach
    void setUp() {
        shelterCenter = new RescueCenter();
        shelterCenter.setId(1L);
        shelterCenter.setName("Nhà tạm trú Đống Đa");
        shelterCenter.setType(CenterType.shelter);
        shelterCenter.setLat(new BigDecimal("21.0277644"));
        shelterCenter.setLng(new BigDecimal("105.8341598"));
        shelterCenter.setAddress("456 Tây Sơn, Đống Đa, Hà Nội");
        shelterCenter.setPhone("0902345678");
        shelterCenter.setSupplies("{\"water\":300,\"rice\":150}");
        shelterCenter.setCapacity(300);
        shelterCenter.setCreatedAt(LocalDateTime.now());

        createRequest = new CenterRequest(
            "Điểm cứu trợ Quận 1", "supply_distribution",
            new BigDecimal("10.7768899"), new BigDecimal("106.7005228"),
            "123 Nguyễn Huệ, Quận 1", "0901234567",
            "{\"water\":500}", 500
        );
    }

    @Test
    void getAll_NoFilter_ReturnsAll() {
        when(repository.findAllByOrderByNameAsc()).thenReturn(List.of(shelterCenter));

        List<CenterResponse> result = centerService.getAll(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Nhà tạm trú Đống Đa");
    }

    @Test
    void getAll_WithTypeFilter_ReturnsFiltered() {
        when(repository.findByType(CenterType.shelter)).thenReturn(List.of(shelterCenter));

        List<CenterResponse> result = centerService.getAll("shelter");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo("shelter");
    }

    @Test
    void getById_Found_ReturnsCenter() {
        when(repository.findById(1L)).thenReturn(Optional.of(shelterCenter));

        CenterResponse result = centerService.getById(1L);

        assertThat(result.name()).isEqualTo("Nhà tạm trú Đống Đa");
    }

    @Test
    void getById_NotFound_Throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> centerService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Rescue center not found");
    }

    @Test
    void create_Success_SavesNewCenter() {
        when(repository.findByNameOrderByIdAsc("Điểm cứu trợ Quận 1")).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(invocation -> {
            RescueCenter saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        CenterResponse result = centerService.create(createRequest);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Điểm cứu trợ Quận 1");
        assertThat(result.type()).isEqualTo("supply_distribution");
    }

    @Test
    void create_DuplicateName_Throws() {
        when(repository.findByNameOrderByIdAsc("Điểm cứu trợ Quận 1")).thenReturn(List.of(new RescueCenter()));

        assertThatThrownBy(() -> centerService.create(createRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("name already exists");
    }

    @Test
    void create_SetsAllFields() {
        when(repository.findByNameOrderByIdAsc("Điểm cứu trợ Quận 1")).thenReturn(List.of());
        when(repository.save(centerCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        centerService.create(createRequest);

        RescueCenter saved = centerCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("Điểm cứu trợ Quận 1");
        assertThat(saved.getType()).isEqualTo(CenterType.supply_distribution);
        assertThat(saved.getLat()).isEqualByComparingTo("10.7768899");
        assertThat(saved.getAddress()).isEqualTo("123 Nguyễn Huệ, Quận 1");
        assertThat(saved.getCapacity()).isEqualTo(500);
    }

    @Test
    void update_Success_UpdatesExisting() {
        when(repository.findById(1L)).thenReturn(Optional.of(shelterCenter));
        when(repository.findByNameOrderByIdAsc("Điểm cứu trợ Quận 1")).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CenterResponse result = centerService.update(1L, createRequest);

        assertThat(result.name()).isEqualTo("Điểm cứu trợ Quận 1");
        assertThat(result.type()).isEqualTo("supply_distribution");
    }

    @Test
    void update_WithSameName_DoesNotCheckUniqueness() {
        CenterRequest sameNameRequest = new CenterRequest(
            "Nhà tạm trú Đống Đa", "shelter",
            new BigDecimal("21.0"), new BigDecimal("105.8"),
            "Đống Đa, HN", "0902345678", null, 300
        );
        when(repository.findById(1L)).thenReturn(Optional.of(shelterCenter));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CenterResponse result = centerService.update(1L, sameNameRequest);

        assertThat(result.name()).isEqualTo("Nhà tạm trú Đống Đa");
        verify(repository, never()).findByNameOrderByIdAsc(any());
    }

    @Test
    void update_NameTakenByOther_Throws() {
        when(repository.findById(1L)).thenReturn(Optional.of(shelterCenter));
        when(repository.findByNameOrderByIdAsc("Điểm cứu trợ Quận 1"))
            .thenReturn(List.of(new RescueCenter()));

        assertThatThrownBy(() -> centerService.update(1L, createRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("name already exists");
    }

    @Test
    void update_NotFound_Throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> centerService.update(99L, createRequest))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_Success_Removes() {
        when(repository.existsById(1L)).thenReturn(true);

        centerService.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_NotFound_Throws() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> centerService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Rescue center not found");
    }
}
