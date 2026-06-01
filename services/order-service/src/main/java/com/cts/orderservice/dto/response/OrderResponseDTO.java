package com.cts.orderservice.dto.response;


import lombok.*;
import com.cts.orderservice.enums.OrderStatus;
import com.cts.orderservice.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {
    private Long orderId;
    private Long userId;
    private Long cartId;
    private Long addressId;
    private Double totalPrice;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private List<OrderItemResponseDTO> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
