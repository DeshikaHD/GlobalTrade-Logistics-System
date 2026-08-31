package com.globaltrade.client;

import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.OrderItem;
import com.globaltrade.ejb.InventoryManagerRemote;
import com.globaltrade.ejb.OrderManagerRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HospitalActor {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your logged-in Hospital ID (Username): ");
            String username = scanner.nextLine().trim();
            Long hospitalId = Long.parseLong(username);
            
            System.out.print("Enter your password: ");
            String password = scanner.nextLine().trim();

            java.util.Properties props = new java.util.Properties();
            props.put(Context.SECURITY_PRINCIPAL, username);
            props.put(Context.SECURITY_CREDENTIALS, password);
            Context ctx = new InitialContext(props);
            
            String invJndi = "ejb:globaltrade-ear/globaltrade-ejb/InventoryManagerBean!" + InventoryManagerRemote.class.getName();
            String ordJndi = "ejb:globaltrade-ear/globaltrade-ejb/OrderManagerBean!" + OrderManagerRemote.class.getName();

            InventoryManagerRemote invManager = (InventoryManagerRemote) ctx.lookup(invJndi);
            OrderManagerRemote orderManager = (OrderManagerRemote) ctx.lookup(ordJndi);

            System.out.println("Welcome to the Hospital Portal. Commands: list, order <product> <qty>, history, exit");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) break;

                String[] parts = input.split(" ");
                if (parts.length == 0 || parts[0].isEmpty()) continue;

                String cmd = parts[0].toLowerCase();
                try {
                    if (cmd.equals("list")) {
                        List<Inventory> supplies = invManager.getAvailableInventory();
                        supplies.forEach(i -> System.out.println(i.getProductName() + " - Qty: " + i.getQuantityAvailable()));
                    } else if (cmd.equals("history")) {
                        List<Order> orders = orderManager.getOrdersForCustomer(hospitalId);
                        for (Order o : orders) {
                            System.out.println("Order #" + o.getId() + " [" + o.getStatus() + "]");
                            if ("DELAYED_TRANSIT_ISSUE".equals(o.getStatus())) {
                                System.out.println("  *** ALERT: Delivery delayed due to truck breakdown or weather issue! ***");
                            }
                            for (OrderItem item : o.getOrderItems()) {
                                System.out.println("  - " + item.getInventory().getProductName() + " x" + item.getQuantity());
                            }
                        }
                    } else if (cmd.equals("order")) {
                        if (parts.length < 3) {
                            System.out.println("Usage: order <Product Name> <Quantity>");
                            continue;
                        }
                        int lastIdx = parts.length - 1;
                        int quantity = Integer.parseInt(parts[lastIdx]);
                        
                        StringBuilder productBuilder = new StringBuilder();
                        for (int i = 1; i < lastIdx; i++) {
                            if (i > 1) productBuilder.append(" ");
                            productBuilder.append(parts[i]);
                        }
                        String productName = productBuilder.toString();

                        Inventory dummyInv = new Inventory();
                        dummyInv.setProductName(productName);
                        OrderItem item = new OrderItem(null, dummyInv, quantity);
                        
                        List<OrderItem> items = new ArrayList<>();
                        items.add(item);
                        
                        Order newOrder = orderManager.placeOrder(hospitalId, items);
                        System.out.println("Order placed successfully! Order ID: " + newOrder.getId());
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
