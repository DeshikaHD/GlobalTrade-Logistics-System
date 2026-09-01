INSERT INTO customers (name, type, contact_info) VALUES ('General Hospital', 'HOSPITAL', 'contact@genhosp.com');

INSERT INTO vendors (name, contact_info, rating, is_eligible) VALUES ('MediSupplies Inc.', 'orders@medisupplies.com', 100, true);
INSERT INTO vendors (name, contact_info, rating, is_eligible) VALUES ('Global Pharma', 'sales@globalpharma.com', 80, true);

INSERT INTO inventory (product_name, sku, quantity_available, warehouse_location, reorder_threshold, reorder_quantity, vendor_id) VALUES ('Surgical Masks', 'SM-100', 5000, 'A1', 1000, 5000, 1);
INSERT INTO inventory (product_name, sku, quantity_available, warehouse_location, reorder_threshold, reorder_quantity, vendor_id) VALUES ('IV Fluids', 'IV-200', 1000, 'B2', 500, 2000, 2);
INSERT INTO inventory (product_name, sku, quantity_available, warehouse_location, reorder_threshold, reorder_quantity, vendor_id) VALUES ('Syringes', 'SYR-300', 10000, 'C3', 2000, 10000, 1);

INSERT INTO orders (customer_id, order_date, status, trackingNumber) VALUES (1, '2026-09-01 10:00:00', 'SHIPPED', 'TRK-OUT-001');
INSERT INTO orders (customer_id, order_date, status, trackingNumber) VALUES (1, '2026-09-01 11:00:00', 'PENDING', 'TRK-OUT-002');
