package org.example.dummypaymentapi.dto.callback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallbackPayload {
    private String transactionId;
    private String status;
}