package com.safaricom.api.repository;

import com.safaricom.api.entity.MpesaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MpesaTransactionRepository extends JpaRepository<MpesaTransaction, Long> {
}
