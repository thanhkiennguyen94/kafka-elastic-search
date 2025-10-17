package com.example.orderservice.kafka.consumer;

import com.example.commonservice.event.CreateUserEvent;
import com.example.commonservice.util.ConstantGroup;
import com.example.commonservice.util.ConstantTopic;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.request.Order.CreateOrderRequest;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCreatedConsumer {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @KafkaListener(topics = ConstantTopic.CREATE_USER_TOPIC, groupId = ConstantGroup.ORDER_GROUP)
    public void handleUserCreated(CreateUserEvent event) {
        log.info("📩 Received user created event for userId={}", event.getUserId());
        // Tạo order mặc định
        orderService.createOrder(new CreateOrderRequest(event.getUserId(), "DEFAULT_PRODUCT", BigDecimal.ZERO));
        log.info("✅ Default order created for user {}", event.getUserId());
    }
}