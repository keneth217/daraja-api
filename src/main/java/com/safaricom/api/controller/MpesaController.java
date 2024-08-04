package com.safaricom.api.controller;

import com.safaricom.api.service.MpesaService;
import com.safaricom.api.dto.StkPushResponse;
import com.safaricom.api.repository.StkPushResponseRepository;
import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class MpesaController {

    private final MpesaService mpesaService;
    private final StkPushResponseRepository responseRepository;

    @GetMapping("/initiate-stk-push")
    public String initiateSTKPush(@RequestParam String phoneNumber, @RequestParam double amount) {
        mpesaService.initiateSTKPush(phoneNumber, amount);
        return "STK Push initiated";
    }

    @PostMapping("/callback")
    public void handleCallback(@RequestBody String callbackResponse) {
        // Parse the callback response and save to the database
        JSONObject jsonObject = new JSONObject(callbackResponse);
        JSONObject stkCallback = jsonObject.getJSONObject("Body").getJSONObject("stkCallback");
        System.out.println("-------callback----------------");
        System.out.println(stkCallback);
        String merchantRequestId = stkCallback.getString("MerchantRequestID");
        String checkoutRequestId = stkCallback.getString("CheckoutRequestID");
        int resultCode = stkCallback.getInt("ResultCode");
        String resultDesc = stkCallback.getString("ResultDesc");

        // Extract details from CallbackMetadata
        JSONObject metadata = stkCallback.getJSONObject("CallbackMetadata");
        JSONArray items = metadata.getJSONArray("Item");
        System.out.println("-----------------call back metDt------------");
        System.out.println(metadata);
        System.out.println(items);

        String phoneNumber = null;
        double amount = 0;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item.getString("Name").equals("PhoneNumber")) {
                phoneNumber = item.getString("Value");
            } else if (item.getString("Name").equals("Amount")) {
                amount = item.getDouble("Value");
            }
        }

        // Save response to database
        StkPushResponse response = new StkPushResponse();
        response.setPhoneNumber(phoneNumber);
        response.setAmount(amount);
        response.setResponse(callbackResponse);
        response.setTimestamp(getTimestamp());
        responseRepository.save(response);
    }

    @PostMapping("/confirmation")
    public ResponseEntity<String> handleConfirmation(@RequestBody String request) {
        logger.info("Received M-PESA confirmation request: {}", request);

        // Parse and handle the confirmation request here
        // This is typically where you'd update your database with the payment status

        // Example response for success
        return new ResponseEntity<>("Confirmation handled successfully", HttpStatus.OK);
    }

    @PostMapping("/validation")
    public ResponseEntity<String> handleValidation(@RequestBody String request) {
        logger.info("Received M-PESA validation request: {}", request);

        // Parse and handle the validation request here
        // This is typically where you'd validate the request before confirming it

        // Example response for success
        return new ResponseEntity<>("Validation handled successfully", HttpStatus.OK);
    }

    private String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.now().format(formatter);
    }

    @GetMapping("/responses")
    public List<StkPushResponse> getAllResponses() {
        return responseRepository.findAll();
    }


}
