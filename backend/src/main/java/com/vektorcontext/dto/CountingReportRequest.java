package com.vektorcontext.dto;

import java.util.List;

public class CountingReportRequest {
    private String auditedLabel;
    private String auditType;
    private String auditedAt;
    private List<CountingItemDTO> items;

    public String getAuditedLabel() { return auditedLabel; }
    public void setAuditedLabel(String v) { this.auditedLabel = v; }
    public String getAuditType() { return auditType; }
    public void setAuditType(String v) { this.auditType = v; }
    public String getAuditedAt() { return auditedAt; }
    public void setAuditedAt(String v) { this.auditedAt = v; }
    public List<CountingItemDTO> getItems() { return items; }
    public void setItems(List<CountingItemDTO> v) { this.items = v; }
}