<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>GlobalTrade Logistics Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-color: #0f172a;
            --surface-color: #1e293b;
            --primary-color: #3b82f6;
            --primary-hover: #2563eb;
            --text-main: #f8fafc;
            --text-muted: #94a3b8;
            --border-color: #334155;
            --success: #10b981;
            --warning: #f59e0b;
            --danger: #ef4444;
        }

        body {
            font-family: 'Inter', sans-serif;
            background-color: var(--bg-color);
            color: var(--text-main);
            margin: 0;
            padding: 0;
            line-height: 1.6;
        }

        .dashboard-container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 2rem;
        }

        header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 3rem;
            padding-bottom: 1rem;
            border-bottom: 1px solid var(--border-color);
        }

        h1 {
            font-size: 2rem;
            font-weight: 700;
            margin: 0;
            background: linear-gradient(to right, #60a5fa, #a78bfa);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .user-info {
            font-size: 0.9rem;
            color: var(--text-muted);
        }

        .section-header {
            margin-top: 3rem;
            margin-bottom: 1.5rem;
            font-size: 1.5rem;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .section-header::before {
            content: '';
            display: block;
            width: 4px;
            height: 24px;
            background-color: var(--primary-color);
            border-radius: 2px;
        }

        .table-container {
            background-color: var(--surface-color);
            border-radius: 12px;
            padding: 1px;
            overflow-x: auto;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
            border: 1px solid var(--border-color);
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }

        .table-container:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
        }

        table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
        }

        th {
            background-color: rgba(15, 23, 42, 0.6);
            padding: 1rem 1.5rem;
            font-weight: 600;
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: var(--text-muted);
            border-bottom: 1px solid var(--border-color);
        }

        td {
            padding: 1rem 1.5rem;
            border-bottom: 1px solid var(--border-color);
            font-size: 0.95rem;
        }

        tr:last-child td {
            border-bottom: none;
        }

        tbody tr {
            transition: background-color 0.15s ease;
        }

        tbody tr:hover {
            background-color: rgba(255, 255, 255, 0.03);
        }

        .status-badge {
            display: inline-block;
            padding: 0.25rem 0.75rem;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        .status-pending { background-color: rgba(245, 158, 11, 0.2); color: var(--warning); }
        .status-shipped, .status-requested { background-color: rgba(59, 130, 246, 0.2); color: var(--primary-color); }
        .status-delivered, .status-fulfilled, .status-cleared { background-color: rgba(16, 185, 129, 0.2); color: var(--success); }
        .status-rejected { background-color: rgba(239, 68, 68, 0.2); color: var(--danger); }

        /* Inventory specific styles */
        .qty-high { color: var(--success); font-weight: 600; }
        .qty-low { color: var(--warning); font-weight: 600; }
        .qty-critical { color: var(--danger); font-weight: 700; }

    </style>
</head>
<body>

<div class="dashboard-container">
    <header>
        <h1>Logistics Dashboard</h1>
        <div class="user-info">
            Welcome, <strong><%= request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "Guest" %></strong>
        </div>
    </header>

    <!-- Outbound Orders Section -->
    <h2 class="section-header">Outbound Orders (Customer Shipments)</h2>
    <div class="table-container">
        <table>
            <thead>
                <tr>
                    <th>Order ID</th>
                    <th>Customer Name</th>
                    <th>Date</th>
                    <th>Tracking #</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="order" items="${outboundOrders}">
                    <tr>
                        <td>#${order.id}</td>
                        <td>${order.customer.name}</td>
                        <td>${order.orderDate}</td>
                        <td>${order.trackingNumber != null ? order.trackingNumber : 'N/A'}</td>
                        <td>
                            <c:choose>
                                <c:when test="${order.status == 'PENDING' || order.status == 'PACKED'}">
                                    <span class="status-badge status-pending">${order.status}</span>
                                </c:when>
                                <c:when test="${order.status == 'SHIPPED'}">
                                    <span class="status-badge status-shipped">${order.status}</span>
                                </c:when>
                                <c:when test="${order.status == 'DELIVERED'}">
                                    <span class="status-badge status-delivered">${order.status}</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-badge status-pending">${order.status}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty outboundOrders}">
                    <tr>
                        <td colspan="5" style="text-align: center; color: var(--text-muted);">No outbound orders found.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>

    <!-- Inbound Orders Section -->
    <h2 class="section-header">Inbound Orders (Supplier Restock)</h2>
    <div class="table-container">
        <table>
            <thead>
                <tr>
                    <th>Order ID</th>
                    <th>Vendor</th>
                    <th>Product SKU</th>
                    <th>Qty Requested</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="supOrder" items="${inboundOrders}">
                    <tr>
                        <td>#${supOrder.id}</td>
                        <td>${supOrder.vendor.name}</td>
                        <td>${supOrder.sku}</td>
                        <td>${supOrder.quantityRequested}</td>
                        <td>
                            <c:choose>
                                <c:when test="${supOrder.status == 'REQUESTED'}">
                                    <span class="status-badge status-requested">${supOrder.status}</span>
                                </c:when>
                                <c:when test="${supOrder.status == 'FULFILLED'}">
                                    <span class="status-badge status-fulfilled">${supOrder.status}</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-badge status-pending">${supOrder.status}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty inboundOrders}">
                    <tr>
                        <td colspan="5" style="text-align: center; color: var(--text-muted);">No inbound orders found.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>

    <!-- Inventory Section -->
    <h2 class="section-header">Warehouse Inventory</h2>
    <div class="table-container">
        <table>
            <thead>
                <tr>
                    <th>Product Name</th>
                    <th>SKU</th>
                    <th>Warehouse Location</th>
                    <th>Available Quantity</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="item" items="${inventory}">
                    <tr>
                        <td>${item.productName}</td>
                        <td>${item.sku}</td>
                        <td>${item.warehouseLocation}</td>
                        <td>
                            <c:choose>
                                <c:when test="${item.quantityAvailable > 100}">
                                    <span class="qty-high">${item.quantityAvailable}</span>
                                </c:when>
                                <c:when test="${item.quantityAvailable > 20}">
                                    <span class="qty-low">${item.quantityAvailable}</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="qty-critical">${item.quantityAvailable}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty inventory}">
                    <tr>
                        <td colspan="4" style="text-align: center; color: var(--text-muted);">No inventory records found.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

</body>
</html>
