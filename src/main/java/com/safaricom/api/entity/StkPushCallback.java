package com.safaricom.api.entity;

import com.safaricom.api.entity.CallbackMetadata;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "stk_push_callbacks")
public class StkPushCallback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_request_id")
    private String merchantRequestId;

    @Column(name = "checkout_request_id")
    private String checkoutRequestId;

    @Column(name = "result_code")
    private int resultCode;

    @Column(name = "result_desc")
    private String resultDesc;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "callback_metadata_id")
    private CallbackMetadata callbackMetadata;
}
