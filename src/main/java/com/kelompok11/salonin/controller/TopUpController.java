package com.kelompok11.salonin.controller;

import com.kelompok11.salonin.model.TopupHistory;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.service.TopUpService;
import com.midtrans.Config;
import com.midtrans.ConfigFactory;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransSnapApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/topup")
public class TopUpController {

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
    public String topupPage() {
        return "topup";
    }

    @PostMapping("/token")
    @ResponseBody
    public String createPaymentToken(@RequestBody Map<String, Integer> request) {
        String orderId = UUID.randomUUID().toString();
        
        Map<String, Object> params = new HashMap<>();
        
        Map<String, String> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", orderId);
        transactionDetails.put("gross_amount", String.valueOf(request.get("amount")));
        
        Map<String, String> creditCard = new HashMap<>();
        creditCard.put("secure", "true");
        
        params.put("transaction_details", transactionDetails);
        params.put("credit_card", creditCard);
        
        try {
            return snapApi.createTransactionToken(params);
        } catch (MidtransError e) {
            throw new RuntimeException("Failed to create Midtrans transaction token", e);
        }
    }
}