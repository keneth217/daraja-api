package com.safaricom.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final MpesaService mpesaService;

    public PaymentController(MpesaService mpesaService) {
        this.mpesaService = mpesaService;
    }

    @GetMapping("/initiate-stk-push")
    public String initiateStkPush(@RequestParam String phoneNumber, @RequestParam double amount) {
        mpesaService.initiateSTKPush(phoneNumber, amount);
        return "STK Push initiated";
    }
}
