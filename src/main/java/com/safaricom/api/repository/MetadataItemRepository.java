package com.safaricom.api.repository;

import com.safaricom.api.entity.MetadataItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetadataItemRepository extends JpaRepository<MetadataItem,Long> {
}
