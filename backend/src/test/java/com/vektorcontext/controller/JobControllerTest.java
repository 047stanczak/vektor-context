package com.vektorcontext.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vektorcontext.exception.JobNotFoundException;
import com.vektorcontext.models.ImportJob;
import com.vektorcontext.services.ImportJobService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock
    private ImportJobService importJobService;

    @InjectMocks
    private JobController controller;

    @Test
    void all_success_returnsJobList() {
        ImportJob job = new ImportJob();
        when(importJobService.findAllByOrderByIdDesc()).thenReturn(List.of(job));

        ResponseEntity<List<ImportJob>> response = controller.all();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsExactly(job);
    }

    @Test
    void all_empty_returnsEmptyList() {
        when(importJobService.findAllByOrderByIdDesc()).thenReturn(List.of());

        ResponseEntity<List<ImportJob>> response = controller.all();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void all_exception_propagates() {
        when(importJobService.findAllByOrderByIdDesc()).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> controller.all())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }

    @Test
    void status_success_returnsJob() {
        Long jobId = 10L;
        ImportJob job = new ImportJob();
        when(importJobService.findById(jobId)).thenReturn(job);

        ResponseEntity<ImportJob> response = controller.status(jobId);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(job);
    }

    @Test
    void status_notFound_throwsException() {
        Long jobId = 99L;
        when(importJobService.findById(jobId)).thenReturn(null);

        assertThatThrownBy(() -> controller.status(jobId))
                .isInstanceOf(JobNotFoundException.class)
                .hasMessageContaining("Job não encontrado: 99");
    }

    @Test
    void status_exception_propagates() {
        Long jobId = 1L;
        when(importJobService.findById(jobId)).thenThrow(new RuntimeException("Service down"));

        assertThatThrownBy(() -> controller.status(jobId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Service down");
    }
}