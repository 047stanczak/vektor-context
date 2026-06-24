package com.vektorcontext.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit")
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "audited_label")
    private String auditedLabel;

    @Column(name = "audit_type")
    private String auditType;

    @Column(name = "audited_at")
    private LocalDate auditedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public String getAuditedLabel() { return auditedLabel; }
    public void setAuditedLabel(String v) { this.auditedLabel = v; }
    public String getAuditType() { return auditType; }
    public void setAuditType(String v) { this.auditType = v; }
    public LocalDate getAuditedAt() { return auditedAt; }
    public void setAuditedAt(LocalDate v) { this.auditedAt = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}