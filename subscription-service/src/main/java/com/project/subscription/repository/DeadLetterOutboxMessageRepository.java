package com.project.subscription.repository;

import com.project.subscription.entity.DeadLetterOutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterOutboxMessageRepository extends JpaRepository<DeadLetterOutboxMessage, Long> {}
