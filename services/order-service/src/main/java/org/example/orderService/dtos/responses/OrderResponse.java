package org.example.orderService.dtos.responses;


import lombok.*;
import org.example.orderService.enums.OrderStatus;
import org.example.orderService.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private Long cartId;
    private Long addressId;
    private Double totalPrice;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
