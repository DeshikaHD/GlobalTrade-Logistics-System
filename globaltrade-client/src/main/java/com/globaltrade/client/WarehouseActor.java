package com.globaltrade.client;

import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.OrderItem;
import com.globaltrade.core.exception.InsufficientStockException;
import com.globaltrade.ejb.WarehouseManagerRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;
import java.util.Scanner;

public class WarehouseActor {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your Warehouse Staff Username: ");
            String username = scanner.nextLine().trim();
            
            System.out.print("Enter your password: ");
            String password = scanner.nextLine().trim();

            java.util.Properties props = new java.util.Properties();
            props.put(Context.SECURITY_PRINCIPAL, username);
            props.put(Context.SECURITY_CREDENTIALS, password);
            Context ctx = new InitialContext(props);
            
            String warehouseJndi = "ejb:globaltrade-ear/globaltrade-ejb/WarehouseManagerBean!" + WarehouseManagerRemote.class.getName();
            WarehouseManagerRemote warehouseManager = (WarehouseManagerRemote) ctx.lookup(warehouseJndi);

            // Force immediate authentication test due to WildFly lazy JNDI
            try {
                warehouseManager.getPendingOrders();
            } catch (Exception e) {
                System.out.println("Authentication failed: Invalid username or password, or server is unreachable.");
                return;
            }

            System.out.println("Welcome to the Warehouse Operations Portal. Commands: pending, pack <OrderId>, exit");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) break;

                String[] parts = input.split(" ");
                if (parts.length == 0 || parts[0].isEmpty()) continue;

                String cmd = parts[0].toLowerCase();
                try {
                    if (cmd.equals("pending")) {
                        List<Order> orders = warehouseManager.getPendingOrders();
                        if (orders.isEmpty()) {
                            System.out.println("No pending orders at the moment.");
                        } else {
                            for (Order o : orders) {
                                System.out.println("Order #" + o.getId() + " - Date: " + o.getOrderDate());
                                for (OrderItem item : o.getOrderItems()) {
                                    System.out.println("  - " + item.getInventory().getProductName() + " x" + item.getQuantity());
                                }
                            }
                        }
                    } else if (cmd.equals("pack")) {
                        if (parts.length < 2) {
                            System.out.println("Usage: pack <OrderId>");
                            continue;
                        }
                        Long orderId = Long.parseLong(parts[1]);
                        
                        try {
                            warehouseManager.packOrder(orderId);
                            System.out.println("Order #" + orderId + " packed successfully! Inventory deducted.");
                        } catch (InsufficientStockException e) {
                            System.out.println("ERROR: Cannot pack order. " + e.getMessage());
                        } catch (Exception e) {
                            System.out.println("Failed to pack order: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Unknown command.");
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
