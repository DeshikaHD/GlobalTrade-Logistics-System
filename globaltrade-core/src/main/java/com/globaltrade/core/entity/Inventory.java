package com.globaltrade.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "inventory")
public class Inventory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "product_name", nullable = false, unique = true, length = 100)
    private String productName;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @NotNull
    @Min(0)
    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable;

    @NotBlank
    @Column(name = "warehouse_location", nullable = false, length = 100)
    private String warehouseLocation;

    @NotNull
    @Min(0)
    @Column(name = "reorder_threshold", nullable = false, columnDefinition = "integer default 0")
    private Integer reorderThreshold = 0;

    @NotNull
    @Min(1)
    @Column(name = "reorder_quantity", nullable = false, columnDefinition = "integer default 1")
    private Integer reorderQuantity = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor primaryVendor;

    public Inventory() {
    }

    public Inventory(String productName, String sku, Integer quantityAvailable, String warehouseLocation) {
        this.productName = productName;
        this.sku = sku;
        this.quantityAvailable = quantityAvailable;
        this.warehouseLocation = warehouseLocation;
    }

    public Inventory(String productName, String sku, Integer quantityAvailable, String warehouseLocation, Integer reorderThreshold, Integer reorderQuantity, Vendor primaryVendor) {
        this.productName = productName;
        this.sku = sku;
        this.quantityAvailable = quantityAvailable;
        this.warehouseLocation = warehouseLocation;
        this.reorderThreshold = reorderThreshold;
        this.reorderQuantity = reorderQuantity;
        this.primaryVendor = primaryVendor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(Integer quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public void setWarehouseLocation(String warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
    }

    public Integer getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public Vendor getPrimaryVendor() {
        return primaryVendor;
    }

    public void setPrimaryVendor(Vendor primaryVendor) {
        this.primaryVendor = primaryVendor;
    }
}
