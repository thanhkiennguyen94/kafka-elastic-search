package com.example.orderservice.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListOrderDTOResponse {
    Long id;
    Long userId;
    String productName;
    BigDecimal totalAmount;
    LocalDateTime createdAt;
}
