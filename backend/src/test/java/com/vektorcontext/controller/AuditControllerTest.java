package com.vektorcontext.controller;

import static org.assertj.core.api.Assertions.*;

import com.vektorcontext.dto.AuditDTO;
import com.vektorcontext.models.Audit;
import com.vektorcontext.repository.AuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    private AuditRepository repository;

    @InjectMocks
    private AuditController controller;

    @Test
    void listShouldReturnAllAuditsOrderedByCreatedAtDesc() {
        Audit a1 = new Audit();
        ReflectionTestUtils.setField(a1, "id", 1L);
        a1.setAuditedLabel("Label A");
        a1.setAuditType("TYPE_A");
        a1.setAuditedAt(LocalDate.of(2025, 1, 1));
        a1.setCreatedAt(LocalDateTime.of(2025, 1, 1, 12, 0));

        Audit a2 = new Audit();
        ReflectionTestUtils.setField(a2, "id", 2L);
        a2.setAuditedLabel("Label B");
        a2.setAuditType("TYPE_B");
        a2.setAuditedAt(LocalDate.of(2025, 6, 15));
        a2.setCreatedAt(LocalDateTime.of(2025, 6, 15, 9, 0));

        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(a2, a1));

        ResponseEntity<List<AuditDTO>> response = controller.list();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getAuditedLabel()).isEqualTo("Label B");
        assertThat(response.getBody().get(1).getAuditedLabel()).isEqualTo("Label A");
        verify(repository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void listShouldReturnEmptyListWhenNoAudits() {
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        ResponseEntity<List<AuditDTO>> response = controller.list();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void listShouldPropagateExceptionFromRepository() {
        when(repository.findAllByOrderByCreatedAtDesc()).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> controller.list())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    @Test
    void saveShouldSaveAuditAndReturnDto() {
        AuditDTO inputDto = new AuditDTO();
        inputDto.setAuditedLabel("New label");
        inputDto.setAuditType("TYPE_NEW");
        inputDto.setAuditedAt(LocalDate.of(2025, 3, 10));

        Audit savedAudit = new Audit();
        ReflectionTestUtils.setField(savedAudit, "id", 10L);
        savedAudit.setAuditedLabel(inputDto.getAuditedLabel());
        savedAudit.setAuditType(inputDto.getAuditType());
        savedAudit.setAuditedAt(inputDto.getAuditedAt());
        savedAudit.setCreatedAt(LocalDateTime.now());

        when(repository.save(any(Audit.class))).thenReturn(savedAudit);

        ResponseEntity<AuditDTO> response = controller.save(inputDto);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        AuditDTO resultDto = response.getBody();
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getAuditedLabel()).isEqualTo("New label");
        assertThat(resultDto.getAuditType()).isEqualTo("TYPE_NEW");
        assertThat(resultDto.getAuditedAt()).isEqualTo(LocalDate.of(2025, 3, 10));

        ArgumentCaptor<Audit> auditCaptor = ArgumentCaptor.forClass(Audit.class);
        verify(repository).save(auditCaptor.capture());
        Audit capturedAudit = auditCaptor.getValue();
        assertThat(capturedAudit.getAuditedLabel()).isEqualTo("New label");
        assertThat(capturedAudit.getAuditType()).isEqualTo("TYPE_NEW");
        assertThat(capturedAudit.getAuditedAt()).isEqualTo(LocalDate.of(2025, 3, 10));
        assertThat(capturedAudit.getCreatedAt()).isNotNull();
    }

    @Test
    void saveShouldSetCreatedAtToCurrentTime() {
        AuditDTO dto = new AuditDTO();
        dto.setAuditedLabel("Test");
        dto.setAuditType("TEST");
        dto.setAuditedAt(LocalDate.now());

        LocalDateTime beforeCall = LocalDateTime.now();
        when(repository.save(any(Audit.class))).thenAnswer(invocation -> {
            Audit a = invocation.getArgument(0);
            ReflectionTestUtils.setField(a, "id", 1L);
            return a;
        });

        controller.save(dto);

        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
        verify(repository).save(captor.capture());
        LocalDateTime createdAt = captor.getValue().getCreatedAt();
        assertThat(createdAt).isNotNull();
        assertThat(createdAt).isAfterOrEqualTo(beforeCall);
        assertThat(createdAt).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void saveShouldPropagateExceptionFromRepository() {
        AuditDTO dto = new AuditDTO();
        when(repository.save(any(Audit.class))).thenThrow(new RuntimeException("Save failed"));

        assertThatThrownBy(() -> controller.save(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Save failed");
    }

    @Test
    void saveShouldAcceptNullFieldsInDto() {
        AuditDTO inputDto = new AuditDTO();
        inputDto.setAuditedLabel(null);
        inputDto.setAuditType(null);
        inputDto.setAuditedAt(null);

        Audit savedAudit = new Audit();
        ReflectionTestUtils.setField(savedAudit, "id", 5L);
        when(repository.save(any(Audit.class))).thenReturn(savedAudit);

        ResponseEntity<AuditDTO> response = controller.save(inputDto);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(repository).save(any(Audit.class));
    }
}