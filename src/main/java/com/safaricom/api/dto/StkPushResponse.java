package com.safaricom.api.dto;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "stk_push_responses")
public class StkPushResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String phoneNumber;
    private double amount;
    @Column(length = 2000) // or @Lob for TEXT type
    private String response;
    private String timestamp;
}
