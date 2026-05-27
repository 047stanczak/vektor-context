package com.vektorcontext.models;

import jakarta.persistence.*;

@Entity
@Table(name = "separation_transfer")
public class SeparationTransfer {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "transaction", nullable = false)
    private String transaction;

    @Column(name = "operation", nullable = false)
    private String operation;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "entity_code")
    private Integer entityCode;

    @Column(name = "entity_name")
    private String entityName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "transaction", referencedColumnName = "transaction", insertable = false, updatable = false)
    })
    private SeparationOperation separationOperation;

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

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Integer getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(Integer entityCode) {
        this.entityCode = entityCode;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public SeparationOperation getSeparationOperation() {
        return separationOperation;
    }

    public void setSeparationOperation(SeparationOperation separationOperation) {
        this.separationOperation = separationOperation;
    }
}