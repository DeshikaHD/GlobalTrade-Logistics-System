package com.globaltrade.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "customs_declarations")
public class CustomsDeclaration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "hs_code", nullable = false)
    private String hsCode;

    @NotNull
    @Column(name = "tax_paid", nullable = false)
    private Double taxPaid;

    @NotBlank
    @Column(name = "broker_name", nullable = false)
    private String brokerName;

    @NotNull
    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    public CustomsDeclaration() {
    }

    public CustomsDeclaration(String hsCode, Double taxPaid, String brokerName, Shipment shipment) {
        this.hsCode = hsCode;
        this.taxPaid = taxPaid;
        this.brokerName = brokerName;
        this.shipment = shipment;
        this.submissionDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHsCode() {
        return hsCode;
    }

    public void setHsCode(String hsCode) {
        this.hsCode = hsCode;
    }

    public Double getTaxPaid() {
        return taxPaid;
    }

    public void setTaxPaid(Double taxPaid) {
        this.taxPaid = taxPaid;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }
}
