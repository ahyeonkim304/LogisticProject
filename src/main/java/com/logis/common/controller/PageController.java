package com.logis.common.controller;

import com.logis.auth.dto.SessionUser;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class PageController {

    @ModelAttribute("user")
    public SessionUser currentUser(HttpSession session) {
        return (SessionUser) session.getAttribute("user");
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping({"", "/"})
    public String dashboard() {
        return "dashboard/index";
    }

    @GetMapping("/products")
    public String products() {
        return "product/index";
    }

    @GetMapping("/orders")
    public String orders() {
        return "order/index";
    }

    @GetMapping("/inbound")
    public String inbound() {
        return "inbound/index";
    }

    @GetMapping("/outbound")
    public String outbound() {
        return "outbound/index";
    }

    @GetMapping("/inventory")
    public String inventory() {
        return "inventory/index";
    }

    @GetMapping("/inventory/history")
    public String inventoryHistory() {
        return "inventory/history";
    }

    @GetMapping("/locations")
    public String locations() {
        return "location/index";
    }

    @GetMapping("/location-inventory")
    public String locationInventory() {
        return "location_inventory/index";
    }

    @GetMapping("/pallets")
    public String pallets() {
        return "pallet/index";
    }

    @GetMapping("/admin/accounts")
    public String adminAccounts(HttpSession session, Model model) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        if (user == null || !user.isAdmin()) return "redirect:/";
        return "admin/accounts";
    }
}
