package com.vektorcontext.controller;

import com.vektorcontext.dto.AuditDTO;
import com.vektorcontext.models.Audit;
import com.vektorcontext.repository.AuditRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditRepository repository;

    public AuditController(AuditRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<AuditDTO>> list() {
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc().stream().map(AuditDTO::from).toList());
    }

    @PostMapping
    public ResponseEntity<AuditDTO> save(@RequestBody AuditDTO dto) {
        Audit audit = new Audit();
        audit.setAuditedLabel(dto.getAuditedLabel());
        audit.setAuditType(dto.getAuditType());
        audit.setAuditedAt(dto.getAuditedAt());
        audit.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(AuditDTO.from(repository.save(audit)));
    }
}