package com.safaricom.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MpesaController {

    private final MpesaService mpesaService;

    public MpesaController(MpesaService mpesaService) {
        this.mpesaService = mpesaService;
    }

    @GetMapping("/initiate-stk-push")
    public String initiateSTKPush(@RequestParam String phoneNumber, @RequestParam double amount) {
        mpesaService.initiateSTKPush(phoneNumber, amount);
        return "STK Push initiated";
    }
}
