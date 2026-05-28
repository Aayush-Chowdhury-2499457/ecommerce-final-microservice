package org.example.dummypaymentapi.service.impl;

import org.example.dummypaymentapi.client.PaymentServiceCallbackClient;
import org.example.dummypaymentapi.dto.callback.CallbackPayload;
import org.example.dummypaymentapi.dto.request.InitiatePaymentRequest;
import org.example.dummypaymentapi.dto.request.SimulatePaymentRequest;
import org.example.dummypaymentapi.dto.response.InitiatePaymentResponse;
import org.example.dummypaymentapi.dto.response.TransactionStatusResponse;
import org.example.dummypaymentapi.entity.DummyTransaction;
import org.example.dummypaymentapi.enums.TransactionStatus;
import org.example.dummypaymentapi.exception.ResourceNotFoundException;
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
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) {
        String transactionId = UUID.randomUUID().toString();
        String paymentLink = "http://localhost:9090/pay/" + transactionId;

        DummyTransaction transaction = DummyTransaction.builder()
                .transactionId(transactionId)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status(TransactionStatus.PENDING)
                .paymentLink(paymentLink)
                .callbackUrl(request.getCallbackUrl())
                .build();

        transactionRepository.save(transaction);

        log.info("Payment initiated — transactionId={}, orderId={}",
                transactionId, request.getOrderId());

        return InitiatePaymentResponse.builder()
                .transactionId(transactionId)
                .paymentLink(paymentLink)
                .status("PENDING")
                .build();
    }

    @Override
    public TransactionStatusResponse getTransactionStatus(String transactionId) {
        DummyTransaction transaction = findTransactionOrThrow(transactionId);

        return TransactionStatusResponse.builder()
                .transactionId(transaction.getTransactionId())
                .orderId(transaction.getOrderId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus().name())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void simulatePayment(String transactionId, SimulatePaymentRequest request) {
        DummyTransaction transaction = findTransactionOrThrow(transactionId);

        TransactionStatus newStatus = request.getResult().equalsIgnoreCase("SUCCESS")
                ? TransactionStatus.SUCCESS
                : TransactionStatus.FAILED;

        transaction.setStatus(newStatus);
        transactionRepository.save(transaction);

        log.info("Payment simulated — transactionId={}, result={}",
                transactionId, newStatus);

        try {
            CallbackPayload payload = new CallbackPayload(
                    transactionId,
                    newStatus.name()
            );

            paymentServiceCallbackClient.sendCallback(payload);

            log.info("Callback sent to payment-service for transactionId={}",
                    transactionId);

        } catch (Exception e) {
            log.error("Failed to send callback to payment-service — error: {}",
                    e.getMessage());
        }
    }

    private DummyTransaction findTransactionOrThrow(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found with id: " + transactionId));
    }
}