package com.example.commonservice.outbox;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "aggregate_type")
    String aggregateType; // Ví dụ: "User" hoặc "Order"

    @Column(name = "aggregate_id")
    Long aggregateId;     // ID của entity chính (userId, orderId,...)

    @Column(name = "event_type")
    String eventType;     // Ví dụ: "UserCreated"

    @Lob
    @Column(name = "payload")
    String payload;       // JSON data (event details)

    @Enumerated(EnumType.STRING)
    @Column(name = "status_order")
    OutboxStatus statusOrder;  // PENDING, SENT, FAILED

    @Enumerated(EnumType.STRING)
    @Column(name = "status_elastic")
    OutboxStatus statusElastic;  // PENDING, SENT, FAILED

    @Column(name = "retry_count")
    Integer retryCount;   // Số lần retry gửi sang Kafka

    @Column(name = "last_error")
    String lastError;     // Lưu lỗi gần nhất (nếu có)

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        retryCount = 0;
        if (statusOrder == null) statusOrder = OutboxStatus.PENDING;
        if (statusElastic == null) statusElastic = OutboxStatus.PENDING;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}