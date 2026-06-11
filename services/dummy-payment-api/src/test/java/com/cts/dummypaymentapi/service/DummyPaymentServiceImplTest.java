package com.cts.dummypaymentapi.service;

import com.cts.dummypaymentapi.dto.request.ProcessPaymentRequest;
import com.cts.dummypaymentapi.dto.response.ProcessPaymentResponse;
import com.cts.dummypaymentapi.entity.DummyTransaction;
import com.cts.dummypaymentapi.enums.TransactionStatus;
import com.cts.dummypaymentapi.repository.DummyTransactionRepository;
import com.cts.dummypaymentapi.service.impl.DummyPaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DummyPaymentServiceImpl} covering the direct-success
 * payment flow and the currency-default branch.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class DummyPaymentServiceImplTest {

    @Mock
    DummyTransactionRepository transactionRepository;

    @InjectMocks
    DummyPaymentServiceImpl service;

    /**
     * Happy path: a fully-populated request is persisted as SUCCESS and the
     * response mirrors the request fields.
     */
    @Test
    void processPayment_withCurrency_returnsSuccessResponse() {
        log.info("Test: processPayment with explicit currency");
        when(transactionRepository.save(any(DummyTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProcessPaymentRequest request = new ProcessPaymentRequest(101L, 250.0, "USD");

        ProcessPaymentResponse response = service.processPayment(request);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionId()).isNotBlank();
        assertThat(response.getOrderId()).isEqualTo(101L);
        assertThat(response.getAmount()).isEqualTo(250.0);
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("Payment processed successfully");
    }

    /**
     * Branch: a null currency on the request defaults to {@code INR} on both the
     * persisted transaction and the response.
     */
    @Test
    void processPayment_nullCurrency_defaultsToInr() {
        log.info("Test: processPayment with null currency defaults to INR");
        ArgumentCaptor<DummyTransaction> captor = ArgumentCaptor.forClass(DummyTransaction.class);
        when(transactionRepository.save(any(DummyTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProcessPaymentRequest request = new ProcessPaymentRequest(202L, 99.5, null);

        ProcessPaymentResponse response = service.processPayment(request);

        assertThat(response.getCurrency()).isEqualTo("INR");

        verify(transactionRepository, times(1)).save(captor.capture());
        DummyTransaction saved = captor.getValue();
        assertThat(saved.getCurrency()).isEqualTo("INR");
        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(saved.getOrderId()).isEqualTo(202L);
        assertThat(saved.getAmount()).isEqualTo(99.5);
        assertThat(saved.getTransactionId()).isEqualTo(response.getTransactionId());
    }

    /**
     * Verifies the persisted transaction is always saved with SUCCESS status and
     * the generated transaction id matches the response.
     */
    @Test
    void processPayment_persistsTransactionAsSuccess() {
        log.info("Test: processPayment persists transaction as SUCCESS");
        ArgumentCaptor<DummyTransaction> captor = ArgumentCaptor.forClass(DummyTransaction.class);
        when(transactionRepository.save(any(DummyTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProcessPaymentResponse response =
                service.processPayment(new ProcessPaymentRequest(303L, 10.0, "EUR"));

        verify(transactionRepository).save(captor.capture());
        DummyTransaction saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(saved.getTransactionId()).isEqualTo(response.getTransactionId());
        assertThat(saved.getCurrency()).isEqualTo("EUR");
    }
}
