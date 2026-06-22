package com.vektorcontext.dto;

import com.vektorcontext.models.SeparationProduct;

public class SeparationProductDTO {

    private Long id;
    private Integer productCode;
    private String productDescription;
    private String storeCode;
    private Double quantity;
    private String productComplement;

    public static SeparationProductDTO from(SeparationProduct s) {
        SeparationProductDTO dto = new SeparationProductDTO();
        dto.id = s.getId();
        dto.productCode = s.getProductCode();
        dto.productDescription = s.getProduct() != null ? s.getProduct().getDescription() : null;
        dto.storeCode = s.getStoreCode();
        dto.quantity = s.getQuantity();
        dto.productComplement = s.getProduct() != null ? s.getProduct().getComplement() : null;
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getProductCode() { return productCode; }
    public void setProductCode(Integer productCode) { this.productCode = productCode; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public String getProductComplement() { return productComplement; }
    public void setProductComplement(String productComplement) { this.productComplement = productComplement; }
}