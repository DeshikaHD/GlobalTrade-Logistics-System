package com.globaltrade.client;

import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.OrderItem;
import com.globaltrade.ejb.CarrierManagerRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;
import java.util.Scanner;

public class CarrierActor {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your Carrier Driver Username: ");
            String username = scanner.nextLine().trim();
            
            System.out.print("Enter your password: ");
            String password = scanner.nextLine().trim();

            java.util.Properties props = new java.util.Properties();
            props.put(Context.SECURITY_PRINCIPAL, username);
            props.put(Context.SECURITY_CREDENTIALS, password);
            Context ctx = new InitialContext(props);
            
            String carrierJndi = "ejb:globaltrade-ear/globaltrade-ejb/CarrierManagerBean!" + CarrierManagerRemote.class.getName();
            CarrierManagerRemote carrierManager = (CarrierManagerRemote) ctx.lookup(carrierJndi);

            // Force immediate authentication test due to WildFly lazy JNDI
            try {
                carrierManager.getShippedOrders();
            } catch (Exception e) {
                System.out.println("Authentication failed: Invalid username or password, or server is unreachable.");
                return;
            }

            System.out.println("Welcome to the Carrier Operations Portal. Commands: manifest, deliver <OrderId>, breakdown <OrderId>, exit");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) break;

                String[] parts = input.split(" ");
                if (parts.length == 0 || parts[0].isEmpty()) continue;

                String cmd = parts[0].toLowerCase();
                try {
                    if (cmd.equals("manifest")) {
                        List<Order> orders = carrierManager.getShippedOrders();
                        if (orders.isEmpty()) {
                            System.out.println("No shipped orders currently on the truck.");
                        } else {
                            for (Order o : orders) {
                                System.out.println("Order #" + o.getId() + " - To: " + o.getCustomer().getName() + " - Date: " + o.getOrderDate());
                                for (OrderItem item : o.getOrderItems()) {
                                    System.out.println("  - " + item.getInventory().getProductName() + " x" + item.getQuantity());
                                }
                            }
                        }
                    } else if (cmd.equals("deliver")) {
                        if (parts.length < 2) {
                            System.out.println("Usage: deliver <OrderId>");
                            continue;
                        }
                        Long orderId = Long.parseLong(parts[1]);
                        
                        try {
                            carrierManager.updateTransitStatus(orderId, "DELIVERED");
                            System.out.println("Order #" + orderId + " successfully marked as DELIVERED!");
                        } catch (Exception e) {
                            System.out.println("Failed to deliver order: " + e.getMessage());
                        }
                    } else if (cmd.equals("breakdown")) {
                        if (parts.length < 2) {
                            System.out.println("Usage: breakdown <OrderId>");
                            continue;
                        }
                        Long orderId = Long.parseLong(parts[1]);
                        
                        try {
                            carrierManager.updateTransitStatus(orderId, "BREAKDOWN");
                            System.out.println("Order #" + orderId + " breakdown reported. Should not reach here because of exception.");
                        } catch (Exception e) {
                            System.out.println("Breakdown recorded (Exception Caught): " + e.getMessage());
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
