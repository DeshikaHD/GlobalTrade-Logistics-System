# WildFly Application Users List

This document lists the required Application Users that must be added to WildFly using the `add-user.bat` or `add-user.sh` script to successfully run the various client Actor portals in this project.

## 1. Carrier Portal (`CarrierActor.java`)
This actor represents the truck driver. It interacts with `CarrierManagerBean` to update transit statuses.
* **Username:** `carrier1`
* **Password:** `Carrier123!`
* **Group / Role:** `CARRIER`
* **Add Command (Windows):** `add-user.bat -a -u "carrier1" -p "Carrier123!" -g "CARRIER"`

## 2. Warehouse Portal (`WarehouseActor.java`)
This actor represents the warehouse staff who packs the items. It interacts with `WarehouseManagerBean`.
* **Username:** `warehouse1`
* **Password:** `Warehouse123!`
* **Group / Role:** `WAREHOUSE_STAFF`
* **Add Command (Windows):** `add-user.bat -a -u "warehouse1" -p "Warehouse123!" -g "WAREHOUSE_STAFF"`

## 3. Hospital Portal (`HospitalActor.java`)
This actor represents the hospital (the customer). It interacts with `OrderManagerBean` and `InventoryManagerBean`.
* **Username:** `1` *(Note: The Hospital Portal expects the Database ID of the Customer as the username for security validation during order placement. Replace '1' with your actual Customer ID from the DB)*
* **Password:** `Customer123`
* **Group / Role:** `CUSTOMER`

---
**How to add these users:**
Open Command Prompt, navigate to your WildFly `bin` directory (e.g., `C:\wildfly-27.0.1.Final\bin`), and paste the commands provided above.

## 4. Vendor Portal (`VendorActor.java`)
This actor represents the supplier fulfilling restock orders. It interacts with `SupplierIntegrationFacadeBean`.
* **Username:** `vendor1`
* **Password:** `Vendor123!`
* **Group / Role:** `VENDOR`
* **Add Command (Windows):** `add-user.bat -a -u "vendor1" -p "Vendor123!" -g "VENDOR"`
