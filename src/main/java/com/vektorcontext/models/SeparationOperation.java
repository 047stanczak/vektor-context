package com.vektorcontext.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "separation_operation")
public class SeparationOperation {

    @Id
    @Column(name = "transaction", nullable = false)
    private String transaction;

    @Column(name = "operation", nullable = false)
    private String operation;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "time")
    private String time;

    @OneToMany(mappedBy = "separationOperation")
    private List<SeparatedProduct> separatedProducts;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<SeparatedProduct> getSeparatedProducts() {
        return separatedProducts;
    }

    public void setSeparatedProducts(List<SeparatedProduct> separatedProducts) {
        this.separatedProducts = separatedProducts;
    }
}