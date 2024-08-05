package com.safaricom.api.entity;

import com.safaricom.api.entity.CallbackMetadata;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "callback_metadata_items")
public class MetadataItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value; // Store as String

    @ManyToOne
    @JoinColumn(name = "callback_metadata_id")
    private CallbackMetadata callbackMetadata;
}
