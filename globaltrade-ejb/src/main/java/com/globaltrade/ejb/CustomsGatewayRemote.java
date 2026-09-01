package com.globaltrade.ejb;

import com.globaltrade.core.entity.CustomsDeclaration;
import com.globaltrade.core.entity.Shipment;
import jakarta.ejb.Remote;
import java.util.List;

@Remote
public interface CustomsGatewayRemote {
    CustomsDeclaration submitDeclaration(Long shipmentId, String hsCode, Double taxPaid, String brokerName);
    void approveShipment(Long shipmentId);
    void rejectShipment(Long shipmentId);
    List<Shipment> getPendingClearanceShipments();
    boolean ping();
}
