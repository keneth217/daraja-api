package com.safaricom.api.controller;

import com.safaricom.api.dto.StkPushRequest;
import com.safaricom.api.entity.CallbackMetadata;
import com.safaricom.api.entity.MetadataItem;
import com.safaricom.api.entity.StkPushCallback;
import com.safaricom.api.repository.StkPushCallbackRepository;
import com.safaricom.api.service.MpesaService;
import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class MpesaController {

    private final MpesaService mpesaService;
//    private final StkPushCallbackRepository callbackRepository;

    @PostMapping("/initiate-stk-push")
    public ResponseEntity<String> initiateSTKPush(@RequestBody StkPushRequest stkPushRequest) {
        try {
            mpesaService.initiateSTKPush(stkPushRequest);
            return ResponseEntity.ok("STK Push initiated");
        } catch (IOException e) {
            // Log the exception message
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to initiate STK Push");
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleMpesaCallback(@RequestBody String callbackResponse) {
        try {
            // Process the callback
            mpesaService.processCallback(callbackResponse);
            return ResponseEntity.ok("Callback processed successfully");
        } catch (Exception e) {admin
                
            System.err.println("Failed to process callback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing callback");
        }
    }

}
