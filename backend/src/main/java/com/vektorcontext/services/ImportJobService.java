package com.vektorcontext.services;

import com.vektorcontext.models.ImportJob;
import com.vektorcontext.models.ImportJob.ImportStatus;
import com.vektorcontext.repository.ImportJobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ImportJobService {

    private final ImportJobRepository importJobRepository;

    public ImportJobService(ImportJobRepository importJobRepository) {
        this.importJobRepository = importJobRepository;
    }

    public ImportJob create(String fileName, String type) {
        ImportJob job = new ImportJob();
        job.setFileName(fileName);
        job.setType(type);
        job.setStatus(ImportStatus.PROCESSING);
        job.setStartedAt(LocalDateTime.now());
        return importJobRepository.save(job);
    }

    public void success(Long jobId) {
        ImportJob job = importJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job não encontrado: " + jobId));
        job.setStatus(ImportStatus.SUCCESS);
        job.setFinishedAt(LocalDateTime.now());
        importJobRepository.save(job);
    }

    public void error(Long jobId, String errorMessage) {
        ImportJob job = importJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job não encontrado: " + jobId));
        job.setStatus(ImportStatus.ERROR);
        job.setErrorMessage(errorMessage);
        job.setFinishedAt(LocalDateTime.now());
        importJobRepository.save(job);
    }

    public ImportJob findById(Long jobId) {
        return importJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job não encontrado: " + jobId));
    }

    public List<ImportJob> findAll() {
        return importJobRepository.findAll();
    }
}