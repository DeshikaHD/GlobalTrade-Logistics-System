package com.globaltrade.ejb;

import com.globaltrade.core.entity.CustomsDeclaration;
import com.globaltrade.core.entity.Shipment;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface CustomsGatewayLocal {
    CustomsDeclaration submitDeclaration(Long shipmentId, String hsCode, Double taxPaid, String brokerName);
    void approveShipment(Long shipmentId);
    void rejectShipment(Long shipmentId);
    List<Shipment> getPendingClearanceShipments();
}
