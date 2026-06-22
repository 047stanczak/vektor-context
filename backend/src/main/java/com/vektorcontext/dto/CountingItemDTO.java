package com.vektorcontext.dto;

import com.vektorcontext.models.StockSnapshot;

public class CountingItemDTO {
    private Integer productCode;
    private String barcode;
    private String description;
    private String complement;
    private Double currentStock;

    public static CountingItemDTO from(StockSnapshot s) {
        CountingItemDTO dto = new CountingItemDTO();
        dto.productCode = s.getProductCode();
        dto.barcode = s.getProduct() != null ? s.getProduct().getBarcode() : null;
        dto.description = s.getProduct() != null ? s.getProduct().getDescription() : null;
        dto.complement = s.getProduct() != null ? s.getProduct().getComplement() : null;
        dto.currentStock = s.getCurrentStock();
        return dto;
    }

    public Integer getProductCode() { return productCode; }
    public String getBarcode() { return barcode; }
    public String getDescription() { return description; }
    public String getComplement() { return complement; }
    public Double getCurrentStock() { return currentStock; }
}