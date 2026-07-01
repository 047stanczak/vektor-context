package com.vektorcontext.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vektorcontext.api.ApiResponse;
import com.vektorcontext.repository.SeparationOperationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class SeparationOperationControllerTest {

    @Mock
    private SeparationOperationRepository separationOperationRepository;

    @InjectMocks
    private SeparationOperationController controller;

    @Test
    void separators_success_returnsList() {
        List<String> names = List.of("Separator A", "Separator B");
        when(separationOperationRepository.findDistinctSeparatorNames()).thenReturn(names);

        ResponseEntity<ApiResponse<List<String>>> response = controller.separators();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        ApiResponse<List<String>> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isTrue();
        assertThat(body.data()).containsExactly("Separator A", "Separator B");
    }

    @Test
    void separators_empty_returnsEmptyList() {
        when(separationOperationRepository.findDistinctSeparatorNames()).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<String>>> response = controller.separators();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().data()).isEmpty();
    }

    @Test
    void separators_exception_propagates() {
        when(separationOperationRepository.findDistinctSeparatorNames())
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> controller.separators())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }
}