package com.project.payment.repository;

import com.project.payment.entity.DeadLetterPaymentOutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterPaymentOutboxMessageRepository
        extends JpaRepository<DeadLetterPaymentOutboxMessage, Long> {}
