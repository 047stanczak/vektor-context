package com.vektorcontext.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.vektorcontext.models.ImportJob;
import com.vektorcontext.services.ImportJobService;
import com.vektorcontext.services.parser.ParserProduct;
import com.vektorcontext.services.parser.ParserSeparatedProducts;
import com.vektorcontext.services.parser.ParserSeparationOperation;
import com.vektorcontext.services.parser.ParserSeparationProducts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ImportControllerTest {

    @Mock
    private ParserProduct parserProduct;

    @Mock
    private ParserSeparatedProducts parserSeparatedProducts;

    @Mock
    private ParserSeparationOperation parserSeparationOperations;

    @Mock
    private ParserSeparationProducts parserSeparationProducts;

    @Mock
    private ImportJobService importJobService;

    @InjectMocks
    private ImportController controller;

    @Test
    void importProducts_shouldCreateJobAndParse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "produtos.csv",
                "text/csv", "codigo\n1".getBytes());
        ImportJob job = new ImportJob();
        ReflectionTestUtils.setField(job, "id", 1L);
        when(importJobService.create("produtos.csv", "PRODUCTS")).thenReturn(job);

        ResponseEntity<ImportJob> response = controller.importProducts(file);

        assertThat(response.getStatusCodeValue()).isEqualTo(202);
        assertThat(response.getBody()).isSameAs(job);
        verify(parserProduct).parseProducts(file.getBytes(), 1L);
    }

    @Test
    void importSeparationOperations_shouldCreateJobAndParse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "ops.csv",
                "text/csv", "col1\nval".getBytes());
        ImportJob job = new ImportJob();
        ReflectionTestUtils.setField(job, "id", 2L);
        when(importJobService.create("ops.csv", "SEPARATION_OPERATIONS")).thenReturn(job);

        ResponseEntity<ImportJob> response = controller.importSeparationOperations(file);

        assertThat(response.getStatusCodeValue()).isEqualTo(202);
        assertThat(response.getBody()).isSameAs(job);
        verify(parserSeparationOperations).parseSeparationOperations(file.getBytes(), 2L);
    }

    @Test
    void importSeparatedProducts_shouldCreateJobAndParse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "sep.csv",
                "text/csv", "col\nval".getBytes());
        ImportJob job = new ImportJob();
        ReflectionTestUtils.setField(job, "id", 3L);
        when(importJobService.create("sep.csv", "SEPARATED_PRODUCTS")).thenReturn(job);

        ResponseEntity<ImportJob> response = controller.importSeparatedProducts(file);

        assertThat(response.getStatusCodeValue()).isEqualTo(202);
        assertThat(response.getBody()).isSameAs(job);
        verify(parserSeparatedProducts).parseSeparatedProducts(file.getBytes(), 3L);
    }

    @Test
    void importSeparationProducts_shouldCreateJobAndParse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "itens.csv",
                "text/csv", "col\nval".getBytes());
        ImportJob job = new ImportJob();
        ReflectionTestUtils.setField(job, "id", 4L);
        when(importJobService.create("itens.csv", "SEPARATION_PRODUCTS")).thenReturn(job);

        ResponseEntity<ImportJob> response = controller.importSeparationProducts(file);

        assertThat(response.getStatusCodeValue()).isEqualTo(202);
        assertThat(response.getBody()).isSameAs(job);
        verify(parserSeparationProducts).parseSeparationProducts(file.getBytes(), 4L);
    }

    @Test
    void importProducts_shouldPropagateExceptionFromService() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "prod.csv",
                "text/csv", "c".getBytes());
        when(importJobService.create(any(), any())).thenThrow(new RuntimeException("DB down"));

        assertThatThrownBy(() -> controller.importProducts(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB down");
        verify(parserProduct, never()).parseProducts(any(), any());
    }
}