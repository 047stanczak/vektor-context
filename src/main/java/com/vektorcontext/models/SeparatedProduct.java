package com.vektorcontext.models;

import jakarta.persistence.*;

@Entity
@Table(name = "separated_product")
public class SeparatedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation", nullable = false)
    private String operation;

    @Column(name = "transaction", nullable = false)
    private String transaction;

    @Column(name = "product_code", nullable = false)
    private Integer productCode;

    @Column(name = "quantity", nullable = false)
    private Double quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "transaction",
        referencedColumnName = "transaction",
        insertable = false,
        updatable = false
    )
    private SeparationOperation separationOperation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public SeparationOperation getSeparationOperation() {
        return separationOperation;
    }

    public void setSeparationOperation(SeparationOperation separationOperation) {
        this.separationOperation = separationOperation;
    }

    
}