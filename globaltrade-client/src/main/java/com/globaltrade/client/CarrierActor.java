package com.globaltrade.client;


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
                carrierManager.getManifest();
            } catch (Exception e) {
                System.out.println("Authentication failed: Invalid username or password, or server is unreachable.");
                return;
            }

            System.out.println("Welcome to the Carrier Operations Portal. Commands: manifest, pickup <TrackingNumber>, deliver <TrackingNumber>, breakdown <TrackingNumber>, exit");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) break;

                String[] parts = input.split(" ");
                if (parts.length == 0 || parts[0].isEmpty()) continue;

                String cmd = parts[0].toLowerCase();
                try {
                    if (cmd.equals("manifest")) {
                        List<String> trackingNumbers = carrierManager.getManifest();
                        if (trackingNumbers.isEmpty()) {
                            System.out.println("No shipped orders or inbound shipments currently waiting.");
                        } else {
                            System.out.println("--- Manifest ---");
                            for (String tracking : trackingNumbers) {
                                System.out.println("Tracking Number: " + tracking);
                            }
                        }
                    } else if (cmd.equals("pickup")) {
                        if (parts.length < 2) {
                            System.out.println("Usage: pickup <TrackingNumber>");
                            continue;
                        }
                        String trackingNumber = parts[1];
                        
                        try {
                            carrierManager.updateTransitStatus(trackingNumber, "IN_TRANSIT");
                            System.out.println("Item " + trackingNumber + " successfully marked as IN_TRANSIT!");
                        } catch (Exception e) {
                            System.out.println("Failed to pickup item: " + e.getMessage());
                        }
                    } else if (cmd.equals("deliver")) {
                        if (parts.length < 2) {
                            System.out.println("Usage: deliver <TrackingNumber>");
                            continue;
                        }
                        String trackingNumber = parts[1];
                        
                        try {
                            carrierManager.updateTransitStatus(trackingNumber, "DELIVERED");
                            System.out.println("Item " + trackingNumber + " successfully marked as DELIVERED!");
                        } catch (Exception e) {
                            System.out.println("Failed to deliver item: " + e.getMessage());
                        }
                    } else if (cmd.equals("breakdown")) {
                        if (parts.length < 2) {
                            System.out.println("Usage: breakdown <TrackingNumber>");
                            continue;
                        }
                        String trackingNumber = parts[1];
                        
                        try {
                            carrierManager.updateTransitStatus(trackingNumber, "BREAKDOWN");
                            System.out.println("Item " + trackingNumber + " breakdown reported. Should not reach here because of exception.");
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
