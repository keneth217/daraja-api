package com.safaricom.api.dto;

import lombok.Data;

@Data
public class StkPushResponse {
    private String id;
    private String merchantRequestId;
    private String checkoutRequestId;
    private int responseCode;
    private String responseDescription;
    private String customerMessage;

}
