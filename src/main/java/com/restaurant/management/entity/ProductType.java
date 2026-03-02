package com.restaurant.management.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ProductTypes")
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_type_id")
    private Integer productTypeId;

    @Column(name = "type_name")
    private String typeName;

    public ProductType() {}

    public Integer getProductTypeId() { return productTypeId; }
    public void setProductTypeId(Integer productTypeId) { this.productTypeId = productTypeId; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
}