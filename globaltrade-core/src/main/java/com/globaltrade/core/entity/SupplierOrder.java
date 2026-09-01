package com.globaltrade.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
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

    @Transient
    private String productName;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "quantity_accepted")
    private Integer quantityAccepted;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @Column(name = "trade_documentation_provided")
    private Boolean tradeDocumentationProvided = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public Integer getQuantityAccepted() {
        return quantityAccepted;
    }

    public void setQuantityAccepted(Integer quantityAccepted) {
        this.quantityAccepted = quantityAccepted;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Boolean getTradeDocumentationProvided() {
        return tradeDocumentationProvided;
    }

    public void setTradeDocumentationProvided(Boolean tradeDocumentationProvided) {
        this.tradeDocumentationProvided = tradeDocumentationProvided;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }
}
