package com.globaltrade.web;

import com.globaltrade.ejb.DashboardManagerLocal;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    @EJB
    private DashboardManagerLocal dashboardManager;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("outboundOrders", dashboardManager.getAllOutboundOrders());
            request.setAttribute("inboundOrders", dashboardManager.getAllInboundOrders());
            request.setAttribute("inventory", dashboardManager.getAllInventory());
            
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Error loading dashboard data", e);
        }
    }
}
