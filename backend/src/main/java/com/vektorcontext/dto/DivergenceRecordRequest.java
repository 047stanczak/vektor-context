package com.vektorcontext.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DivergenceRecordRequest {

    @NotNull
    private LocalDate date;
    @NotNull
    private Integer storeCode;
    @NotNull
    private Integer productCode;
    @NotBlank
    private String tipo;
    @NotNull
    private Double quantity;
    private Double currentStock;
    @NotBlank
    private String separatorName;
    private String nf;
    private String observation;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(Integer storeCode) {
        this.storeCode = storeCode;
    }

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Double currentStock) {
        this.currentStock = currentStock;
    }

    public String getSeparatorName() {
        return separatorName;
    }

    public void setSeparatorName(String separatorName) {
        this.separatorName = separatorName;
    }

    public String getNf() {
        return nf;
    }

    public void setNf(String nf) {
        this.nf = nf;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
}