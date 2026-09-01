package com.globaltrade.client;

import com.globaltrade.core.entity.Shipment;
import com.globaltrade.ejb.CustomsGatewayRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;
import java.util.Scanner;

public class CustomsActor {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your Government Official Username: ");
            String username = scanner.nextLine().trim();
            
            System.out.print("Enter your password: ");
            String password = scanner.nextLine().trim();

            java.util.Properties props = new java.util.Properties();
            props.put(Context.SECURITY_PRINCIPAL, username);
            props.put(Context.SECURITY_CREDENTIALS, password);
            Context ctx = new InitialContext(props);
            
            String customsJndi = "ejb:globaltrade-ear/globaltrade-ejb/CustomsGatewayBean!" + CustomsGatewayRemote.class.getName();
            CustomsGatewayRemote customsGateway = (CustomsGatewayRemote) ctx.lookup(customsJndi);

            // Force immediate authentication test due to WildFly lazy JNDI
            try {
                customsGateway.ping();
            } catch (Exception e) {
                System.out.println("Authentication failed: Invalid username or password, or server is unreachable.");
                return;
            }

            System.out.println("Welcome to the Government Customs Operations Portal.");
            System.out.println("Commands: list, approve <ShipmentId>, reject <ShipmentId>, exit");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) break;

                String[] parts = input.split(" ");
                if (parts.length == 0 || parts[0].isEmpty()) continue;

                String cmd = parts[0].toLowerCase();
                try {
                    if (cmd.equals("list")) {
                        List<Shipment> shipments = customsGateway.getPendingClearanceShipments();
                        if (shipments.isEmpty()) {
                            System.out.println("No shipments currently pending customs clearance.");
                        } else {
                            System.out.println("Pending Shipments for Clearance:");
                            for (Shipment s : shipments) {
                                System.out.println("Shipment ID: " + s.getId() + " | Tracking: " + s.getTrackingNumber() + " | Status: " + s.getStatus());
                            }
                        }
                    } else if (cmd.equals("approve")) {
                        if (parts.length < 2) {
                            System.out.println("Usage: approve <ShipmentId>");
                            continue;
                        }
                        Long shipmentId = Long.parseLong(parts[1]);
                        
                        try {
                            customsGateway.approveShipment(shipmentId);
                            System.out.println("Shipment #" + shipmentId + " has been APPROVED and CLEARED.");
                        } catch (Exception e) {
                            System.out.println("Failed to approve shipment: " + e.getMessage());
                        }
                    } else if (cmd.equals("reject")) {
                        if (parts.length < 2) {
                            System.out.println("Usage: reject <ShipmentId>");
                            continue;
                        }
                        Long shipmentId = Long.parseLong(parts[1]);
                        
                        try {
                            customsGateway.rejectShipment(shipmentId);
                            System.out.println("Shipment #" + shipmentId + " has been REJECTED due to missing or invalid documentation.");
                        } catch (Exception e) {
                            System.out.println("Failed to reject shipment: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Unknown command. Available commands: list, approve <id>, reject <id>, exit");
                    }
                } catch (Exception e) {
                    System.out.println("Error processing command: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
