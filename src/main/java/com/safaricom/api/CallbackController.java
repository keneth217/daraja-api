package com.safaricom.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/callback")
public class CallbackController {

    private final StkPushResponseRepository responseRepository;

    public CallbackController(StkPushResponseRepository responseRepository) {
        this.responseRepository = responseRepository;
    }

    @PostMapping
    public void handleCallback(@RequestBody String callbackResponse) {
        // Parse the callback response and save to the database
        JSONObject jsonObject = new JSONObject(callbackResponse);
        JSONObject stkCallback = jsonObject.getJSONObject("Body").getJSONObject("stkCallback");

        String merchantRequestId = stkCallback.getString("MerchantRequestID");
        String checkoutRequestId = stkCallback.getString("CheckoutRequestID");
        int resultCode = stkCallback.getInt("ResultCode");
        String resultDesc = stkCallback.getString("ResultDesc");

        // Extract details from CallbackMetadata
        JSONObject metadata = stkCallback.getJSONObject("CallbackMetadata");
        JSONArray items = metadata.getJSONArray("Item");

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

    private String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.now().format(formatter);
    }
}
