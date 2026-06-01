
package com.vektorcontext.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_snapshot")
public class StockSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_code", nullable = false)
    private Integer productCode;

    @Column(name = "current_stock", nullable = false)
    private Double currentStock;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    public Double getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Double currentStock) {
        this.currentStock = currentStock;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }
}