package com.safaricom.api.service;

import com.safaricom.api.dto.StkPushRequest;
import com.safaricom.api.entity.CallbackMetadata;
import com.safaricom.api.entity.MetadataItem;
import com.safaricom.api.entity.MpesaTransaction;
import com.safaricom.api.entity.StkPushCallback;
import com.safaricom.api.repository.MpesaTransactionRepository;
import com.safaricom.api.repository.StkPushCallbackRepository;
import jakarta.transaction.Transactional;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class MpesaService {

    @Value("${safaricom.api.url}")
    private String apiUrl;

    @Value("${safaricom.api.username}")
    private String username;

    @Value("${safaricom.api.password}")
    private String password;

    @Value("${safaricom.api.lipa_na_mpesa_online_shortcode}")
    private String lipaNaMpesaShortcode;

    @Value("${safaricom.api.lipa_na_mpesa_online_passkey}")
    private String lipaNaMpesaPasskey;

    @Value("${safaricom.api.callback_url}")
    private String callbackUrl;
private final MpesaTransactionRepository transactionRepository;
    private final StkPushCallbackRepository callbackRepository;
    private final ObjectMapper objectMapper;



    public MpesaService(MpesaTransactionRepository transactionRepository, StkPushCallbackRepository callbackRepository, ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.callbackRepository = callbackRepository;
        this.objectMapper = objectMapper;
    }

    // Method to generate OAuth token
    public String generateToken() throws IOException {
        OkHttpClient client = new OkHttpClient();
        String tokenUrl = apiUrl + "/oauth/v1/generate?grant_type=client_credentials";
        String credentials = Credentials.basic(username, password); // Base64 encoding of username and password
        Request request = new Request.Builder()
                .url(tokenUrl)
                .get()
                .addHeader("Authorization", credentials)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                return jsonObject.getString("access_token"); // Extract token
            } else {
                throw new RuntimeException("Failed to retrieve access token: " + response.message());
            }
        }
    }

    // Method to initiate STK push
    public void initiateSTKPush(StkPushRequest stkPushRequest) throws IOException {
        String token = generateToken();
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");

        // Prepare request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("BusinessShortCode", lipaNaMpesaShortcode);
        requestBody.put("Password", generatePassword()); // Generate password
        requestBody.put("Timestamp", getCurrentTimestamp()); // Get current timestamp
        requestBody.put("TransactionType", "CustomerPayBillOnline");
        requestBody.put("Amount", stkPushRequest.getAmount()); // Use the actual amount
        requestBody.put("PartyA", stkPushRequest.getPhoneNumber()); // Use the actual phone number
        requestBody.put("PartyB", lipaNaMpesaShortcode); // Shortcode should be the same for PartyB
        requestBody.put("PhoneNumber",stkPushRequest.getPhoneNumber()); // Use the actual phone number
        requestBody.put("CallBackURL", callbackUrl);
        requestBody.put("AccountReference", "test");
        requestBody.put("TransactionDesc", "test");

        RequestBody body = RequestBody.create(mediaType, requestBody.toString());
        Request request = new Request.Builder()
                .url(apiUrl + "/mpesa/stkpush/v1/processrequest")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token) // Use the generated token
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                throw new RuntimeException("Failed to initiate STK push: " + response.message() + " - " + errorBody);
            }
            // Read and log the successful response
            String responseBody = response.body() != null ? response.body().string() : "No response body";
            System.out.println("STK Push Response: " + responseBody);
        } catch (IOException e) {
            // Log the exception message
            e.printStackTrace();
            throw new RuntimeException("IOException during STK Push: " + e.getMessage(), e);
        }
    }

    // Method to handle the callback from M-PESA
    @Transactional
    public void processCallback(String callbackResponse) {
        try {
            // Print the entire callback response for debugging
            System.out.println("Callback Response: " + callbackResponse);

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(callbackResponse);

            // Check if CallbackMetadata is present
            if (!jsonResponse.has("Body") ||
                    !jsonResponse.getJSONObject("Body").has("stkCallback") ||
                    !jsonResponse.getJSONObject("Body").getJSONObject("stkCallback").has("CallbackMetadata")) {

                // For failed transactions
                JSONObject stkCallback = jsonResponse.getJSONObject("Body").getJSONObject("stkCallback");
                String resultDesc = stkCallback.getString("ResultDesc");
                System.out.println("Transaction failed: " + resultDesc);
                return;
            }

            // Extract CallbackMetadata
            JSONObject stkCallback = jsonResponse.getJSONObject("Body").getJSONObject("stkCallback");
            JSONObject callbackMetadataJson = stkCallback.getJSONObject("CallbackMetadata");
            JSONArray itemArray = callbackMetadataJson.getJSONArray("Item");

            // Extract values from CallbackMetadata
            String amount = null;
            String mpesaCode = null;
            String phoneNumber = null;

            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject itemJson = itemArray.getJSONObject(i);
                String name = itemJson.getString("Name");

                // Handle Value as different types
                Object valueObj = itemJson.get("Value");
                String value;
                if (valueObj instanceof String) {
                    value = (String) valueObj;
                } else if (valueObj instanceof Number) {
                    value = valueObj.toString(); // Convert Number to String
                } else {
                    value = valueObj.toString();
                }

                switch (name) {
                    case "Amount":
                        amount = value;
                        break;
                    case "MpesaReceiptNumber":
                        mpesaCode = value;
                        break;
                    case "PhoneNumber":
                        phoneNumber = value;
                        break;
                }
            }

            // Create and save the transaction entity
            MpesaTransaction transaction = new MpesaTransaction();
            transaction.setAmount(amount);
            transaction.setMpesaReceiptNumber(mpesaCode);
            transaction.setPhoneNumber(phoneNumber);

            System.out.println("--------------------------saving to database--------------------");
            System.out.println(transaction);

            transactionRepository.save(transaction);

            // Log extracted values
            System.out.println(String.format("Amount: %s, MpesaCode: %s, PhoneNumber: %s", amount, mpesaCode, phoneNumber));

        } catch (Exception e) {
            // Log and rethrow the exception
            System.err.println("Failed to process callback: " + e.getMessage());
            throw new RuntimeException("Failed to process callback: " + e.getMessage(), e);
        }
    }



    // Method to get current timestamp in the required format
    private String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.now().format(formatter);
    }

    // Method to generate password for STK push
    private String generatePassword() {
        String timestamp = getCurrentTimestamp();
        String passwordString = lipaNaMpesaShortcode + lipaNaMpesaPasskey + timestamp;
        return Base64.getEncoder().encodeToString(passwordString.getBytes(StandardCharsets.UTF_8));
    }
}
