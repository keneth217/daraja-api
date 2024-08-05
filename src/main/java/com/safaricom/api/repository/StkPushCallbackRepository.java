package com.safaricom.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.safaricom.api.entity.StkPushCallback;

@Repository
public interface StkPushCallbackRepository extends JpaRepository<StkPushCallback, Long> {
}
