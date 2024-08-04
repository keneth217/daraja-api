package com.safaricom.api.service;

import com.safaricom.api.repository.StkPushResponseRepository;
import com.safaricom.api.dto.StkPushResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    private final RestTemplate restTemplate = new RestTemplate();
    private final StkPushResponseRepository responseRepository;

    public MpesaService(StkPushResponseRepository responseRepository) {
        this.responseRepository = responseRepository;
    }

    public String generateToken() {
        String tokenUrl = apiUrl + "/oauth/v1/generate?grant_type=client_credentials";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            System.out.println("-----------generating token ----------");
            System.out.println(response);
            return jsonObject.getString("access_token");
        }
        throw new RuntimeException("Failed to retrieve access token");
    }

    public void initiateSTKPush(String phoneNumber, double amount) {
        String token = generateToken();
        String stkPushUrl = apiUrl + "/mpesa/stkpush/v1/processrequest";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type", "application/json");

        JSONObject request = new JSONObject();
        request.put("BusinessShortCode", lipaNaMpesaShortcode);
        request.put("Password", generatePassword());
        request.put("Timestamp", getTimestamp());
        request.put("TransactionType", "CustomerPayBillOnline");
        request.put("Amount", amount);
        request.put("PartyA", phoneNumber);
        request.put("PartyB", lipaNaMpesaShortcode);
        request.put("PhoneNumber", phoneNumber);
        request.put("CallBackURL", callbackUrl);
        request.put("AccountReference", "Test");
        request.put("TransactionDesc", "Test");

        HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(stkPushUrl, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("----------------initiating stk push ----------");
            saveResponse(phoneNumber, amount, response.getBody(), getTimestamp());
        } else {
            throw new RuntimeException("Failed to initiate STK push");
        }
    }

    private void saveResponse(String phoneNumber, double amount, String responseBody, String timestamp) {
        StkPushResponse response = new StkPushResponse();
        response.setPhoneNumber(phoneNumber);
        response.setAmount(amount);
        response.setResponse(responseBody);
        response.setTimestamp(timestamp);
        responseRepository.save(response);
    }

    private String generatePassword() {
        // Implement base64 encoding of shortcode+passkey+timestamp
        String shortcode = lipaNaMpesaShortcode;
        String passkey = lipaNaMpesaPasskey;
        String timestamp = getTimestamp();
        String password = shortcode + passkey + timestamp;
        return java.util.Base64.getEncoder().encodeToString(password.getBytes());
    }

    private String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.now().format(formatter);
    }
}
