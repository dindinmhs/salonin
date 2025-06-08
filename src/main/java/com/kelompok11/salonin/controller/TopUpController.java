package com.kelompok11.salonin.controller;

import com.kelompok11.salonin.model.TopupHistory;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.service.NotificationsService;
import com.kelompok11.salonin.service.TopUpService;
import com.midtrans.Config;
import com.midtrans.ConfigFactory;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransSnapApi;
import org.springframework.ui.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/topup")
public class TopUpController {

    @Autowired
    private NotificationsService notificationsService;

    @Autowired
    private TopUpService topUpService;

    private final MidtransSnapApi snapApi;

    public TopUpController() {
        this.snapApi = new ConfigFactory(new Config(
            "SB-Mid-server-0vAybsV3m0nsx5aoqfitXn5_",
            "SB-Mid-client-640_LN6e1AYjHKtL",
            false
        )).getSnapApi();
    }

    @GetMapping
    public String topupPage(Model model) {
        // Dapatkan user dari SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        
        // Dapatkan user dari database berdasarkan email
        String email = auth.getName();
        User user = topUpService.findUserByEmail(email);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }
        
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole().toString());
        
        // Add unread notifications count if you have this functionality
        long unreadNotificationsCount = notificationsService.countUnreadNotifications(user);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);
        
        return "topup";
    }

    @PostMapping("/token")
    @ResponseBody
    public String createPaymentToken(@RequestBody Map<String, Integer> request) {
        String orderId = UUID.randomUUID().toString();
        // Cara alternatif mendapatkan user dari SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        
        // Dapatkan user dari database berdasarkan email
        String email = auth.getName();
        User user = topUpService.findUserByEmail(email);
        
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }
        
        Integer amount = (Integer) request.get("amount");
        
        // Validasi amount
        if (amount == null || amount < 10000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
        }
        
        // Buat objek TopupHistory
        TopupHistory history = new TopupHistory();
        history.setUser(user);
        history.setMidtransId(orderId);
        history.setAmount(request.get("amount"));
        history.setStatus(TopupHistory.Status.SUKSES);
        history.setCreatedAt(LocalDateTime.now());
        
        // Update balance user
        user.setBalance(user.getBalance() + amount);
        
        Map<String, Object> params = new HashMap<>();
        
        Map<String, String> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", orderId);
        transactionDetails.put("gross_amount", String.valueOf(request.get("amount")));
        
        Map<String, String> creditCard = new HashMap<>();
        creditCard.put("secure", "true");
        
        params.put("transaction_details", transactionDetails);
        params.put("credit_card", creditCard);
        
        try {
            // Simpan history ke database
            topUpService.saveTopupHistory(history);
            
            // Buat token Midtrans
            return snapApi.createTransactionToken(params);
        } catch (MidtransError e) {
            throw new RuntimeException("Failed to create Midtrans transaction token", e);
        }
    }

    @GetMapping("/history")
    public String topupHistory(Model model) {
        // Dapatkan user dari SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        
        // Dapatkan user dari database berdasarkan email
        String email = auth.getName();
        User user = topUpService.findUserByEmail(email);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }
        
        List<TopupHistory> histories = topUpService.getTopupHistoryByUser(user);
        model.addAttribute("histories", histories);
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole().toString());
        
        // Add unread notifications count if you have this functionality
        long unreadNotificationsCount = notificationsService.countUnreadNotifications(user);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);
        
        return "topup-history";
    }
}