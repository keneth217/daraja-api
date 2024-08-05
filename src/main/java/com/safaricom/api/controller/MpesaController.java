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
    public String handleCallback(@RequestBody String callbackResponse) {
        try {
            // Delegate the processing to the service
            mpesaService.processCallback(callbackResponse);
            return "ok";
        } catch (Exception e) {
            // Log and handle the exception
            System.err.println("Failed to handle callback: " + e.getMessage());
            return "ok";
        }
    }

    @PostMapping("/confirmation")
    public ResponseEntity<String> handleConfirmation(@RequestBody String request) {
        // Implement this method in MpesaService if needed
        return ResponseEntity.ok("Confirmation handled successfully");
    }

    @PostMapping("/validation")
    public ResponseEntity<String> handleValidation(@RequestBody String request) {
        // Implement this method in MpesaService if needed
        return ResponseEntity.ok("Validation handled successfully");
    }
//    @PostMapping("/api/callback")
//    public ResponseEntity<String> handleCallback(@RequestBody String callbackResponse) {
//        try {
//            // Parse the JSON response
//            JSONObject jsonResponse = new JSONObject(callbackResponse);
//            JSONObject stkCallback = jsonResponse.getJSONObject("Body")
//                    .getJSONObject("stkCallback");
//
//            // Check if CallbackMetadata is present
//            if (!stkCallback.has("CallbackMetadata")) {
//                // Handle failed transactions
//                String resultDesc = stkCallback.getString("ResultDesc");
//                System.out.println("Failed Transaction: " + resultDesc);
//                return ResponseEntity.ok("Data received");
//            }
//
//            // Extract CallbackMetadata
//            JSONObject callbackMetadataJson = stkCallback.getJSONObject("CallbackMetadata");
//            JSONArray itemArray = callbackMetadataJson.getJSONArray("Item");
//
//            String amount = "";
//            String mpesaReceiptNumber = "";
//            String phoneNumber = "";
//
//            for (int i = 0; i < itemArray.length(); i++) {
//                JSONObject itemJson = itemArray.getJSONObject(i);
//                String name = itemJson.getString("Name");
//                String value = itemJson.getString("Value");
//
//                switch (name) {
//                    case "Amount":
//                        amount = value;
//                        break;
//                    case "MpesaReceiptNumber":
//                        mpesaReceiptNumber = value;
//                        break;
//                    case "PhoneNumber":
//                        phoneNumber = value;
//                        break;
//                }
//            }
//
//            // Create and populate StkPushCallback
//            StkPushCallback stkPushCallback = new StkPushCallback();
//            stkPushCallback.setMerchantRequestId(stkCallback.getString("MerchantRequestID"));
//            stkPushCallback.setCheckoutRequestId(stkCallback.getString("CheckoutRequestID"));
//            stkPushCallback.setResultCode(stkCallback.getInt("ResultCode"));
//            stkPushCallback.setResultDesc(stkCallback.getString("ResultDesc"));
//
//            // Create CallbackMetadata and add it to StkPushCallback
//            CallbackMetadata callbackMetadata = new CallbackMetadata();
//            callbackMetadata.setItems(itemArray.toList().stream()
//                    .map(obj -> {
//                        JSONObject itemJson = (JSONObject) obj;
//                        MetadataItem metadataItem = new MetadataItem();
//                        metadataItem.setName(itemJson.getString("Name"));
//                        metadataItem.setValue(itemJson.getString("Value"));
//                        return metadataItem;
//                    })
//                    .collect(Collectors.toList()));
//            stkPushCallback.setCallbackMetadata(callbackMetadata);
//
//            // Save the StkPushCallback which will cascade and save CallbackMetadata and MetadataItem
//            callbackRepository.save(stkPushCallback);
//
//            // Log extracted values
//            System.out.println("Amount: " + amount);
//            System.out.println("Mpesa Receipt Number: " + mpesaReceiptNumber);
//            System.out.println("Phone Number: " + phoneNumber);
//
//            // Respond with confirmation
//            return ResponseEntity.ok("Data received");
//
//        } catch (Exception e) {
//            // Handle exceptions
//            System.err.println("Error processing callback: " + e.getMessage());
//            return ResponseEntity.status(500).body("Error processing callback");
//        }
//    }
}
