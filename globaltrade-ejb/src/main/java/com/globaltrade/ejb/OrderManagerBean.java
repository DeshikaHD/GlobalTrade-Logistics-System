package com.globaltrade.ejb;

import com.globaltrade.core.entity.Customer;
import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.OrderItem;
import com.globaltrade.ejb.interceptor.AuditLoggingInterceptor;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Local(OrderManagerLocal.class)
@Remote(OrderManagerRemote.class)
@Interceptors(AuditLoggingInterceptor.class)
@RolesAllowed("CUSTOMER")
public class OrderManagerBean implements OrderManagerLocal, OrderManagerRemote {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Order placeOrder(Long customerId, List<OrderItem> items) {
        String callerName = sessionContext.getCallerPrincipal().getName();
        if (!callerName.equals(String.valueOf(customerId))) {
            throw new SecurityException("Unauthorized: Caller ID does not match requested Customer ID");
        }

        Customer customer = em.find(Customer.class, customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        Order order = new Order(customer, LocalDateTime.now(), "PENDING");
        
        for (OrderItem reqItem : items) {
            String prodName = reqItem.getInventory().getProductName();
            try {
                Inventory inv = em.createQuery("SELECT i FROM Inventory i WHERE i.productName = :name", Inventory.class)
                                  .setParameter("name", prodName)
                                  .getSingleResult();
                OrderItem realItem = new OrderItem(order, inv, reqItem.getQuantity());
                order.addOrderItem(realItem);
            } catch (NoResultException e) {
                throw new IllegalArgumentException("Invalid product name: " + prodName);
            }
        }
        
        em.persist(order);
        em.flush();
        em.detach(order);
        order.setOrderItems(new ArrayList<>(order.getOrderItems()));
        return order;
    }

    @Override
    public List<Order> getOrdersForCustomer(Long customerId) {
        List<Order> orders = em.createQuery(
            "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.customer.id = :custId", Order.class)
            .setParameter("custId", customerId)
            .getResultList();

        List<Order> safeOrders = new ArrayList<>();
        for (Order o : orders) {
            em.detach(o);
            o.setCustomer(null);
            List<OrderItem> safeItems = new ArrayList<>(o.getOrderItems());
            o.setOrderItems(safeItems);
            safeOrders.add(o);
        }
        return safeOrders;
    }
}
