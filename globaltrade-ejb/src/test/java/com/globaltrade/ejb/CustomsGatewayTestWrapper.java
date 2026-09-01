package com.globaltrade.ejb;

import com.globaltrade.core.entity.CustomsDeclaration;
import com.globaltrade.core.entity.Shipment;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.util.List;

@Stateless
@RunAs("CUSTOMS_OFFICIAL")
@PermitAll
public class CustomsGatewayTestWrapper {

    @EJB
    private CustomsGatewayRemote gateway;

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean ping() {
        return gateway.ping();
    }

    public CustomsDeclaration submitDeclaration(Long shipmentId, String hsCode, Double taxPaid, String brokerName) {
        return gateway.submitDeclaration(shipmentId, hsCode, taxPaid, brokerName);
    }

    public void approveShipment(Long shipmentId) {
        gateway.approveShipment(shipmentId);
    }

    public void rejectShipment(Long shipmentId) {
        gateway.rejectShipment(shipmentId);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Shipment> getPendingClearanceShipments() {
        return gateway.getPendingClearanceShipments();
    }
}

