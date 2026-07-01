package com.vektorcontext.services;

import com.vektorcontext.models.ImportJob;
import com.vektorcontext.models.ImportJob.ImportStatus;
import com.vektorcontext.repository.ImportJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportJobServiceTest {

    @Mock
    private ImportJobRepository importJobRepository;

    @InjectMocks
    private ImportJobService importJobService;

    @Test
    void create_savesJobWithProcessingStatus() {
        when(importJobRepository.save(any(ImportJob.class))).thenAnswer(i -> i.getArgument(0));

        ImportJob result = importJobService.create("produtos.csv", "PRODUCTS");

        assertEquals("produtos.csv", result.getFileName());
        assertEquals("PRODUCTS", result.getType());
        assertEquals(ImportStatus.PROCESSING, result.getStatus());
        assertNotNull(result.getStartedAt());
    }

    @Test
    void success_updatesStatusToSuccess() {
        ImportJob job = new ImportJob();
        job.setStatus(ImportStatus.PROCESSING);
        when(importJobRepository.findById(1L)).thenReturn(Optional.of(job));

        importJobService.success(1L);

        assertEquals(ImportStatus.SUCCESS, job.getStatus());
        assertNotNull(job.getFinishedAt());
        verify(importJobRepository).save(job);
    }

    @Test
    void success_notFound_throwsException() {
        when(importJobRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> importJobService.success(1L));
    }

    @Test
    void error_updatesStatusToErrorWithMessage() {
        ImportJob job = new ImportJob();
        job.setStatus(ImportStatus.PROCESSING);
        when(importJobRepository.findById(1L)).thenReturn(Optional.of(job));

        importJobService.error(1L, "Falha no parser");

        assertEquals(ImportStatus.ERROR, job.getStatus());
        assertEquals("Falha no parser", job.getErrorMessage());
        assertNotNull(job.getFinishedAt());
        verify(importJobRepository).save(job);
    }

    @Test
    void error_notFound_throwsException() {
        when(importJobRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> importJobService.error(1L, "erro"));
    }

    @Test
    void findById_returnsJob() {
        ImportJob job = new ImportJob();
        when(importJobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertEquals(job, importJobService.findById(1L));
    }

    @Test
    void findById_notFound_throwsException() {
        when(importJobRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> importJobService.findById(1L));
    }

    @Test
    void findAllByOrderByIdDesc_returnsList() {
        when(importJobRepository.findAllByOrderByIdDesc()).thenReturn(List.of(new ImportJob()));

        List<ImportJob> result = importJobService.findAllByOrderByIdDesc();

        assertEquals(1, result.size());
    }
}
