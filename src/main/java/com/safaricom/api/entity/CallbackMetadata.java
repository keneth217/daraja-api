package com.safaricom.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "callback_metadata")
public class CallbackMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "callbackMetadata", orphanRemoval = true)
    private List<MetadataItem> items;
}
