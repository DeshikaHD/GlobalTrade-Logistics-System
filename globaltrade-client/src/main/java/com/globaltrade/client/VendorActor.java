package com.globaltrade.client;

import com.globaltrade.core.entity.SupplierEvaluation;
import com.globaltrade.core.entity.SupplierOrder;
import com.globaltrade.ejb.SupplierIntegrationFacadeRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;
import java.util.Scanner;

public class VendorActor {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your Vendor Username: ");
            String username = scanner.nextLine().trim();
            
            System.out.print("Enter your password: ");
            String password = scanner.nextLine().trim();

            java.util.Properties props = new java.util.Properties();
            props.put(Context.SECURITY_PRINCIPAL, username);
            props.put(Context.SECURITY_CREDENTIALS, password);
            Context ctx = new InitialContext(props);
            
            String vendorJndi = "ejb:globaltrade-ear/globaltrade-ejb/SupplierIntegrationFacadeBean!" + SupplierIntegrationFacadeRemote.class.getName();
            SupplierIntegrationFacadeRemote facade = (SupplierIntegrationFacadeRemote) ctx.lookup(vendorJndi);

            try {
                facade.ping();
            } catch (Exception e) {
                System.out.println("Authentication failed: Invalid username or password, or server is unreachable.");
                return;
            }

            System.out.print("Enter your numeric Vendor ID to connect to your portal: ");
            Long vendorId = Long.parseLong(scanner.nextLine().trim());

            System.out.println("Welcome to the Vendor Fulfillment Portal. Commands: orders, fulfill <id> <hasDocs(true/false)> [trackingNumber], evaluations, exit");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) break;

                String[] parts = input.split(" ");
                if (parts.length == 0 || parts[0].isEmpty()) continue;

                String cmd = parts[0].toLowerCase();
                try {
                    if (cmd.equals("orders")) {
                        List<SupplierOrder> orders = facade.getActiveOrdersForVendor(vendorId);
                        if (orders.isEmpty()) {
                            System.out.println("No active restock requests.");
                        } else {
                            for (SupplierOrder o : orders) {
                                System.out.println("Order #" + o.getOrderId() + " | SKU: " + o.getSku() + " | Qty: " + o.getQuantity() + " | Date: " + o.getPlacementTimestamp());
                            }
                        }
                    } else if (cmd.equals("fulfill")) {
                        if (parts.length < 3) {
                            System.out.println("Usage: fulfill <id> <hasDocs(true/false)> [trackingNumber]");
                            continue;
                        }
                        Long orderId = Long.parseLong(parts[1]);
                        boolean hasDocs = Boolean.parseBoolean(parts[2]);
                        String tracking = parts.length > 3 ? parts[3] : null;
                        
                        try {
                            SupplierOrder result = facade.fulfillOrder(orderId, tracking, hasDocs);
                            System.out.println("Order #" + orderId + " fulfilled! Tracking assigned: " + result.getShipment().getTrackingNumber());
                        } catch (Exception e) {
                            System.out.println("Failed to fulfill order: " + e.getMessage());
                        }
                    } else if (cmd.equals("evaluations")) {
                        List<SupplierEvaluation> evals = facade.getVendorEvaluations(vendorId);
                        if (evals.isEmpty()) {
                            System.out.println("No evaluation records found.");
                        } else {
                            for (SupplierEvaluation e : evals) {
                                System.out.println("Eval ID: " + e.getId() + " | Score: " + e.getScore() + "/100 | Date: " + e.getEvaluationDate() + " | Remarks: " + e.getRemarks());
                            }
                        }
                    } else {
                        System.out.println("Unknown command.");
                    }
                } catch (Exception e) {
                    System.out.println("Error processing command: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
