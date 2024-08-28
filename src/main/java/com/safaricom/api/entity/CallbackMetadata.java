package com.safaricom.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "callback_metadata")
public class CallbackMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "callbackMetadata", cascade = CascadeType.ALL)
    private List<MetadataItem> items;  // 'mappedBy' should correspond to 'callbackMetadata' in MetadataItem

    // Other fields and methods
}
