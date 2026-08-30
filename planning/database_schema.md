# Database Schema Design

This document defines the conceptual core schemas and their cardinalities for the GlobalTrade SCM platform.

## Core Schemas

### 1. Customers
Stores information about the clients (e.g., Hospitals) placing orders.
* **id** (Primary Key)
* **name**
* **type**
* **contact_info**

### 2. Inventory
Tracks the physical goods stored in the Warehouse.
* **id** (Primary Key)
* **product_name**
* **sku**
* **quantity_available**
* **warehouse_location**

### 3. Orders
Represents a request from a Customer for goods.
* **id** (Primary Key)
* **customer_id** (Foreign Key -> Customers.id)
* **order_date**
* **status** (e.g., PENDING, SHIPPED, DELIVERED)

### 4. Order_Items (Junction/Detail Table)
Maps the Many-to-Many relationship between Orders and Inventory (a specific item on an order).
* **id** (Primary Key)
* **order_id** (Foreign Key -> Orders.id)
* **inventory_id** (Foreign Key -> Inventory.id)
* **quantity**

### 5. Shipments
Tracks the physical movement of orders (Outbound to Hospitals or Inbound from Suppliers).
* **id** (Primary Key)
* **order_id** (Foreign Key -> Orders.id)
* **carrier_details**
* **tracking_number**
* **status** (e.g., IN_TRANSIT, CUSTOMS_CLEARED, DELIVERED)
* **type** (INBOUND, OUTBOUND)

### 6. Auditing
Records lifecycle events and system actions for accountability and tracking.
* **id** (Primary Key)
* **entity_name** (e.g., 'Order', 'Shipment')
* **entity_id**
* **action** (e.g., CREATE, UPDATE, STATUS_CHANGE)
* **changed_by**
* **timestamp**

---

## Cardinality & Relationships

* **Customer to Orders:** 1-to-Many (1:N)
  * A single customer can place multiple orders, but an order belongs to exactly one customer.
* **Order to Order_Items:** 1-to-Many (1:N)
  * An order can contain multiple line items.
* **Inventory to Order_Items:** 1-to-Many (1:N)
  * A specific inventory product can appear across multiple order line items.
* **Order to Shipments:** 1-to-Many (1:N)
  * An order can be fulfilled by one or more shipments (if items are split or backordered).
