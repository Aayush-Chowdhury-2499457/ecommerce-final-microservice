package com.cts.paymentservice.repository;

import com.cts.paymentservice.entity.Payment;
import com.cts.paymentservice.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data repository for {@link Payment} entities.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds the payment for the given order id.
     *
     * @param orderId the order id
     * @return the matching payment, if any
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Checks whether a payment with the given order id and status exists.
     *
     * @param orderId the order id
     * @param status  the payment status
     * @return {@code true} if such a payment exists
     */
    boolean existsByOrderIdAndPaymentStatus(Long orderId, PaymentStatus status);
}