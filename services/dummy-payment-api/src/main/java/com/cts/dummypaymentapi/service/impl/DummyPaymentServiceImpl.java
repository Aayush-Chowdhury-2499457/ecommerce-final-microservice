package com.cts.dummypaymentapi.service.impl;

import com.cts.dummypaymentapi.dto.request.ProcessPaymentRequest;
import com.cts.dummypaymentapi.dto.response.ProcessPaymentResponse;
import com.cts.dummypaymentapi.entity.DummyTransaction;
import com.cts.dummypaymentapi.enums.TransactionStatus;
import com.cts.dummypaymentapi.repository.DummyTransactionRepository;
import com.cts.dummypaymentapi.service.DummyPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DummyPaymentServiceImpl implements DummyPaymentService {

    private final DummyTransactionRepository transactionRepository;

    @Override
    @Transactional
    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request) {

        // Generate transactionId
        String transactionId = UUID.randomUUID().toString();

        // Save transaction as SUCCESS directly — no pending, no simulate
        DummyTransaction transaction = DummyTransaction.builder()
                .transactionId(transactionId)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status(TransactionStatus.SUCCESS)
                .build();

        transactionRepository.save(transaction);

        log.info("Payment processed directly — transactionId={}, orderId={}",
                transactionId, request.getOrderId());

        // Return response directly — no POST to anyone
        return ProcessPaymentResponse.builder()
                .transactionId(transactionId)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status("SUCCESS")
                .message("Payment processed successfully")
                .build();
    }
}