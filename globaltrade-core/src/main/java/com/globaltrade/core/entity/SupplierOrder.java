package com.globaltrade.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_orders")
public class SupplierOrder implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @NotNull
    @Column(name = "placement_timestamp", nullable = false)
    private LocalDateTime placementTimestamp;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String sku;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    public SupplierOrder() {
    }

    public SupplierOrder(Vendor vendor, String sku, Integer quantity, String status) {
        this.vendor = vendor;
        this.sku = sku;
        this.quantity = quantity;
        this.status = status;
        this.placementTimestamp = LocalDateTime.now();
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getPlacementTimestamp() {
        return placementTimestamp;
    }

    public void setPlacementTimestamp(LocalDateTime placementTimestamp) {
        this.placementTimestamp = placementTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
