package com.safaricom.api.dto;

import lombok.Data;

@Data
public class StkPushRequest {
    private String businessShortCode;
    private String password;
    private String timestamp;
    private String transactionType;
    private double amount;
    private String partyA;
    private String partyB;
    private String phoneNumber;
    private String callBackURL;
    private String accountReference;
    private String transactionDesc;
}
