package com.emergency.service;

import com.emergency.dto.request.DisasterTypeRequest;
import com.emergency.dto.response.DisasterTypeResponse;
import com.emergency.exception.ResourceNotFoundException;
import com.emergency.model.DisasterType;
import com.emergency.repository.DisasterTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisasterTypeServiceTest {

    @Mock private DisasterTypeRepository disasterTypeRepository;
    @InjectMocks private DisasterTypeService disasterTypeService;
    @Captor private ArgumentCaptor<DisasterType> typeCaptor;

    private DisasterType floodType;
    private DisasterTypeRequest createRequest;

    @BeforeEach
    void setUp() {
        floodType = new DisasterType();
        floodType.setId(1L);
        floodType.setName("Lũ lụt");
        floodType.setSlug("flood");
        floodType.setIcon("🌊");
        floodType.setBaseUrgencyScore(80);
        floodType.setCreatedAt(LocalDateTime.now());

        createRequest = new DisasterTypeRequest("Hỏa hoạn", "fire", "🔥", 90);
    }

    @Test
    void getAll_ReturnsAllTypes() {
        when(disasterTypeRepository.findAllByOrderByNameAsc()).thenReturn(List.of(floodType));

        List<DisasterTypeResponse> result = disasterTypeService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Lũ lụt");
    }

    @Test
    void getById_Found_ReturnsType() {
        when(disasterTypeRepository.findById(1L)).thenReturn(Optional.of(floodType));

        DisasterTypeResponse result = disasterTypeService.getById(1L);

        assertThat(result.name()).isEqualTo("Lũ lụt");
        assertThat(result.slug()).isEqualTo("flood");
    }

    @Test
    void getById_NotFound_Throws() {
        when(disasterTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> disasterTypeService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Disaster type not found");
    }

    @Test
    void create_Success_SavesNewType() {
        when(disasterTypeRepository.existsBySlug("fire")).thenReturn(false);
        when(disasterTypeRepository.save(any())).thenAnswer(invocation -> {
            DisasterType saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        DisasterTypeResponse result = disasterTypeService.create(createRequest);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Hỏa hoạn");
        assertThat(result.slug()).isEqualTo("fire");
        assertThat(result.baseUrgencyScore()).isEqualTo(90);
    }

    @Test
    void create_DuplicateSlug_Throws() {
        when(disasterTypeRepository.existsBySlug("fire")).thenReturn(true);

        assertThatThrownBy(() -> disasterTypeService.create(createRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Slug already exists");
    }

    @Test
    void create_SetsAllFields() {
        when(disasterTypeRepository.existsBySlug("fire")).thenReturn(false);
        when(disasterTypeRepository.save(typeCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        disasterTypeService.create(createRequest);

        DisasterType saved = typeCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("Hỏa hoạn");
        assertThat(saved.getSlug()).isEqualTo("fire");
        assertThat(saved.getIcon()).isEqualTo("🔥");
        assertThat(saved.getBaseUrgencyScore()).isEqualTo(90);
    }

    @Test
    void update_Success_UpdatesExistingType() {
        when(disasterTypeRepository.findById(1L)).thenReturn(Optional.of(floodType));
        when(disasterTypeRepository.existsBySlug("fire")).thenReturn(false);
        when(disasterTypeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DisasterTypeResponse result = disasterTypeService.update(1L, createRequest);

        assertThat(result.name()).isEqualTo("Hỏa hoạn");
        assertThat(result.slug()).isEqualTo("fire");
    }

    @Test
    void update_WithSameSlug_DoesNotCheckUniqueness() {
        DisasterTypeRequest sameSlugRequest = new DisasterTypeRequest("Lũ lụt mới", "flood", "🌊", 85);
        when(disasterTypeRepository.findById(1L)).thenReturn(Optional.of(floodType));
        when(disasterTypeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DisasterTypeResponse result = disasterTypeService.update(1L, sameSlugRequest);

        assertThat(result.slug()).isEqualTo("flood");
        verify(disasterTypeRepository, never()).existsBySlug(any());
    }

    @Test
    void update_SlugTakenByOther_Throws() {
        DisasterTypeRequest otherSlugRequest = new DisasterTypeRequest("Lũ lụt", "fire", "🌊", 85);
        when(disasterTypeRepository.findById(1L)).thenReturn(Optional.of(floodType));
        when(disasterTypeRepository.existsBySlug("fire")).thenReturn(true);

        assertThatThrownBy(() -> disasterTypeService.update(1L, otherSlugRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Slug already exists");
    }

    @Test
    void update_NotFound_Throws() {
        when(disasterTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> disasterTypeService.update(99L, createRequest))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_Success_RemovesType() {
        when(disasterTypeRepository.existsById(1L)).thenReturn(true);

        disasterTypeService.delete(1L);

        verify(disasterTypeRepository).deleteById(1L);
    }

    @Test
    void delete_NotFound_Throws() {
        when(disasterTypeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> disasterTypeService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Disaster type not found");
    }
}
