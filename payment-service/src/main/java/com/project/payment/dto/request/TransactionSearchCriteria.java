package com.project.payment.dto.request;

import com.project.payment.entity.enums.TransactionStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record TransactionSearchCriteria(
    String userId,
    TransactionStatus status,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
) {}
