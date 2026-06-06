package com.vektorcontext.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "divergence_record")
public class DivergenceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "store_code", nullable = false)
    private Integer storeCode;

    @Column(name = "product_code", nullable = false)
    private Integer productCode;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "quantity", nullable = false)
    private Double quantity;

    @Column(name = "current_stock")
    private Double currentStock;

    @Column(name = "separator_name")
    private String separatorName;

    @Column(name = "nf")
    private String nf;

    @Column(name = "observation", columnDefinition = "TEXT")
    private String observation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code", referencedColumnName = "code", nullable = false, insertable = false, updatable = false)
    private Product product;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}