package com.vektorcontext.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "separation_product")
public class SeparationProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction")
    private String transaction;

    @Column(name = "snapshot_date")
    private LocalDate snapshotDate;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "store_code")
    private String storeCode;

    @Column(name = "product_code")
    private Integer productCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code", referencedColumnName = "code", nullable = false, insertable = false, updatable = false)
    private Product product;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "stock_pending_creator")
    private String stockPendingCreator;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getStockPendingCreator() {
        return stockPendingCreator;
    }

    public void setStockPendingCreator(String stockPendingCreator) {
        this.stockPendingCreator = stockPendingCreator;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }
}