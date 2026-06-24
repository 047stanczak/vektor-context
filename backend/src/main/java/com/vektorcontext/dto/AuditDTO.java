package com.vektorcontext.dto;

import com.vektorcontext.models.Audit;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AuditDTO {
    private Long id;
    private String auditedLabel;
    private String auditType;
    private LocalDate auditedAt;
    private LocalDateTime createdAt;

    public static AuditDTO from(Audit a) {
        AuditDTO dto = new AuditDTO();
        dto.id = a.getId();
        dto.auditedLabel = a.getAuditedLabel();
        dto.auditType = a.getAuditType();
        dto.auditedAt = a.getAuditedAt();
        dto.createdAt = a.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getAuditedLabel() { return auditedLabel; }
    public void setAuditedLabel(String v) { this.auditedLabel = v; }
    public String getAuditType() { return auditType; }
    public void setAuditType(String v) { this.auditType = v; }
    public LocalDate getAuditedAt() { return auditedAt; }
    public void setAuditedAt(LocalDate v) { this.auditedAt = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}