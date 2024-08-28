package com.safaricom.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "mpesa_transactions")
public class MpesaTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount")
    private String amount;

    @Column(name = "mpesa_receipt_number")
    private String mpesaReceiptNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL)
    private List<MetadataItem> metadataItems;  // 'mappedBy' should correspond to 'transaction' in MetadataItem
}
