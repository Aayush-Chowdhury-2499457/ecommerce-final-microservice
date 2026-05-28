package org.example.dummypaymentapi.service.impl;

import org.example.dummypaymentapi.client.PaymentServiceCallbackClient;
import org.example.dummypaymentapi.dto.request.ProcessPaymentRequest;
import org.example.dummypaymentapi.dto.response.ProcessPaymentResponse;
import org.example.dummypaymentapi.entity.DummyTransaction;
import org.example.dummypaymentapi.enums.TransactionStatus;
import org.example.dummypaymentapi.repository.DummyTransactionRepository;
import org.example.dummypaymentapi.service.DummyPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DummyPaymentServiceImpl implements DummyPaymentService {

    private final DummyTransactionRepository transactionRepository;
    private final PaymentServiceCallbackClient paymentServiceCallbackClient;

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
                .paymentLink(null)   // no payment link needed
                .callbackUrl(null)   // no callback needed
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