package com.vektorcontext.models;

import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @Column(name = "code")
    private Integer code;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "description")
    private String description;

    @Column(name = "complement")
    private String complement;

    @Column(name = "brand")
    private String brand;

    @Column(name = "product_group")
    private String productGroup;

    @Column(name = "department")
    private String department;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "pack_quantity")
    private Integer packQuantity;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getProductGroup() {
        return productGroup;
    }

    public void setProductGroup(String productGroup) {
        this.productGroup = productGroup;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Integer getPackQuantity() {
        return packQuantity;
    }

    public void setPackQuantity(Integer packQuantity) {
        this.packQuantity = packQuantity;
    }
}